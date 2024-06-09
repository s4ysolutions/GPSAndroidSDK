package s4y.gps.sdk.store.dependencies

import s4y.gps.sdk.GPSUpdate

interface IGPSUpdatesSerializer {
    fun serialize(gpsUpdate: Array<GPSUpdate>): String
}