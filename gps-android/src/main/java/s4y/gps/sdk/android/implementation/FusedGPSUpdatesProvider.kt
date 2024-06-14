package s4y.gps.sdk.android.implementation

import android.Manifest
import android.content.Context
import android.os.HandlerThread
import android.os.Looper
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asStateFlow
import s4y.gps.sdk.IGPSProvider
import s4y.gps.sdk.dependencies.IGPSUpdatesProvider
import s4y.gps.sdk.GPSUpdate
import s4y.gps.sdk.android.GPSPreferences

@Suppress("unused")
class FusedGPSUpdatesProvider(private val context: Context, private val looper: Looper? = null) :
    IGPSUpdatesProvider {
    private val preferences = GPSPreferences(context)
    override var granularity: IGPSProvider.Granularity
        get() = preferences.granularity
        set(value) {
            preferences.granularity = value
            restartIfNeeded()
        }
    override var priority: IGPSProvider.Priority
        get() = preferences.priority
        set(value) {
            preferences.priority = value
            restartIfNeeded()
        }
    override var maxUpdateAgeMillis: Long
        get() = preferences.maxUpdateAgeMillis
        set(value) {
            preferences.maxUpdateAgeMillis = value
            restartIfNeeded()
        }
    override var durationMillis: Long
        get() = preferences.durationMillis
        set(value) {
            preferences.durationMillis = value
            restartIfNeeded()
        }
    override var intervalMillis: Long
        get() = preferences.intervalMillis
        set(value) {
            preferences.intervalMillis = value
            restartIfNeeded()
        }
    override var maxUpdateDelayMillis: Long
        get() = preferences.maxUpdateDelayMillis
        set(value) {
            preferences.maxUpdateDelayMillis = value
            restartIfNeeded()
        }
    override var maxUpdates: Int
        get() = preferences.maxUpdates
        set(value) {
            preferences.maxUpdates = value
            restartIfNeeded()
        }
    override var minUpdateDistanceMeters: Float
        get() = preferences.minUpdateDistanceMeters
        set(value) {
            preferences.minUpdateDistanceMeters = value
            restartIfNeeded()
        }
    override var minUpdateIntervalMillis: Long
        get() = preferences.minUpdateIntervalMillis
        set(value) {
            preferences.minUpdateIntervalMillis = value
            restartIfNeeded()
        }
    override var waitForAccurateLocation: Boolean
        get() = preferences.waitForAccurateLocation
        set(value) {
            preferences.waitForAccurateLocation = value
            restartIfNeeded()
        }

    private val _status = MutableStateFlow(IGPSUpdatesProvider.Status.IDLE)
    private val _statusListeners = mutableListOf<(IGPSUpdatesProvider.Status) -> Unit>()
    private val _notifyStatusChanged: (IGPSUpdatesProvider.Status) -> Unit = { status ->
        _status.value = status
        val listeners = synchronized(_statusListeners) { _statusListeners.toList() }
        listeners.forEach { it(status) }
    }

    override fun asStateFlow() = _status.asStateFlow()

    private val _updates = MutableSharedFlow<GPSUpdate>(1, 0, BufferOverflow.DROP_OLDEST)
    private val _updatesListeners = mutableListOf<(GPSUpdate) -> Unit>()
    private val _notifyUpdate: (GPSUpdate) -> Unit = { update ->
        _updates.tryEmit(update)
        if (_updatesListeners.isNotEmpty()) {
            val listeners = synchronized(_updatesListeners) { _updatesListeners.toList() }
            listeners.forEach { it(update) }
        }
    }
    override val updates = object : IGPSUpdatesProvider.IUpdates {
        override fun asSharedFlow(): SharedFlow<GPSUpdate> = _updates
        override fun addListener(listener: (GPSUpdate) -> Unit) {
            synchronized(_updatesListeners) {
                _updatesListeners.add(listener)
            }
        }

        override fun removeListener(listener: (GPSUpdate) -> Unit) {
            synchronized(_updatesListeners) {
                _updatesListeners.remove(listener)
            }
        }
    }

    private var client: FusedLocationProviderClient? = null
    private val clientLock = Any()

    private val locationUpdatesListener =
        LocationListener { location ->
            if (_status.value != IGPSUpdatesProvider.Status.ACTIVE) {
                _notifyStatusChanged(IGPSUpdatesProvider.Status.ACTIVE)
            }
            val update = location.toGPSUpdate()
            _notifyUpdate(update)
        }

    private var gpsHandlerThread: HandlerThread? = null

    @RequiresPermission(anyOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun startUpdates() {
        stopUpdates()
        synchronized(clientLock) {
            val actualLooper = looper ?: run {
                gpsHandlerThread = HandlerThread("LocationUpdatesThread").apply {
                    start()
                }
                gpsHandlerThread!!.looper
            }
            // first cancel previous updates
            val locationUpdatesRequest = LocationRequest.Builder(intervalMillis)
                .setDurationMillis(durationMillis)
                .setGranularity(granularity.gmsGranularity)
                .setMaxUpdateAgeMillis(maxUpdateAgeMillis)
                .setMaxUpdateDelayMillis(maxUpdateDelayMillis)
                .setMaxUpdates(maxUpdates)
                .setMinUpdateDistanceMeters(minUpdateDistanceMeters)
                // TODO:
                // .setMinUpdateIntervalMillis(minUpdateIntervalMillis)
                .setPriority(priority.gmsPriority)
                // .setWaitForAccurateLocation(waitForAccurateLocation)
                .build()

            // i want to recreate it in order to do not keep reference to the context
            client = LocationServices.getFusedLocationProviderClient(context).apply {
                requestLocationUpdates(
                    locationUpdatesRequest,
                    locationUpdatesListener,
                    actualLooper
                )
                _notifyStatusChanged(IGPSUpdatesProvider.Status.WARMING_UP)
            }
        }
    }

    override fun stopUpdates() = synchronized(clientLock) {
        gpsHandlerThread?.quitSafely()
        client?.removeLocationUpdates(locationUpdatesListener)
        _notifyStatusChanged(IGPSUpdatesProvider.Status.IDLE)
    }

    private fun restartIfNeeded() {
        if (_status.value != IGPSUpdatesProvider.Status.IDLE) {
            stopUpdates()
            startUpdates()
        }
    }
}