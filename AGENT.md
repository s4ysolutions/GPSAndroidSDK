# GPSAndroidSDK — Agent Instructions

## Versions

- This SDK: `3.3.0` (gps-android/build.gradle.kts `publishing.publications.Release.version`)
- AGP: `9.2.1` | Gradle: `9.6.0` | compileSdk: `37` | Kotlin: `2.2.0` (via AGP auto-application)
- Used by: [WayTodaySDK-Android](https://github.com/s4ysolutions/WayTodaySDK-Android) `4.3.0+`

## Compatibility rules

When bumping this SDK's version:
1. Update `version` in `gps-android/build.gradle.kts` publishing block
2. Update `README.md` dependency snippet
3. Update `GPSAndroidSDK` dependency version in WayTodaySDK-Android `waytoday-sdk/build.gradle`
4. Bump WayTodaySDK-Android minor version accordingly

## Build

```bash
./gradlew assembleRelease
```

Kotlin JVM toolchain: Java 21 (requires JDK 21 on build machine).

## Git

Artifacts published via JitPack on tag push. Tag must match publishing version exactly.
No CI — JitPack builds on first artifact request or via jitpack.io UI.
