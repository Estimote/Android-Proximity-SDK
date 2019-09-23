# Estimote Proximity SDK for Android 

Stick an Estimote Beacon at your desk, in your car, or on a package, and the Estimote Proximity SDK will let your app know when you enter or exit its range. Works indoors, in the background, and is accurate up to a few meters.

**Powered by Estimote Monitoring:** Estimote’s own signal-processing technology, with emphasis on maximum reliability. (up to 3 times better than other beacon-based technologies we’ve [benchmarked against](http://blog.estimote.com/post/165007958785/launching-the-most-reliable-configurationless#better-reliability).

Other Proximity SDK highlights include:

1. **Tag-based identification:** define your proximity zones with human-readable tags instead of abstract identifiers.
2. **Multiple zones per beacon:** set up more than one enter/exit zone per single beacon. (e.g., a “close” zone and a “far” zone)
3. **Software-defined range:** define the enter/exit trigger range in code, rather than by the beacon’s broadcasting power.
4. **Cloud-based tagging & grouping:** add, remove, and replace beacons, without changing the app’s code - just modify the tags in Estimote Cloud.

# Table of Contents

* [Tag-based identification](#tag-based-identification)
* [Installation](#installation)
* [How to use Proximity SDK in your app](#how-to-use-it-in-your-app)
* [Location permissions](#location-permissions)
* [Background support](#background-support)
* [Additional features](#additional-features)
* [Helpful stuff](#helpful-stuff)
* [Example apps](#example-apps)
* [Your feedback and questions](#your-feedback-and-questions)
* [Changelog](#changelog)

# Tag-based identification

Estimote Proximity SDK uses tag-based identification to allow for dynamic setup changes.
You monitor beacons by tags, which you assign in Estimote Cloud. For example, instead of saying "monitor for beacon 123 and beacon 456", you say, "monitor for beacons tagged as `lobby`". This way, if you need to replace or add more beacons to the lobby, you just add/change tags in Estimote Cloud. Your app will pick up the new setup the next time the ProximityObserver is started.

>As our SDK is still in version `0.x.x`, we're constantly modifying our API according to your feedback. Our latest iteration is based on simple tags, backed up with attachments as an optional additional information. From the version `0.6.0`, the method `.forAttachmentKeyAndValue(...)` is deprecated - please use `.forTag(...)` instead.

Estimote Proximity SDK is built on the top of three key components: _observer_, _zone_, and _zone's context_. 

- _Observer_ - starts and stops monitoring for a provided list of zones 
- _Zone_ - representation of a physical area combining a group of beacons with the same _tag_.
- _Zone’s context (Proximity Context)_ - a combination of a single beacon with its _tag_ and _list of attachments_ assigned to it.
- _Action (callbacks)_ - every _zone_ has three types of callbacks triggered when you: enter a _zone's context_, exit it, or a number of heard _contexts_ changes.

Below there’s a representation of two zones:

- `blueberry` zone with two _Proximity Contexts_,
- `mint` zone with only one _Proximity Context_.

![Proximity zones based on tags](/images/tags.png)

# Installation

## Gradle

Add the below line to your `build.gradle` file, or use our [Example app](#example-apps) to download a ready, pre-integrated demo 

```Gradle
implementation 'com.estimote:proximity-sdk:1.0.4'

// for compatibility with Android 10, also add:
implementation 'com.estimote:scanning-plugin:0.25.4'
```
> If you are using Gradle version below `3.0.0` then you should use `compile` instead of `implementation`.

# How to use it in your app

## Requirements

- One or more [Estimote Proximity Beacons](https://estimote.com/products/) with Estimote Monitoring enabled. Here’s [how to enable it.](https://community.estimote.com/hc/en-us/articles/226144728-How-to-enable-Estimote-Monitoring)
- An Android device with Bluetooth Low Energy support. We suggest using Android 5.0+ (Lollipop or newer). 
- An account in [Estimote Cloud](https://cloud.estimote.com/#/)

## 1. Setting up tags in your Estimote Cloud account
1. Go to https://cloud.estimote.com/#/
2. Click on the beacon you want to configure
3. Click `Edit settings` button
4. Click `Tags` and put your desired tag/tags. 
5. Click Save changes
Tags are Cloud-only settings — no additional connecting to the beacons with the Estimote app is required!

## 2. Build proximity observer
The `ProximityObserver` is the main object for performing proximity observations. Build it using `ProximityObserverBuilder` - and don't forget to put in your Estimote Cloud credentials!

```Kotlin
// Kotlin
val cloudCredentials = EstimoteCloudCredentials(YOUR_APP_ID_HERE , YOUR_APP_TOKEN_HERE)
val proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
                .withBalancedPowerMode()
                .onError { /* handle errors here */ }
                .build()
```

```Java
// Java
EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials(YOUR_APP_ID_HERE, YOUR_APP_TOKEN_HERE);
ProximityObserver proximityObserver = new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                .withBalancedPowerMode()
                .onError(new Function1<Throwable, Unit>() {
                  @Override
                  public Unit invoke(Throwable throwable) {
                    return null;
                  }
                })
                .build();
```
You can customize your `ProximityObserver` using the available options:

- **withLowLatencyPowerMode** - the most reliable mode, but may drain battery a lot. 
- **withBalancedPowerMode** - balance between scan reliability and battery drainage. 
- **withLowPowerMode** - battery efficient mode, but not that reliable.
- **onError** - action triggered when any error occurs - such as cloud connection problems, scanning, etc.
- **withScannerInForegroundService** - starts the observation proces with scanner wrapped in [foreground service](https://developer.android.com/guide/components/services.html). This will display notification in user's notifications bar, but will ensure that the scanning won't be killed by the system. **Important:** Your scanning will be handled without the foreground service by default. 
- **withTelemetryReportingDisabled** - `ProximityObserver` will automatically send telemetry data from your beacons, such as light level, or temperature, to our cloud. This is also an important data for beacon health check (such as tracking battery life for example).
- **withAnalyticsReportingDisabled** - Analytic data (current visitors in your zones, number of enters, etc) ) is sent to our cloud by default. Use this to turn it off. 
- **withEstimoteSecureMonitoringDisabled** - using this will disable scanning for encrypted Estimote packets. `ProximityObserver` will not try to resolve encrypted packets using Estimote Secure Monitoring protocol. Only unencrypted packets will be observed.

## 3. Define proximity zones
Create your own proximity zones using `proximityObserver.zoneBuilder()`

```Kotlin
// Kotlin
val venueZone = ProximityZoneBuilder()
                .forTag("venue")
                .inFarRange()
                .onEnter {/* do something here */}
                .onExit {/* do something here */}
                .onContextChange {/* do something here */}
                .build()
```

```Java
// Java
ProximityZone venueZone = 
    new ProximityZoneBuilder()
        .forTag("venue")
        .inFarRange()
        .onEnter(new Function1<ProximityContext, Unit>() {
          @Override public Unit invoke(ProximityContext proximityContext) {
            /* Do something here */
            return null;
          }
        })
        .onExit(new Function1<ProximityContext, Unit>() {
              @Override
              public Unit invoke(ProximityContext proximityContext) {
                  /* Do something here */
                  return null;
              }
          })
        .onContextChange(new Function1<List<? extends ProximityContext>, Unit>() {
          @Override
          public Unit invoke(List<? extends ProximityContext> proximityContexts) {
            /* Do something here */
            return null;
          }
        })
        .build();
```
You zones can be defined with the below options: 

- **forTag** - a tag that will trigger this zone actions. 
- **onEnter** - the action that will be triggered when the user enters the zone  
- **onExit** - the action that will be triggered when the user exits the zone.
- **onContextChange** - triggers when there is a change in a proximity context of a given tag. If the zone consists of more than one beacon, this will help tracking the ones that are nearby inside the zone, while still remaining one `onEnter` and one `onExit` event for the whole zone in general.  
- **inFarRange** - the far distance at which actions will be invoked. 
- **inNearRange** - the near distance at which actions will be invoked.
- **inCustomRange** - custom desired trigger distance in meters. 

> Notice that due to the nature of Bluetooth Low Energy, the range is "desired" and not "exact". We are constantly improving the precision. 

## 4. Start proximity observation
When you are done defining your zones, you will need to start the observation process:

```Kotlin
// Kotlin
val observationHandler = proximityObserver.startObserving(myZone)
```

```Java
// Java
ProximityObserver.Handler observationHandler =
       proximityObserver
           .startObserving(venueZone);
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
## (Optional) Adding attachments to your beacons

While zone identification is based on tags, **attachments** are a way to add additional content to a beacon and a zone it defines. Think of it as a custom backend where you can assign any additional data to a particular beacon.

1. Go to https://cloud.estimote.com/#/
2. Click on the beacon you want to configure
3. Click `Edit settings` button
4. Click `Beacon attachment` tab and click `add attachment`

When you enter the proximity zone of any beacon with this attachment, you will get a `ProximityContext` as an parameter to your `onEnter` or `onExit` actions. The attachment will be there. Here is an example on how to use it: 

``` Kotlin 
 val exhibitionZone = ProximityZoneBuilder()
                .forTag("exhibit")
                .inNearRange()
                .onEnter { proximityContext ->
                    val title = proximityContext.getAttachments()["title"]
                    val description = proximityContext.getAttachments()["description"]
                    val imageUrl = proximityContext.getAttachments()["image_url"]
                    // Use all above data to update your app's UI
                }
                .create()

```

# Location permissions

In order for ProximitySDK to work, you need to grant your app a location permission. You can ask your user for the permission by yourself, or use our [RequirementsWizard](#checking-requirements-for-bluetooth-scanning-with-requirementswizard) to do it for you. 


# Background support

## Background scanning using foreground service
*Use case: Scanning when your app is in the background (not yet killed). Scanning attached to the notification object even when all activities are destroyed.*

It is now possible to scan when the app is in the background, but it needs to be handled properly according to the Android official guidelines. 

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

3. To keep scanning active while the user is not in your activity (home button pressed) put start/stop in `onCreate()`/`onDestroy()` of your desired **ACTIVITY**. 

4. To scan even after the user has killed your activity (swipe in app manager) put start/stop in `onCreate()`/`onDestroy()` of your **CLASS EXTENDING APPLICATION CLASS**. 

> Tip: You can control the lifecycle of scanning by starting/stopping it in the different places of your app. If you happen to  never stop it, the underlying `foreground service` will keep running, and the notification will be still visible to the user. If you want such behaviour, remember to initialize the `notification` object correctly - add button to it that stops the service. Please, read more in the [official android documentation](https://developer.android.com/training/notify-user/build-notification.html) about managing notification objects. 

## Experimental: background scanning using Proximity Trigger (Android 8.0+) 
*Use case: Displaying your notification when user enters the zone while having your app KILLED - the notification allows him to open your app (if you create it in such way). Triggering your `PendingIntent` when user enters the zone.*

Since Android version 8.0 there is a possibility to display a notification to the user when he enters the specified zone. This may allow him to open your app (by clicking the notification for example) that will start the proper foreground scanning.  
You can do this by using our `ProximityTrigger`, and here is how:

1. Declare an notification object like this: 
``` Kotlin
// KOTLIN
val notification = Notification.Builder(this)
              .setSmallIcon(R.drawable.notification_icon_background)
              .setContentTitle("Beacon scan")
              .setContentText("Scan is running...")
              // you can add here an action to open your app when user clicks the notification
              .setPriority(Notification.PRIORITY_HIGH)
              .build()
``` 
> Tip: Remember that on Android 8.0 you will also need to create a notification channel. [Read more here](https://developer.android.com/training/notify-user/build-notification.html).

2. Use `ProximityTriggerBuilder` to build `ProximityTrigger`:

``` Kotlin 
// KOTLIN
val triggerHandle = ProximityTriggerBuilder(applicationContext)
                .displayNotificationWhenInProximity(notification)
                .build()
                .start()
```
This will register the notification to be invoked when the user enters the zone of your beacons. You can use the `triggerHandle` to call `stop()` - this will deregister the system callback for you. 

Also, bear in mind, that the system callback **may be invoked many times**, thus displaying your notification again and again. In order to avoid this problem, you should add a button to your notification that will call `trigger.stop()` to stop the system scan. On the other hand, you can use `displayOnlyOnce()` method when building the `ProximityTrigger` object - this will fire your notification only once, and then you will need to call `start()` again.

> Known problems: The scan registraton gets cancelled when user disables bluetooth and WiFi on his phone. After that, the trigger may not work, and your app will need to be opened once again to reschedule the `ProximityTrigger`.

> **This feature is still experimental and in development.** 

# Additional features

## Caching data for limited internet connection use cases
Since the version `0.5.0` the `ProximityObserver` will persist necessary data locally, so that when there is no internet access, it may still be able to do proximity observation using that data. The only need is to call `proximityObserver.start()` **at least once** when the internet connection is available - it will fetch all the necessary data from the Estimote Cloud, and will store them locally for the later use.  

## Scanning for Estimote Telemetry
*Use case: Getting sensors data from your Estimote beacons.*

You can easily scan for raw `Estimote Telemetry` packets that contain your beacons' sensor data. All this data is broadcasted in the two separate sub-packets, called `frame A` and `frame B`. Our SDK allows you to scan for both of them separately, or to scan for the whole merged data at once (containing frame A and B data, and also the full device identifier).
Here is how to launch scanning for full telemetry data:

``` Kotlin
// KOTLIN
 bluetoothScanner = EstimoteBluetoothScannerFactory(applicationContext).getSimpleScanner()
        telemetryFullScanHandler =
                bluetoothScanner
                        .estimoteTelemetryFullScan()
                        .withOnPacketFoundAction {
                            Log.d("Full Telemetry", "Got Full Telemetry packet: $it") 
                        }
                        .withOnScanErrorAction { 
                            Log.e("Full Telemetry", "Full Telemetry scan failed: $it") 
                        }
                        .start()
```
You can use `telemetryFullScanHandler.stop()` to stop the scanning. Similarily to the `ProximityObserver` you can also start this scan in the foreground service using `getScannerWithForegroundService(notification)` method instead of `.getSimpleScanner()`.

Basic info about possible scanning modes:

`estimoteTelemetryFullScan()` - contains merged data from frame A and B, as well as full device id. Will be less frequently reported than individual frames.

`estimoteTelemetryFrameAScan()` - data from frame A + short device id. Reported on every new frame A.

`estimoteTelemetryFrameBScan()` - data from frame B + short device id. Reported on every new frame B.

> Tip: Read more about the Estimote Telemetry protocol specification [here](https://github.com/Estimote/estimote-specs/blob/master/estimote-telemetry.js). You can also check [our tutorial](http://developer.estimote.com/sensors/android-things/) about how to use the telemetry scanning on your Android Things device (RaspberryPi 3.0 for example).  

# Helpful stuff 

## Checking requirements for Bluetooth scanning with RequirementsWizard
*Use case: Making sure that everything needed for Bluetooth scanning to work is set up - the user has Bluetooth enabled, location permissions were granted, etc. Displaying default popup dialogs to enable Bluetooth and give needed permissions.*

The `ProximityObserver` won't work without the certain requirements fulfilled. Bluetooth needs to be enabled on a phone, Location permissions need to be granted, etc. You can do this either manually, by checking this before starting the `ProximityObserver`, or use our support library named **Mustard**, which contains handy Kotlin recipes for Android's UI-related stuff. 
The `RequirementsWizard` comes in handy, when you need to check all the necessary requirements. It will automatically display default dialogs for the user to enable needed stuff (like bluetooth) for you. 

1. Add our `Mustard` support library to your module's `build.gradle` file:

```Gradle
implementation 'com.estimote:mustard:0.2.1'
```

2. Use `RequirementsWizard` **before** starting the `ProximityObserver`:

``` Kotlin
// KOTLIN
RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
            YOUR_ACTIVITY_CONTEXT_HERE,
            onRequirementsFulfilled : { /* start the ProximityObserver here! */ },
            onRequirementsMissing: { /* scanning won't work, handle this case in your app */ },
            onError: { /* Oops, some error occurred, handle it here! */ })
```

``` Java
// JAVA
RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
      this, 
      new Function0<Unit>() {
        @Override
        public Unit invoke() {
          proximityObserver.addProximityZone(venueZone).start();
          return null;
        }
      },

      new Function1<List<? extends Requirement>, Unit>() {
        @Override
        public Unit invoke(List<? extends Requirement> requirements) {
          /* scanning won't work, handle this case in your app */
          return null;
        }
      },

      new Function1<Throwable, Unit>() {
        @Override
        public Unit invoke(Throwable throwable) {
          /* Oops, some error occurred, handle it here! */ }
          return null;
        }
      });
```

> Why a separate library? - Mustard library depends on Android support libraries to display proper dialogs for the user. Some of you might don't want to add additional Android support libraries to your project, or some unwanted version confilicts might appear. This is why we decided to keep it as a separate thing. 

> Why "Mustard"? - The name "Kotlin" is coincidentally the same as the popular brand of ketchup in Poland. This is why we named our first support library "Ketchup". It's basically a place for our Kotlin/RX utils shared across our stack. When we decided to create a separate library for UI-related stuff, we thought of how much we love hot-dogs. And you know, hot-dogs come best with both ketchup and mustard :)

## ProGuard configuration
If you want to use `ProGuard` with our SDK, make sure to add additional rules to your `proguard-rules.pro` file. 

``` 
-keepattributes Signature, InternalClasses, Exceptions
-keep class com.estimote.proximity_sdk.internals.proximity.cloud.model.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.Platform$Java8
-dontwarn kotlin.**
```
# Example apps

To get a working prototype, download a [ready-made app template](https://cloud.estimote.com/#/apps/add) in the Estimote Cloud.
App ID & App Token credentials are generated automatically.

- Use [Proximity](https://cloud.estimote.com/#/apps/add/proximity-content-multiple) to run a simple demo in the foreground.
- Use [Notification](https://cloud.estimote.com/#/apps/add/notification) to run a demo in the background and display notifications.

Demos require Estimote Beacons [configured with Estimote Monitoring](https://community.estimote.com/hc/en-us/articles/226144728-How-to-enable-Estimote-Monitoring).

# Documentation
Our Kdoc is available [here](https://estimote.github.io/Android-Proximity-SDK/.).

# Your feedback and questions
At Estimote we're massive believers in feedback! Here are some common ways to share your thoughts with us:
  - Posting issue/question/enhancement on our [issues page](https://github.com/Estimote/Android-Proximity-SDK/issues).
  - Asking our community managers on our [Estimote SDK for Android forum](https://forums.estimote.com/).
  - Keep up with the development progress reports [in this thread on our forums](https://forums.estimote.com/t/changes-to-android-sdk-current-progress/7450). 

# Changelog
To see what has changed in recent versions of our SDK, see the [CHANGELOG](CHANGELOG.md).

