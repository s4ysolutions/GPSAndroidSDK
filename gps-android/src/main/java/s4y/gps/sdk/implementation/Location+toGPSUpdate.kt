package s4y.gps.sdk.implementation

import android.location.Location
import s4y.gps.sdk.GPSUpdate

fun Location.toGPSUpdate(): GPSUpdate = GPSUpdate(
    latitude,
    longitude,
    speed,
    accuracy,
    bearing.toDouble(),
    time
)

