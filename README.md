## Usage

### Add dependency
[![](https://jitpack.io/v/s4ysolutions/GPSAndroidSDK.svg)](https://jitpack.io/#s4ysolutions/GPSAndroidSDK)

### The SDK heavely depends on the kotlin flow library, so you need to add the following dependencies to your project

```gradle
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0"
implementation "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0"
```

### To request GPS updates

Instantiate a `GPSUpdatesManager` object