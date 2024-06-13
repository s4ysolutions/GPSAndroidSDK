package s4y.gps.sdk.android.implementation

import android.content.Context
import s4y.gps.sdk.IGPSProvider

class Preferences(context: Context) {
    private val preferences = context.getSharedPreferences("gps_sdk", Context.MODE_PRIVATE)
    var granularity: IGPSProvider.Granularity =
        preferences.getInt("granularity", IGPSProvider.Granularity.PERMISSION_LEVEL.ordinal)
            .let { IGPSProvider.Granularity.entries[it] }
        set(value) {
            field = value
            preferences.edit().putInt("granularity", value.ordinal).apply()
        }
    var priority: IGPSProvider.Priority =
        preferences.getInt("priority", IGPSProvider.Priority.HIGH_ACCURACY.ordinal)
            .let { IGPSProvider.Priority.entries[it] }
        set(value) {
            field = value
            preferences.edit().putInt("priority", value.ordinal).apply()
        }
    var maxUpdateAgeMillis: Long =
        preferences.getLong("maxUpdateAgeMillis", 5000L)
        set(value) {
            field = value
            preferences.edit().putLong("maxUpdateAgeMillis", value).apply()
        }
    var durationMillis: Long =
        preferences.getLong("durationMillis", Long.MAX_VALUE)
        set(value) {
            field = value
            preferences.edit().putLong("durationMillis", value).apply()
        }
    var intervalMillis: Long =
        preferences.getLong("intervalMillis", 0L) //ASAP
        set(value) {
            field = value
            preferences.edit().putLong("intervalMillis", value).apply()
        }
    var maxUpdateDelayMillis: Long =
        preferences.getLong("maxUpdateDelayMillis", 0L)
        set(value) {
            field = value
            preferences.edit().putLong("maxUpdateDelayMillis", value).apply()
        }
    var maxUpdates: Int =
        preferences.getInt("maxUpdates", Int.MAX_VALUE) //infinity
        set(value) {
            field = value
            preferences.edit().putInt("maxUpdates", value).apply()
        }
    var minUpdateDistanceMeters: Float =
        preferences.getFloat("minUpdateDistanceMeters", 0f) //update even if the user did not move
        set(value) {
            field = value
            preferences.edit().putFloat("minUpdateDistanceMeters", value).apply()
        }
    var minUpdateIntervalMillis: Long =
        preferences.getLong("minUpdateIntervalMillis", -1L) //TODO: double check
        set(value) {
            field = value
            preferences.edit().putLong("minUpdateIntervalMillis", value).apply()
        }
    var waitForAccurateLocation: Boolean =
        preferences.getBoolean("waitForAccurateLocation", false)
        set(value) {
            field = value
            preferences.edit().putBoolean("waitForAccurateLocation", value).apply()
        }
}