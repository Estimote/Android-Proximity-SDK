# Estimote Proximity SDK for Android 

*Estimote Proximity SDK aims to provide a simple way for apps to react to physical context by reading signals from Estimote Beacons.*

**Main features:**

1. Reliability. It's built upon Estimote Monitoring, Estimote's algorithm for reliable enter/exit reporting.
2. No need to operate on abstract identifiers, or Proximity UUID, Major, Minor triplets. Estimote Proximity SDK lets you define zones by setting predicates for human-readable JSONs.
3. You can define multiple zones for a single beacon, i.e. at different ranges.
4. Cloud-backed grouping. When you change your mind and want to replace one beacon with another, all you need to do is reassign JSON attachments in Estimote Cloud. You don't even need to connect to the beacon!

## Requirements

- One or more [Estimote Proximity or Location Beacon](https://estimote.com/products/) with the `Estimote Location` packet advertising enabled. 
- An Android device with Bluetooth Low Energy support. We suggest using Android 5.0+ (Lollipop or newer). 
- An account in [Estimote Cloud](https://cloud.estimote.com/#/)
## Installation

Add this line to your `build.gradle` file:
```Gradle
compile 'com.estimote:proximity-sdk:0.1.0-alpha.6'
```
Note: this is a pre-release version of Estimote Proximity SDK for Android.

## Attachment-based identification explanation

Details of each of your Estimote devices are available in Estimote Cloud. Each device has a unique identifier, but remembering it and using it for every one of your devices can be challenging. This is why Estimote Proximity SDK uses attachment-based identification.

Each device has an associated JSON. When the SDK detects a change in the proximity to a device, it checks the device's attachment JSON to see which of the registered rules should be applied.

During the pre-release stage of Estimote Proximity SDK, attachment JSONs are encoded in tags. The convention for a tag-encoded attachment is

```
{
    "attachment" : {
        // Attachment JSON goes here.
        // You can put here any JSON you wish to use in your apps.
    }
}
```

## 0. Setting up attachments in your Estimote Cloud account
1. Go to https://cloud.estimote.com/#/
2. Click on the beacon you want to configure
3. Click the Edit settings button
4. Click the Tags field
5. Click the Create New Tag button
6. Paste in the JSON with attachment that's going to represent your beacon
7. Click Save changes
Tags are Cloud-only settings — no additional connecting to the beacons with the Estimote app is required.

![Cloud attachments](/images/adding_attachment_json_tag.png)

## 1. Build proximity observer
The `ProximityObserver` is the main object for performing proximity observations. Build it using `ProximityObserverBuilder` - and don't forget to put in your Estimote Cloud credentials!

```Kotlin
// Kotlin
val cloudCredentials = EstimoteCloudCredentials(YOUR_APP_ID_HERE , YOUR_APP_TOKEN_HERE)
proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
                .withBalancedPowerMode()
                .withOnErrorAction { /* handle errors here */ }
                .build()
```

```Java
// Java
EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials(YOUR_APP_ID_HERE, YOUR_APP_TOKEN_HERE);
ProximityObserver proximityObserver = new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                .withBalancedPowerMode()
                .withOnErrorAction(new Function1<Throwable, Unit>() {
                  @Override
                  public Unit invoke(Throwable throwable) {
                    return null;
                  }
                })
                .build();
```
You can customize your `ProximityObject` using the available options:
- **withLowLatencyPowerMode** - the most reliable mode, but may drain battery a lot. 
- **withBalancedPowerMode** - balance between scan reliability and battery drainage. 
- **withLowPowerMode** - battery efficient mode, but not that reliable.
- **withOnErrorAction** - action triggered when any error occurs - such as cloud connection problems, scanning, etc.
- **withScannerInForegroundService** - starts the observation proces with scanner wrapped in [foreground service](https://developer.android.com/guide/components/services.html). This will display notification in user's notifications bar, but will ensure that the scanning won't be killed by the system. **Important:** Your scanning will be handled without the foreground service by default. 
- **withTelemetryReporting** - Enabling this will send telemetry data from your beacons, such as light level, or temperature, to our cloud. This is also an important data for beacon health check (such as tracking battery life for example). Bear in mind that enabling this will slightly increase battery drain. 
- **withAnalyticsReportingDisabled** - Analytic data (current visitors in your zones, number of enters, etc) ) is sent to our cloud by default. Use this to turn it off.

## 2. Define proximity zones
Now for the fun part - create your own proximity zones using `proximityObserver.zoneBuilder()`

```Kotlin
// Kotlin
val venueZone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("venue", "office")
                .inFarRange()
                .withOnEnterAction{/* do something here */}
                .withOnExitAction{/* do something here */}
                .withOnChangeAction{/* do something here */}
                .create()
```

```Java
// Java
ProximityZone venueZone = 
    proximityObserver.zoneBuilder()
        .forAttachmentKeyAndValue("venue", "office")
        .inFarRange()
        .withOnEnterAction(new Function1<ProximityAttachment, Unit>() {
          @Override public Unit invoke(ProximityAttachment proximityAttachment) {
            /* Do something here */
            return null;
          }
        })
        .withOnExitAction(new Function1<ProximityAttachment, Unit>() {
              @Override
              public Unit invoke(ProximityAttachment proximityAttachment) {
                  /* Do something here */
                  return null;
              }
          })
        .withOnChangeAction(new Function1<List<? extends ProximityAttachment>, Unit>() {
          @Override
          public Unit invoke(List<? extends ProximityAttachment> proximityAttachments) {
            /* Do something here */
            return null;
          }
        })
        .create();
```
You zones can be defined with the below options: 
- **forAttachmentKey** - the key you want to trigger actions for. Value is omitted when checking the predicate (wildcard).
- **forAttachmentKeyAndValue** - the exact key and value that will trigger this zone actions. 
- **onEnterAction** - the action that will be triggered when the user enters the zone  
- **onExitAction** - the action that will be triggered when the user exits the zone.
- **onChangeAction** - triggers when there is a change in proximity attachments of a given key. If the zone consists of more than one beacon, this will help tracking the ones that are nearby inside the zone, while still remaining one `onEnter` and `onExit` event for the whole zone in general.  
- **inFarRange** - the far distance at which actions will be invoked. Notice that due to the nature of Bluetooth Low Energy, it is "desired" and not "exact." We are constantly improving the precision. 
- **inNearRange** - the near distance at which actions will be invoked.
- **inCustomRange** - custom desired trigger distance in meters. 

## 3. Start proximity observation
When you are done defining your zones, you will need to start the observation process:

```Kotlin
// Kotlin
val observationHandler = proximityObserver
               .addProximityZone(venueZone)
               .start()
```

```Java
// Java
ProximityObserver.Handler observationHandler =
       proximityObserver
           .addProximityZone(venueZone)
           .start();
```

The `ProximityObserver` will return `ProximityObserver.Handler` that you can use to stop scanning later. For example:
```Kotlin
// Kotlin
override fun onDestroy() {
    observationHandler.stop()
    super.onDestroy()
}
```

```Java
// Java
@Override
protected void onDestroy() {
    observationHandler.stop();
    super.onDestroy();
}
```
## Background scanning
It is now possible to scan when the app is in the background (or even killed), but it needs to be handled properly according to the Android official guidelines. Please, read the explanation below:

> IMPORTANT: Launching "silent bluetooth scan" without the knowledge of the user is not permitted by the system - if you do so, your service might be killed in any moment, without your permission. We don't want this behaviour, so we decided to only allow scanning in the background using a foreground service with a notification. You can implement your own solution, based on any kind of different service/API, but you must bear in mind, that the system might kill it if you don't handle it properly. 

1. Declare an notification object like this: 
``` Kotlin
// KOTLIN
val notification = Notification.Builder(this)
              .setSmallIcon(R.drawable.notification_icon_background)
              .setContentTitle("Beacon scan")
              .setContentText("Scan is running...")
              .setPriority(Notification.PRIORITY_HIGH)
              .build()
```

2. Use ` .withScannerInForegroundService(notification)` when building `ProximityObserver` via `ProximityObserverBuilder`:

3. To keep scanning active while the user is not in your app (home button pressed) put start/stop in `onCreate()`/`onDestroy()` of your desired **ACTIVITY**. 

4. To scan even after the user has killed your app (swipe in app manager) put start/stop in `onCreate()`/`onDestroy()` of your **CLASS EXTENDING APPLICATION CLASS**. You will also need to handle stopping scan through the notification, because even though the user will destroy the activity, the notification (foregrounbd service) will still remain visible. You can play with it and see the behaviour by yourself. Please, read more in [official android documentation](https://developer.android.com/training/notify-user/build-notification.html) about managing notification objects. 

## Example app

To get a working prototype, check out the [example app](https://github.com/Estimote/Android-Proximity-SDK/tree/master/example/ProximityApp). It's a single screen app with three labels that change the background color when:

- you are in close proximity to the first desk,
- in close proximity to the second desk,
- when you are in the venue in general.

The demo requires at least two Proximity or Location beacons configured for Estimote Monitoring. It's enabled by default in dev kits shipped after mid-September 2017; to enable it on your own check out the [instructions](https://community.estimote.com/hc/en-us/articles/226144728-How-to-enable-Estimote-Monitoring-).

The demo expects beacons having specific tags assigned:

- `{"attachment":{"venue":"office","desk":"mint"}}` for the first one,
- `{"attachment":{"venue":"office","desk":"blueberry"}}` for the second one.


## Documentation
Javadoc documentation available soon...

## Your feedback and questions
At Estimote we're massive believers in feedback! Here are some common ways to share your thoughts with us:
  - Posting issue/question/enhancement on our [issues page](https://github.com/Estimote/Android-Proximity-SDK/issues).
  - Asking our community managers on our [Estimote SDK for Android forum](https://forums.estimote.com/c/android-sdk).
  - Keep up with the development progress reports [in this thread on our forums](https://forums.estimote.com/t/changes-to-android-sdk-current-progress/7450). 

## Changelog
To see what has changed in recent versions of our SDK, see the [CHANGELOG](CHANGELOG.md).

