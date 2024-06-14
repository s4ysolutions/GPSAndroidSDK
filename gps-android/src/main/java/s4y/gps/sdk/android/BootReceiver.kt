package s4y.gps.sdk.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action || "android.permission.QUICKBOOT_POWERON" == intent.action) {
            if (GPSPreferences.keepAlive(context)) {
                val startIntent = Intent(context, GPSUpdatesForegroundService::class.java)
                // it is assumed service will be configured in Application.onCreate
                startIntent.action = GPSUpdatesForegroundService.ACTION_START
                context.startService(startIntent)
            }
        }
    }
}
