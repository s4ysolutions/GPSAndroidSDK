package s4y.gps.sdk.store

import s4y.gps.sdk.store.dependencies.IGPSUpdatesSerializer
import s4y.gps.sdk.GPSUpdate

class GPSUpdatesSerializerCSV : IGPSUpdatesSerializer {
    companion object {
        const val separator = ","
        const val header = "latitude,longitude,velocity,accuracy,bearing,ts"
    }

    override fun serialize(gpsUpdates: Array<GPSUpdate>): String {
        val sb = StringBuilder()
        sb.append(header)
        sb.append("\n")
        gpsUpdates.forEach {
            sb.append(it.latitude)
            sb.append(separator)
            sb.append(it.longitude)
            sb.append(separator)
            sb.append(it.velocity)
            sb.append(separator)
            sb.append(it.accuracy)
            sb.append(separator)
            sb.append(it.bearing)
            sb.append(separator)
            sb.append(it.ts)
            sb.append("\n")
        }
        return sb.toString()
    }
}