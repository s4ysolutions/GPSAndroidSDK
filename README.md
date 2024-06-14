## Usage

### Add dependency
[![](https://jitpack.io/v/s4ysolutions/GPSAndroidSDK.svg)](https://jitpack.io/#s4ysolutions/GPSAndroidSDK)


### To request GPS updates

```java
        // use FusedGPSUpdatesProvider for location updates
        IGPSUpdatesProvider gpsUpdatesProvider = new FusedGPSUpdatesProvider(context, looper);
        // create GPSUpdatesManager with 500 points capacity
        GPSUpdatesManager gpsUpdatesManager = new GPSUpdatesManager(gpsUpdatesProvider, 500);
        // subscribe to filtered GPS updates        
        gpsUpdatesManager.getLast().addListener(gpsUpdate -> {...});
        // start GPS updates        
        gpsUpdatesManager.start();
```

### To init and start GPS background service

```java
        // this makes service to stop itself if updates has been stopped
        GPSUpdatesForegroundService.setUpdatesManager(gpsUpdatesManager);
        // Optional notification settings
        GPSUpdatesForegroundService.setNotificationChannelId("gps_updates");
        GPSUpdatesForegroundService.setNotificationChannelName("GPS Updates");
        GPSUpdatesForegroundService.setNotificationId(23);
        GPSUpdatesForegroundService.setNotificationContentTitle("Stop tracking");
        // start service
        // due the service is terminating on gpsUpdatesManager stop
        // it should be started manualy every time gpsUpdatesManager starts
        GPSUpdatesForegroundService.start(context);
```