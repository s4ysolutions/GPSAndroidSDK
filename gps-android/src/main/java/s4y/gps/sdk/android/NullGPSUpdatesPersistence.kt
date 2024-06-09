package s4y.gps.sdk.android

import s4y.gps.sdk.GPSUpdate
import s4y.gps.sdk.store.dependencies.IGPSUpdatesPersistence
import s4y.gps.sdk.store.dependencies.IGPSUpdatesSerializer

class NullGPSUpdatesPersistence: IGPSUpdatesPersistence {
    override fun save(gpsUpdates: Array<GPSUpdate>, serializer: IGPSUpdatesSerializer): String {
        return ""
    }
}