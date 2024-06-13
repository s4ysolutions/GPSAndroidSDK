package s4y.gps.sdk.dependencies

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import s4y.gps.sdk.GPSUpdate
import s4y.gps.sdk.IGPSProvider

interface IGPSCurrentPositionProvider : IGPSProvider {
    interface IStatus {
        fun asStateFlow(): StateFlow<Boolean>
    }

    val status: IStatus

    fun cancel()
    fun request(): Flow<GPSUpdate>
}