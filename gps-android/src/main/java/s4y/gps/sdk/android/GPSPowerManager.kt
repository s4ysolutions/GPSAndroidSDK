package s4y.gps.sdk.android

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS
import android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
import androidx.appcompat.app.AlertDialog

@Suppress("unused")
class GPSPowerManager(private val context: Context) {
    private var ignoreOptimizationRequested: Boolean
        get() {
            val sp =
                context.getSharedPreferences("solutions.s4y.waytoday.sdk", Context.MODE_PRIVATE)
            return sp.getBoolean("ignoreOptimizationRequested", false)
        }
        set(value) {
            val sp =
                context.getSharedPreferences("solutions.s4y.waytoday.sdk", Context.MODE_PRIVATE)
            sp.edit().putBoolean("ignoreOptimizationRequested", value).apply()
        }

    val needRequestIgnoreOptimization: Boolean
        get() {
            if (ignoreOptimizationRequested) return false
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val packageName = context.packageName
                val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
                val isIgnoringBatteryOptimizations = pm.isIgnoringBatteryOptimizations(packageName)
                !isIgnoringBatteryOptimizations
            } else {
                false
            }
        }

    fun requestIgnoreOptimization() {
        requestIgnoreOptimization(
            R.string.request_ignore_battery_optimization,
            R.string.request_ignore_battery_optimization_title,
            R.string.request_ignore_battery_optimization_ok,
            R.string.request_ignore_battery_optimization_cancel
        )
    }

    fun requestIgnoreOptimization(
        stringRequestIgnoreBatteryOptimizationMessage: Int,
        stringRequestIgnoreBatteryOptimizationTitle: Int,
        stringRequestIgnoreBatteryOptimizationOk: Int,
        stringRequestIgnoreBatteryOptimizationCancel: Int
    ) {
        if (!needRequestIgnoreOptimization) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(stringRequestIgnoreBatteryOptimizationMessage)
                .setTitle(stringRequestIgnoreBatteryOptimizationTitle)
                .setPositiveButton(
                    stringRequestIgnoreBatteryOptimizationOk
                ) { dialog, which ->
                    ignoreOptimizationRequested = true
                    dialog.dismiss()
                    val intent = Intent()
                    intent.action = ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                    intent.data = Uri.parse("package:${context.packageName}")
                    context.startActivity(intent)
                }
                .setNegativeButton(stringRequestIgnoreBatteryOptimizationCancel) { dialog, which ->
                    ignoreOptimizationRequested = true
                    dialog.cancel()
                }
                .show()
        }
    }

    companion object {
        @JvmStatic
        fun needRequestIgnoreOptimization(context: Context): Boolean =
            GPSPowerManager(context).needRequestIgnoreOptimization

        @JvmStatic
        fun requestIgnoreOptimization(context: Context) =
            GPSPowerManager(context).requestIgnoreOptimization()
        @JvmStatic
        fun requestIgnoreOptimization(
            context: Context,
            stringRequestIgnoreBatteryOptimizationMessage: Int,
            stringRequestIgnoreBatteryOptimizationTitle: Int,
            stringRequestIgnoreBatteryOptimizationOk: Int,
            stringRequestIgnoreBatteryOptimizationCancel: Int,
        ) = GPSPowerManager(context).requestIgnoreOptimization(
            stringRequestIgnoreBatteryOptimizationMessage,
            stringRequestIgnoreBatteryOptimizationTitle,
            stringRequestIgnoreBatteryOptimizationOk,
            stringRequestIgnoreBatteryOptimizationCancel
        )

        @JvmStatic
        fun requestIgnoreOptimization(
            context: Context,
            stringRequestIgnoreBatteryOptimizationMessage: Int,
            stringRequestIgnoreBatteryOptimizationTitle: Int,
            stringRequestIgnoreBatteryOptimizationOk: Int
        ) = GPSPowerManager(context).requestIgnoreOptimization(
            stringRequestIgnoreBatteryOptimizationMessage,
            stringRequestIgnoreBatteryOptimizationTitle,
            stringRequestIgnoreBatteryOptimizationOk,
            R.string.request_ignore_battery_optimization_cancel
        )
        @JvmStatic
        fun requestIgnoreOptimization(
            context: Context,
            stringRequestIgnoreBatteryOptimizationMessage: Int,
            stringRequestIgnoreBatteryOptimizationTitle: Int,
        ) = GPSPowerManager(context).requestIgnoreOptimization(
            stringRequestIgnoreBatteryOptimizationMessage,
            stringRequestIgnoreBatteryOptimizationTitle,
            R.string.request_ignore_battery_optimization_ok,
            R.string.request_ignore_battery_optimization_cancel,
        )
        @JvmStatic
        fun requestIgnoreOptimization(
            context: Context,
            stringRequestIgnoreBatteryOptimizationMessage: Int,
        ) = GPSPowerManager(context).requestIgnoreOptimization(
            stringRequestIgnoreBatteryOptimizationMessage,
            R.string.request_ignore_battery_optimization_title,
            R.string.request_ignore_battery_optimization_ok,
            R.string.request_ignore_battery_optimization_cancel,
        )
    }
}