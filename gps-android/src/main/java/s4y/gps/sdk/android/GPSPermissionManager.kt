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

        private fun isNotGranted(permission: Int) = permission != PackageManager.PERMISSION_GRANTED

        private fun needLocationPermissionRequest(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
                return false

            return isNotGranted(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION))
                    &&
                    isNotGranted(context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION))

        }

        private fun needBackgroundLocationPermissionRequest(context: Context): Boolean {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q)
                return false

            return isNotGranted(context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
        }

        @JvmStatic
        fun needPermissionRequest(context: Context, workInBackground: Boolean = true): Boolean {
            val locationsNotGranted = needLocationPermissionRequest(context)

            if (!workInBackground)
                return locationsNotGranted

            return needBackgroundLocationPermissionRequest(context)
        }

        @JvmStatic
        fun requestPermissions(activity: Activity, workInBackground: Boolean = true) {
            if (workInBackground)
                requestPermissions(
                    activity,
                    R.string.request_location_permission,
                    R.string.request_location_background_permission
                )
            else
                requestPermissions(
                    activity,
                    R.string.request_location_permission,
                    null
                )
        }

        @JvmStatic
        fun requestPermissions(
            activity: Activity,
            messageForLocation: String,
            messageForBackgroundLocation: String?
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (needLocationPermissionRequest(activity)) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                        builder
                            .setMessage(messageForLocation)
                            .setTitle(R.string.request_permission_title)
                            .setPositiveButton(
                                android.R.string.ok
                            ) { _, _ ->
                                activity.requestPermissions(
                                    arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                                    REQUEST_ENABLE_LOCATION
                                )
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                            .show()
                    } else {
                        activity.requestPermissions(
                            arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_ENABLE_LOCATION
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && messageForBackgroundLocation != null && needBackgroundLocationPermissionRequest(activity)) {
                    if (activity.shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
                        val builder: AlertDialog.Builder = AlertDialog.Builder(activity)
                        builder
                            .setMessage(messageForBackgroundLocation)
                            .setTitle(R.string.request_permission_title)
                            .setPositiveButton(
                                android.R.string.ok
                            ) { _, _ ->
                                activity.requestPermissions(
                                    arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                                    REQUEST_ENABLE_LOCATION
                                )
                            }
                            .setNegativeButton(android.R.string.cancel) { dialog, _ -> dialog.cancel() }
                            .show()
                    } else {
                        activity.requestPermissions(
                            arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION),
                            REQUEST_ENABLE_LOCATION
                        )
                    }
                }
            }
        }

        @JvmStatic
        fun requestPermissions(
            activity: Activity,
            messageForLocation: Int,
            messageForBackgroundLocation: Int?
        ) {
            requestPermissions(
                activity,
                activity.getString(messageForLocation),
                messageForBackgroundLocation?.let { activity.getString(it) }
            )
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