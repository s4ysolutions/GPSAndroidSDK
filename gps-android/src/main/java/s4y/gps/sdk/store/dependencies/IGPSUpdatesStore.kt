package s4y.gps.sdk.store.dependencies

import kotlinx.coroutines.flow.SharedFlow
import s4y.gps.sdk.GPSUpdate

interface IGPSUpdatesStore: MutableCollection<GPSUpdate> {
    var capacity: Int
    val snapshot: Array<GPSUpdate>
    val lastUpdate: SharedFlow<GPSUpdate>
    fun saveAsFile(): String
}