package s4y.gps.sdk.android

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Looper
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import s4y.gps.sdk.GPSUpdatesManager
import s4y.gps.sdk.android.GPSUpdatesForegroundService.Companion.start
import s4y.gps.sdk.android.GPSUpdatesForegroundService.Companion.stop
import s4y.gps.sdk.dependencies.IGPSUpdatesProvider

/**
 * The service is managed by static methods [start] and [stop]
 *
 * It is launched only when the static property [GPSUpdatesManager] is not idle
 * It tracks the status of [GPSUpdatesManager] and stops itself when the status is idle
 */
@Suppress("unused")
class GPSUpdatesForegroundService : LifecycleService() {
    override fun onCreate() {
        println("GPSUpdatesForegroundService.onCreate(${System.currentTimeMillis()})")
        super.onCreate()
        val context = this
        updatesManager?.let { updatesManager ->
            lifecycleScope.launch {
                println("GPSUpdatesForegroundService.updatesManager.collect(${System.currentTimeMillis()})")
                updatesManager.status.asStateFlow().collect {
                    if (IGPSUpdatesProvider.Status.IDLE == it) {
                        println("GPSUpdatesForegroundService.updatesManager.IDLE(${System.currentTimeMillis()})")
                        stop(context)
                    }
                }
            }

        }
    }

