package s4y.gps.sdk.android

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AlertDialog
import s4y.gps.sdk.GPSUpdatesManager

@Suppress("unused")
class GPSPermissionManager {
    companion object {
        private val REQUEST_ENABLE_LOCATION = 22031971

        @JvmStatic
        fun needPermissionRequest(context: Context): Boolean {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        }

        @JvmStatic
        fun requestPermissions(activity: Activity) {
            requestPermissions(activity, R.string.request_location_permission)
        }

        @JvmStatic
        fun requestPermissions(activity: Activity, message: String) {
            if (needPermissionRequest(activity)) {
                if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                    builder.setMessage(message)
                        .setTitle(R.string.request_permission_title)
                        .setPositiveButton(
                            android.R.string.ok
                        ) { dialog, which ->
                            activity.requestPermissions(
                                arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                REQUEST_ENABLE_LOCATION
                            )
                        }
                        .setNegativeButton(android.R.string.cancel) { dialog, which -> dialog.cancel() }
                        .show()
                } else {
                    activity.requestPermissions(
                        arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                        REQUEST_ENABLE_LOCATION
                    )
                }
            }
        }
        @JvmStatic
        fun requestPermissions(activity: Activity, message: Int) {
            requestPermissions(activity, activity.getString(message))
        }

        @JvmStatic
        fun handleOnRequestPermissionsResult(
            requestCode: Int,
            gpsUpdatesManager: GPSUpdatesManager,
            start: Boolean
        ) {
            if (requestCode == REQUEST_ENABLE_LOCATION && start) {
                gpsUpdatesManager.start()
            }
        }
    }
}