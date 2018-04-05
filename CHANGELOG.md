Changelog
=====================
## 0.4.2 (Apr 5, 2018)
- Fixed uncatched exception being thrown when scan error appeared. Now it is being correctly reported via `onErrorAction`.
- Fixed problem with Notification being called many times when using `.withScannerInForegroundService(...)`
- Starting `ProximityObserver` when Bluetooth is disabled now results in `onErrorAction` being called. 

## 0.4.1 (Mar 9, 2018)
- Fixed unhandled crash when `onScanFailed` was being called from BLE stack on some phones. Now it is propagated properly to your `onErrorAction`. 

## 0.4.0 (Mar 8, 2018)
- **Breaking changes**: Changed `EstimoteCloudCredentials` and `ProximityAttachment` packageges to `proximity_sdk.proximity` package. This should allow you to have all our classes in one package, so only one import will be necessary. You should fix your imports after updating to this version. *Disclaimer: We're doing such changes only throughout the beta (0.x.x.) releases. This won't take place in the full release after `1.0.0` anymore - I promise :)*
- Fixed [#32](https://github.com/Estimote/Android-Proximity-SDK/issues/32) where NPE was throw from `TriggerBroadcastReceiver` without even using it. 

## 0.3.3 (Feb 28, 2018)
- Fixed `IllegalArgumentsException: null notification` in foreground service when disabling location permission.
- Added warning when no attachments were found in cloud for defined proximity zones. 

## 0.3.2 (Feb 23, 2018)
- **Breaking changes**: Changed module name `scanning-sdk` to `scanning-plugin`. If you used `BluetoothScanner` class you might need to update your imports. 
- Added support for phones not supporting offloaded hardware filtering. This doesn't fix error code -99 related issues for now.
- Fixed wrong timeout unit (always SECONDS) in launching scans with timeout in `BluetoothScanner`
- Added cache-related improvements to Estmote Secure Monitoring
- `onExit` events are now being reported if beacon signal is completely lost. The delay will by slightly longer than time for typical `onExit` invocation. 

## 0.3.1 (Feb 19, 2018)
- Added missing plugin dependency

## 0.3.0 (Feb 15, 2018)
- Added Estimote Secure Monitoring support. `ProximityObserver` now observes for both encrypted and unencrypted Estimote packets. You can disable this using `.withEstimoteSecureMonitoringDisabled()` when building your `ProximityObserver`.
- Telemetry reporting is now enabled by default. Use `.withTelemetryReportingDisabled()` to disable it. 

## 0.2.3 (Feb 06, 2018)
- Hotfix for `NoSuchMethodError` being thrown when starting observation. 

## 0.2.2 (Feb 05, 2018)
- Fixed [#6](https://github.com/Estimote/Android-Proximity-SDK/issues/6) where `IllegalStateException` was thrown when trying to stop scanning while bluetooth adapter was null/not started. This exception is now being catched and an warning log will be printed - you can handle this case in your `withOnErrorAction` in `ProximityObserver`.

## 0.2.1 (Jan 31, 2018)
- Fixed a bug when attachments with the same key were not resolved.

## 0.2.0 (Jan 17, 2018)
- Attachments are now resolved from dedicated cloud `Beacon Attachment tab`, and no more from JSON strings coded as tags.
- Deprecated `forAttachmentKey("yourKey")` when creating `ProximityZone`. Now you need to use key:value pairs. Using deprecated method will work as calling `forAttachmentKeyAndValue("yourKey", "")`

## 0.1.0-alpha.9 (Jan 16, 2018)
- Hot-fix for `ProximityTrigger.Handler` where calling `stop()` caused crash.

## 0.1.0-alpha.8 (Jan 15. 2018)
- Added `ProximityTrigger` for displaying notification from your app when user enters the proximity zone. It can be accessed using `ProximityTriggerBuilder`.

## 0.1.0-alpha.7 (Jan 10, 2018)

- Added a possibility to access entire beacon's attachment payload as Map using `getPayload()` method in `ProximityAttachment` (e.g. for use in onEnterAction).

- Marks `hasPair()`  in `ProximityAttachment` as deprecated - get entire payload and use Map access methods instead.

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

