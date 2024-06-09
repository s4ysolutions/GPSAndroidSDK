package s4y.gps.sdk.filters

import s4y.gps.sdk.GPSUpdate

class GPSFilterNull : GPSFilter() {
    companion object {
        val instance = GPSFilterNull()
    }

    override val name: String = "No filtering"

    override fun apply(gpsUpdate: GPSUpdate): GPSUpdate {
        return gpsUpdate
    }

    override fun apply(gpsUpdates: Array<GPSUpdate>): Array<GPSUpdate> {
        return gpsUpdates
    }

    override fun reset() {
    }
}