    private var inForeground = false
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("GPSUpdatesForegroundService.onStartCommand(${System.currentTimeMillis()})")
        super.onStartCommand(intent, flags, startId)
        if (intent == null) {
            println("GPSUpdatesForegroundService.onStartCommand(intnet=null, inForeground=${inForeground}, ${System.currentTimeMillis()})")
            if (!inForeground) {
                enterForeground()
            }
            return START_STICKY
        } else {
            if (intent.action == ACTION_START) {
                println("GPSUpdatesForegroundService.onStartCommand(ACTION_START, ${System.currentTimeMillis()})")
                enterForeground()
                return START_REDELIVER_INTENT
            } else if (intent.action == ACTION_STOP) {
                println("GPSUpdatesForegroundService.onStartCommand(ACTION_STOP, ${System.currentTimeMillis()})")
                exit()
                return START_NOT_STICKY
            } else if (intent.action == ACTION_REMOVE_FROM_FOREGROUND) {
                println("GPSUpdatesForegroundService.onStartCommand(ACTION_REMOVE_FROM_FOREGROUND, ${System.currentTimeMillis()})")
                removeFromForeground()
                return START_STICKY
            } else if (intent.action == ACTION_STOP_TRACKING) {
                println("GPSUpdatesForegroundService.onStartCommand(ACTION_STOP_TRACKING, ${System.currentTimeMillis()})")
                Handler(Looper.getMainLooper()).postDelayed({
                    updatesManager?.stop()
                }, 1000)
                updatesManager?.stop()
                return START_STICKY
            }
        }
        val inactive = updatesManager?.status?.isIdle ?: true
        println("call GPSUpdatesForegroundService.enterForeground(${System.currentTimeMillis()})")
        enterForeground()
        if (inactive) {
            exit()
            return START_NOT_STICKY
        } else {
            return START_STICKY
        }
    }

    override fun onDestroy() {
        println("GPSUpdatesForegroundService.onDestroy(${System.currentTimeMillis()})")
        super.onDestroy()
    }

    private fun removeFromForeground() {
        println("GPSUpdatesForegroundService.removeFromForeground(${System.currentTimeMillis()})")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // leaves notification in the notification bar
            // stopForeground(STOP_FOREGROUND_DETACH)
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
        inForeground = false
    }

    private fun exit() {
        println("GPSUpdatesForegroundService.stopSelf(${System.currentTimeMillis()})")
        removeFromForeground()
        stopSelf()
    }

    private fun enterForeground() {
        println("GPSUpdatesForegroundService.enterForeground(${System.currentTimeMillis()})")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                notificationChannelId,
                notificationChannelName,
                notificationChannelImportance
            )
            val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            println("GPSUpdatesForegroundService.createNotificationChannel(${notificationChannel},${System.currentTimeMillis()})")
            manager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(this, notificationChannelId)
            .also { builder ->
                notificationBuilder?.invoke(builder)
                    ?: defaultNotificationBuilder(builder)
            }
        val notification = builder.build()
        try {
            println("GPSUpdatesForegroundService.startForeground(${System.currentTimeMillis()})")
            startForeground(notificationId, notification)
            inForeground = true
        } catch (e: Exception) {
            println("GPSUpdatesForegroundService.startForeground(error, ${System.currentTimeMillis()})")
            // TODO: log the exception
            e.printStackTrace()
        }
    }

    private fun defaultNotificationBuilder(builder: NotificationCompat.Builder): NotificationCompat.Builder {
        val pendingIntent = packageManager.getLaunchIntentForPackage(this.packageName)?.let{intent ->
            intent.putExtra(GPS_SERVICE_NOTIFICATION, ACTION_STOP_TRACKING)
            PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        } ?: run {
            val intent = Intent(this, this::class.java).setAction(ACTION_STOP_TRACKING)
            intent.putExtra(GPS_SERVICE_NOTIFICATION, ACTION_STOP_TRACKING)
            PendingIntent.getService(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        /*
        val stopIntent = PendingIntent.getService(
            this,
            0,
            Intent(this, this::class.java).setAction(ACTION_STOP_TRACKING),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
         */
        return builder
            .setContentTitle(notificationContentTitle)
            .setContentIntent(pendingIntent)
            .setSmallIcon(notificationSmallIcon)
            //   .addAction(android.R.drawable.ic_media_pause, "Stop", stopIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
    }

    companion object {
        const val ACTION_START: String = "s4y.waytoday.start"
        const val ACTION_REMOVE_FROM_FOREGROUND: String = "s4y.waytoday.remove_from_foreground"
        const val ACTION_STOP: String = "s4y.waytoday.stop"
        const val ACTION_STOP_TRACKING: String = "s4y.waytoday.stop_tracking"
        const val GPS_SERVICE_NOTIFICATION: String = "s4y.waytoday.notification"

        @JvmStatic
        var notificationId = 1

        @JvmStatic
        var notificationChannelId = "S4YGPSDemo"

        @JvmStatic
        var notificationChannelName: String = "S4Y GPS Demo"

        @JvmStatic
        var notificationChannelImportance = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationManager.IMPORTANCE_DEFAULT
        } else {
            0
        }

        @JvmStatic
        var notificationContentTitle = "S4Y GPS Demo"

        @JvmStatic
        var notificationSmallIcon = R.drawable.notification

        @JvmStatic
        var updatesManager: GPSUpdatesManager? = null

        @JvmStatic
        var notificationBuilder: ((builder: NotificationCompat.Builder) -> NotificationCompat.Builder)? =
            null

        @JvmStatic
        fun start(context: Context) {
            println("GPSUpdatesForegroundService.start(${System.currentTimeMillis()})")
            val intent = Intent(context, GPSUpdatesForegroundService::class.java)
            intent.setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                println("GPSUpdatesForegroundService.startForegroundService(${System.currentTimeMillis()})")
                context.startForegroundService(intent)
            } else {
                println("GPSUpdatesForegroundService.startService(${System.currentTimeMillis()})")
                context.startService(intent)
            }
        }

        @JvmStatic
        fun stop(context: Context) {
            println("GPSUpdatesForegroundService.stop(${System.currentTimeMillis()})")
            val intent = Intent(context, GPSUpdatesForegroundService::class.java)
            intent.setAction(ACTION_STOP)
            context.stopService(intent)
        }

        @JvmStatic
        fun removeFromForeground(context: Context) {
            println("GPSUpdatesForegroundService.removeFromForeground(${System.currentTimeMillis()})")
            val intent = Intent(context, GPSUpdatesForegroundService::class.java)
            intent.setAction(ACTION_REMOVE_FROM_FOREGROUND)
            context.startService(intent)
        }
    }
}