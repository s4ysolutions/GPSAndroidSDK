package s4y.gps.sdk.filters.kalman

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import s4y.gps.sdk.filters.GPSFilter
import s4y.gps.sdk.filters.dependencies.IGPSFilterProvider

class GPSFilterKalmanConstantPositionProvider: IGPSFilterProvider {
    companion object {
        val instance = GPSFilterKalmanConstantPositionProvider()
    }
    override var filter: GPSFilter = GPSFilterKalmanConstantPosition.instance
    override fun asStateFlow(): StateFlow<GPSFilter> =
        MutableStateFlow(filter)
}