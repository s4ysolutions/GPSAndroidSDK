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
        override val status: IGPSUpdatesProvider.IStatus = updatesProvider.status
        override fun asSharedFlow(): SharedFlow<GPSUpdate> = _last
    }

    var intervalSec: Int
        get() = (updatesProvider.intervalMillis / 1000).toInt()
        set(value) {
            updatesProvider.intervalMillis = value * 1000L
            if (updatesProvider.status.isActive || updatesProvider.status.isWarmingUp) {
                updatesProvider.stopUpdates()
                updatesProvider.startUpdates()
            }
        }

    val status = updatesProvider.status

    override fun close() {
        updateStoreJob.cancel()
        updateLastJob.cancel()
        filterSwitchJob.cancel()
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

    private val updateLastJob = store
        .lastUpdate
        .mapNotNull {
            filterProvider.filter.apply(it)
        }
        .onEach { _last.tryEmit(it) }
        .launchIn(getUpdatesScope)

    private val updateStoreJob: Job = updatesProvider.updates.asSharedFlow()
        .onEach { store.add(it) }
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
        val status: IGPSUpdatesProvider.IStatus
        fun asSharedFlow(): SharedFlow<GPSUpdate>
    }
}