package s4y.gps.sdk.filters.dependencies

import kotlinx.coroutines.flow.StateFlow
import s4y.gps.sdk.filters.GPSFilter

interface IGPSFilterProvider {
    var filter: GPSFilter
    fun asStateFlow(): StateFlow<GPSFilter>
}
