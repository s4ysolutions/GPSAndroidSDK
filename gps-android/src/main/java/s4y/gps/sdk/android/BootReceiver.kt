package s4y.gps.sdk.android

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action || "android.permission.QUICKBOOT_POWERON" == intent.action) {
            if (GPSPreferences.keepAlive(context)) {
                // it is assumed service will be configured in Application.onCreate
                GPSUpdatesForegroundService.start(context)
            }
        }
    }
}
