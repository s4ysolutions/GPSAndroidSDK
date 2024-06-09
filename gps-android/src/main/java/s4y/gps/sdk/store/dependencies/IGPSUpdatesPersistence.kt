package s4y.gps.sdk.store.dependencies

import s4y.gps.sdk.GPSUpdate

interface IGPSUpdatesPersistence {
    fun save(gpsUpdates: Array<GPSUpdate>, serializer: IGPSUpdatesSerializer): String
}