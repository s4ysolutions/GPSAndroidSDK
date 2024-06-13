package s4y.gps.sdk

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import s4y.gps.sdk.android.NullGPSUpdatesPersistence
import s4y.gps.sdk.filters.dependencies.IGPSFilterProvider
import s4y.gps.sdk.dependencies.IGPSUpdatesProvider
import s4y.gps.sdk.store.dependencies.IGPSUpdatesStore
import s4y.gps.sdk.filters.GPSFilter
import s4y.gps.sdk.filters.kalman.GPSFilterKalmanConstantPositionProvider
import s4y.gps.sdk.store.ArrayGPSUpdatesStore
import java.io.Closeable

@Suppress("unused")
class GPSUpdatesManager(
    private val updatesProvider: IGPSUpdatesProvider,
    private val store: IGPSUpdatesStore = ArrayGPSUpdatesStore(500, NullGPSUpdatesPersistence()),
    private val filterProvider: IGPSFilterProvider = GPSFilterKalmanConstantPositionProvider.instance,
    getUpdatesScope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
) : Closeable {

    constructor(
        updatesProvider: IGPSUpdatesProvider,
        capacity: Int,
    ) : this(updatesProvider, ArrayGPSUpdatesStore(capacity, NullGPSUpdatesPersistence()))

    val all = object : IAll {
        override fun asStateFlow(): StateFlow<Array<GPSUpdate>> = _all
        override val snapshot get() = store.snapshot
    }

    val currentFilter = object : IFilter {
        override fun set(filter: GPSFilter) {
            filterProvider.filter = filter
        }

        override fun asStateFlow() = filterProvider.asStateFlow()
        override fun get() = filterProvider.filter
    }

    val last = object : ILast {
        override fun asSharedFlow(): SharedFlow<GPSUpdate> = _last
        override fun addListener(listener: (GPSUpdate) -> Unit) {
            _lastListeners.add(listener)
        }

        override fun removeListener(listener: (GPSUpdate) -> Unit) {
            _lastListeners.remove(listener)
        }
    }

    val status = object : IStatus {
        override fun asStateFlow(): StateFlow<IGPSUpdatesProvider.Status> = _status
        override fun addListener(listener: (IGPSUpdatesProvider.Status) -> Unit) {
            synchronized(_statusListeners) { _statusListeners.add(listener) }
        }

        override fun removeListener(listener: (IGPSUpdatesProvider.Status) -> Unit) {
            synchronized(_statusListeners) { _statusListeners.remove(listener) }
        }
    }

    var intervalSec: Int
        get() = (updatesProvider.intervalMillis / 1000).toInt()
        set(value) {
            updatesProvider.intervalMillis = value * 1000L
            if (status.isActive || status.isWarmingUp) {
                updatesProvider.stopUpdates()
                updatesProvider.startUpdates()
            }
        }

    override fun close() {
        filterSwitchJob.cancel()
        updateStatusJob.cancel()
        updateLastJob.cancel()
        updateStoreJob.cancel()
    }

    fun resume() = updatesProvider.startUpdates()

    fun saveAsFile() = store.saveAsFile()

    fun start() {
        store.clear()
        _all.value = store.snapshot
        updatesProvider.startUpdates()
    }

    fun stop() = updatesProvider.stopUpdates()

    private val _all = MutableStateFlow(filterProvider.filter.apply(store.snapshot))

    private val _last = MutableSharedFlow<GPSUpdate>(1, 0, BufferOverflow.DROP_LATEST)
    private val _lastListeners = mutableListOf<(GPSUpdate) -> Unit>()

    private val updateLastJob = store
        .lastUpdate
        .mapNotNull {
            filterProvider.filter.apply(it)
        }
        .onEach { update ->
            _last.tryEmit(update)
            if (_lastListeners.isNotEmpty()) {
                val listeners = synchronized(_lastListeners) { _lastListeners.toList() }
                listeners.forEach { it(update) }
            }
        }
        .launchIn(getUpdatesScope)

    private val updateStoreJob: Job = updatesProvider.updates.asSharedFlow()
        .onEach { store.add(it) }
        .launchIn(getUpdatesScope)

    private val _status = MutableStateFlow(updatesProvider.asStateFlow().value)
    private val _statusListeners = mutableListOf<(IGPSUpdatesProvider.Status) -> Unit>()
    private val updateStatusJob: Job = updatesProvider.asStateFlow()
        .onEach { status ->
            _status.tryEmit(status)
            if (_statusListeners.isNotEmpty()) {
                val listeners = synchronized(_statusListeners) { _statusListeners.toList() }
                listeners.forEach { it(status) }
            }
        }
        .launchIn(getUpdatesScope)

    private val filterSwitchJob = filterProvider.asStateFlow()
        .onEach {
            it.reset()
            val updates = store.snapshot
            if (updates.isEmpty()) return@onEach
            _all.value = it.apply(updates)
        }
        .launchIn(getUpdatesScope)

    interface IAll {
        fun asStateFlow(): StateFlow<Array<GPSUpdate>>
        val snapshot: Array<GPSUpdate>
    }

    interface IFilter {
        fun set(filter: GPSFilter)
        fun get(): GPSFilter
        fun asStateFlow(): StateFlow<GPSFilter>
    }

    interface ILast {
        fun asSharedFlow(): SharedFlow<GPSUpdate>
        fun addListener(listener: (GPSUpdate) -> Unit)
        fun removeListener(listener: (GPSUpdate) -> Unit)
    }

    interface IStatus {
        fun asStateFlow(): StateFlow<IGPSUpdatesProvider.Status>

        // kotlin coroutines API
        // java API
        fun addListener(listener: (IGPSUpdatesProvider.Status) -> Unit)
        fun removeListener(listener: (IGPSUpdatesProvider.Status) -> Unit)
        val isIdle: Boolean get() = asStateFlow().value == IGPSUpdatesProvider.Status.IDLE
        val isWarmingUp: Boolean get() = asStateFlow().value == IGPSUpdatesProvider.Status.WARMING_UP
        val isActive: Boolean get() = asStateFlow().value == IGPSUpdatesProvider.Status.ACTIVE
    }
}