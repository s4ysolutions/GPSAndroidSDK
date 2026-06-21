# GPSAndroidSDK

Android library providing GPS location updates, Kalman filtering, foreground service, and battery optimization helpers. Used by [WayTodaySDK-Android](https://github.com/s4ysolutions/WayTodaySDK-Android).

## Requirements

- Android minSdk 18 (Android 4.3+)
- Kotlin / Java 8+

## Add Dependency

[![](https://jitpack.io/v/s4ysolutions/GPSAndroidSDK.svg)](https://jitpack.io/#s4ysolutions/GPSAndroidSDK)

> JitPack builds the artifact on first request. To pre-trigger the build for a new version, open
> `https://jitpack.io/#s4ysolutions/GPSAndroidSDK` and click **Get it**.

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.s4ysolutions:GPSAndroidSDK:3.3.0'
}
```

## Quick Start

### GPS updates

```kotlin
// Create provider (uses Google Fused Location)
val provider = FusedGPSUpdatesProvider(context, looper)

// Create manager (500-point buffer, Kalman filter by default)
val manager = GPSUpdatesManager(provider)

// Subscribe via Kotlin Flow
manager.last.asSharedFlow().collect { update ->
    Log.d(TAG, "lat=${update.latitude} lon=${update.longitude}")
}

// Or listener API (Java-friendly)
manager.last.addListener { update -> /* ... */ }

// Start / stop
manager.start()
manager.stop()
```

### Foreground service

```kotlin
// Inject manager before starting
GPSUpdatesForegroundService.updatesManager = manager
GPSUpdatesForegroundService.notificationChannelId = "gps_updates"
GPSUpdatesForegroundService.notificationChannelName = "GPS Updates"
GPSUpdatesForegroundService.notificationId = 1

// Start service (re-start each time manager starts)
GPSUpdatesForegroundService.start(context)

// Stop
GPSUpdatesForegroundService.stop(context)
```

### Battery optimization

```kotlin
val powerManager = GPSPowerManager(context)
if (powerManager.needRequestIgnoreOptimization) {
    powerManager.requestIgnoreOptimization()
}
```

## API

### `GPSUpdatesManager`

| Method / Property | Description |
|---|---|
| `start()` | Clear store, start GPS provider |
| `stop()` | Stop GPS updates |
| `resume()` | Restart provider without clearing store |
| `close()` | Cancel all coroutine jobs |
| `intervalSec` | Get/set update interval in seconds |
| `all` | All buffered updates — `StateFlow<Array<GPSUpdate>>` + snapshot |
| `last` | Latest update — `SharedFlow<GPSUpdate>` + listener API |
| `status` | Provider status — `StateFlow<Status>` + listener API |
| `currentFilter` | Active GPS filter — get/set |

### `GPSUpdate`

| Property | Type | Description |
|---|---|---|
| `latitude` | `Double` | Degrees |
| `longitude` | `Double` | Degrees |
| `velocity` | `Float` | m/s |
| `accuracy` | `Float` | Meters |
| `bearing` | `Double` | Degrees |
| `ts` | `Long` | Unix timestamp ms |
| `isEmpty` | `Boolean` | True if equals `emptyGPSUpdate` |

### `GPSUpdatesForegroundService` (companion statics)

| Method | Description |
|---|---|
| `start(context)` | Launch foreground service |
| `stop(context)` | Stop service |
| `removeFromForeground(context)` | Hide notification, keep running |

### Provider status

`IDLE` → `WARMING_UP` → `ACTIVE` / `ERROR_NO_PERMISSION`

## Architecture

```
GPSUpdatesManager
├── IGPSUpdatesProvider (FusedGPSUpdatesProvider)  ← Google Fused Location API
├── IGPSUpdatesStore (ArrayGPSUpdatesStore)         ← circular buffer
└── IGPSFilterProvider (Kalman filter)              ← noise reduction

GPSUpdatesForegroundService                         ← LifecycleService
GPSPowerManager                                     ← battery optimization dialog
```

## Build

```bash
./gradlew assembleRelease
```

## License

[Apache 2.0](https://www.apache.org/licenses/LICENSE-2.0)
