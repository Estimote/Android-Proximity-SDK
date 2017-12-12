Changelog
=====================
## 0.1.0-alpha.6 (Dec 12, 2017)

- `ProximityObserverFactory` is now `ProximityObserverBuilder`
- Choosing whether to use simple scanner, or scanner wrapped in foreground service is now done in `ProximityObserverBuilder` using `.withScannerInForegroundService(notification)`
``` Kotlin
proximityObserver = ProximityObserverBuilder(applicationContext, credentials)
            .withScannerInForegroundService(notification)
            ...
            .build()
``` 
- Starting `ProximityObserver` is now done using `.start()`:
``` Kotlin
observationHandler = proximityObserver
          .addProximityZones(venueZone, beetrootDeskZone, lemonDeskZone)
          .start()
```
- Added support for uploading telemetry data to Estimote cloud from beacons nearby. You can turn it on while building `ProximityObserver` object: 
``` Kotlin
proximityObserver = ProximityObserverBuilder(applicationContext, credentials)
            .withTelemetryReporting()
            ...
            .build()
``` 
Note: You can see the telemetry data in your Estimote Cloud account for each individual beacon. There should be a chart  with data such as light level, or temperature. Remember, that enabling telemetry data uploading will make device to use slightly more battery due to an additional scan for `Estimote Telemetry` packets. 

- Added a possibility to disable Analytics data cloud reporting (It is turned on by default):
``` Kotlin
proximityObserver = ProximityObserverBuilder(applicationContext, credentials)
            .withAnalyticsReportingDisabled()
            ...
            .build()
``` 
- Fixed [#4](https://github.com/Estimote/Android-Proximity-SDK/issues/4) when disabled bluetooth were causing NPE.
- Added helper class to handle checking for requirements needed to scan for beacons. The class is `RequirementsWizard`, and it is located in our helper library. Add this line to your `build.gradle` file:

```Gradle
compile 'com.estimote:mustard:0.1.0'
```
Use it like this:
```Kotlin 
RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
              applicationContext,
              onRequirementsFulfilled = { /* Start proximity observation here */ },
              onRequirementsMissing =  { /* Handle missing requirements here */ },
              onError = { /* Handle errors here */ }
      )
```
>Why it is in a separate library? Because it uses Android UI support libraries to display default dialogs for user to enable bluetooth, etc. And we don't want to force you to use them in your app. If you need a custom behavior, feel free to implement your own requirements checker.
 
- Added support for Android Oreo 8.1 (API 27)


## 0.1.0-alpha.5 (Nov 22, 2017)
- Added Analytics to the `ProximityObserver`. 
  1. Your Enter/Exit events are now reported for each beacon.
  2. You can check analytics data in Estimote 
- Minor improvements to the scanning mechanism 

## 0.1.0-alpha.4 (Oct 28, 2017)
- Introduced improvements to API. Some breaking changes appeared:
  1. `ProximityObserver.RuleBuilder` is now `ProximityObserver.ZoneBuilder`
  2. `ProximityObserver` has now methods: `addProximityZone` instead of `addProximityRule`
  3. `ProximityObserver.ZoneBuilder` has fluent builder now. 
  4. `onExitAction` now returns `ProximityAttachment` that was last visible. 
  5. `ProximityObserver.Handler` is now an interface. 
  6. `proximityRuleBuilder.withDesiredMeanTriggerDistance()` is now split into three methods:
    - `inNearRange`
    - `inFarRange`
    - `inCustomRange(double)` with parameter in meters. 
  7. Errors are now logged to Android logcat by default. 
- Fixed the compilation error `unknown element: <uses-permission>`. There was a problem with wrong manifest merging.
- Fixed problem when using `forAttachmentKey` was not triggering any actions - now it works properly.

## 0.1.0-alpha.3 (Oct 20, 2017)
- Added key-value tag support to `ProximityObserver`

