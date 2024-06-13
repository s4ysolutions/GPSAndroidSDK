package s4y.gps.sdk.dependencies

import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import s4y.gps.sdk.GPSUpdate
import s4y.gps.sdk.IGPSProvider

interface IGPSUpdatesProvider : IGPSProvider {
    // locationUpdates parameters
    var intervalMillis: Long
    var maxUpdateDelayMillis: Long
    var maxUpdates: Int
    var minUpdateDistanceMeters: Float
    var minUpdateIntervalMillis: Long
    var waitForAccurateLocation: Boolean

    fun startUpdates()
    fun stopUpdates()

    interface IUpdates {
        fun asSharedFlow(): SharedFlow<GPSUpdate>
        fun addListener(listener: (GPSUpdate) -> Unit)
        fun removeListener(listener: (GPSUpdate) -> Unit)
    }

    val updates: IUpdates

    enum class Status {
        IDLE,
        WARMING_UP,
        ACTIVE,
    }

    fun asStateFlow(): StateFlow<Status>
}