package com.estimote.proximityapp

import android.app.Notification
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.estimote.cloud_plugin.common.EstimoteCloudCredentials
import com.estimote.internal_plugins_api.cloud.proximity.ProximityAttachment
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.Requirement
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.proximity_sdk.proximity.ProximityObserver
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder
import com.estimote.proximity_sdk.trigger.ProximityTriggerBuilder
import kotlinx.android.synthetic.main.activity_main.*


/**
 * Proximity SDK example.
 *
 * Requirements:
 * 1. Cloud account with beacon attachment setup.
 * 2. AppId and AppToken of your Estimote Cloud App.
 *
 * @author Estimote Inc. (contact@estimote.com)
 */
class MainActivity : AppCompatActivity() {

    // In order to run this example you need to create an App in your Estimote Cloud account and put here your AppId and AppToken
    // ============
    // THIS WILL NOT COMPILE UNLESS YOU PUT THE PROPER VALUES. SEE ABOVE FOR MORE DETAILS.
    private val cloudCredentials = EstimoteCloudCredentials(YOUR_APP_ID_HERE, YOUR_APP_TOKEN_HERE)
    // ============

    // Actions to trigger
    private val makeMintDeskFilled: (ProximityAttachment) -> Unit = { _ -> mint_image.reveal() }
    private val makeMintDeskWhite: (ProximityAttachment) -> Unit = { mint_image.collapse() }
    private val makeBlueberryDeskFilled: (ProximityAttachment) -> Unit = { _ -> blueberry_image.reveal() }
    private val makeBlueberryDeskWhite: (ProximityAttachment) -> Unit = { blueberry_image.collapse() }
    private val makeVenueFilled: (ProximityAttachment) -> Unit = { _ -> venue_image.reveal() }
    private val makeVenueWhite: (ProximityAttachment) -> Unit = { venue_image.collapse() }
    private val displayToastAboutMissingRequirements: (List<Requirement>) -> Unit = { Toast.makeText(this, "Unable to start proximity observation. Requirements not fulfilled: ${it.size}", Toast.LENGTH_SHORT).show() }
    private val displayToastAboutError: (Throwable) -> Unit = { Toast.makeText(this, "Error while trying to start proximity observation: ${it.message}", Toast.LENGTH_SHORT).show() }

    private lateinit var notification: Notification
    private lateinit var proximityObserver: ProximityObserver
    private var proximityObservationHandler: ProximityObserver.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Take a look at NotificationCreator class which handles different OS versions
        notification = NotificationCreator().createNotification(this)
        mint_image.setColor(R.color.mint_cocktail)
        blueberry_image.setColor(R.color.blueberry_muffin)
        venue_image.setColor(R.color.icy_marshmallow)
        // Proximity observation has some requirements to be fulfilled in order to scan for beacons:
        // Bluetooth needs to be enabled, Location Permissions must be granted by the user,
        // Device must support Bluetooth Low Energy, and so. This is why we created RequirementsWizard.
        // He will check if everything is fine, and if not, he will display adequate dialogs to the user.
        // After all, you will be notified through one of the callbacks.
        // Let the magic happen!
        RequirementsWizardFactory.createEstimoteRequirementsWizard().fulfillRequirements(
                this,
                onRequirementsFulfilled = { startProximityObservation() },
                onRequirementsMissing = displayToastAboutMissingRequirements,
                onError = displayToastAboutError
        )
    }


    private fun startProximityObservation() {
        proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
                // Start scan in foreground service - it takes your notification and will handle scanning in the foreground service,
                // so that the system won't kill your scanning as long as the user is playing with the app.
                // You can also use .startWithSimpleScanner() which won't use any service for scan, and wont need notification object.
                // This will cause your scan to stop when user exits your app (or shortly after).
                // But you can use this method to implement some custom logic - maybe using your own service?
                // Read more about handling background scanning here:
                // https://github.com/Estimote/Android-Proximity-SDK#background-scanning
                .withScannerInForegroundService(notification)
                // Choose scan power mode - you can play with three different - low latency, low power, and balanced.
                // The default mode is balanced. If you have used our old SDK before
                // you might notice that we no longer allow to setup exact scan time periods.
                // This caused many misconceptions and from now on we will handle the proper scan setup for you.
                // This is cool, isn't it? Tell us what you think about it!
                .withBalancedPowerMode()
                // Analytics data (current visitors in your zones, number of enters, etc) )is sent to our cloud by default. Uncomment the line below to turn it off.
//                .withAnalyticsReportingDisabled()
                // Telemetry reporting - enabling this will send telemetry data from your beacons, such as light level, or temperature, to our cloud.
//                .withTelemetryReporting()
                .build()


        // The first zone is for the venue in general.
        // All devices in this venue will have the same key,
        // and the actions will be triggered when entering/exiting the venue.
        val venueZone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("venue", "office")
                .inCustomRange(5.0)
                .withOnEnterAction(makeVenueFilled)
                .withOnExitAction(makeVenueWhite)
                .create()

        // The next zone is defined for a single desk in your venue - let's call it "Mint desk".
        val mintDeskZone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("desk", "mint")
                .inNearRange()
                .withOnEnterAction(makeMintDeskFilled)
                .withOnExitAction(makeMintDeskWhite)
                .create()

        // The last zone is defined for another single desk in your venue - the "Blueberry desk".
        val blueberryDeskZone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue("desk", "blueberry")
                .inNearRange()
                .withOnEnterAction(makeBlueberryDeskFilled)
                .withOnExitAction(makeBlueberryDeskWhite)
                .create()

        // Add zones to ProximityObserver and START observation!
        proximityObservationHandler = proximityObserver
                .addProximityZones(venueZone, mintDeskZone, blueberryDeskZone)
                .start()
    }


    override fun onDestroy() {
        super.onDestroy()
        // After starting your scan, the Proximity Observer will return you a handler to stop the scanning process.
        // We will use it here to stop the scan when activity is destroyed.
        // IMPORTANT:
        // If you don't stop the scan here, the foreground service will remain active EVEN if the user kills your APP.
        // You can use it to retain scanning when app is killed, but you will need to handle actions properly.
        proximityObservationHandler?.stop()
    }


    // ===
    // THE CODE BELOW SHOWS HOW TO SETUP THE PROXIMITY TRIGGER FOR ANDROID 8.0+ DEVICES
    // ===

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_trigger ->
                showTriggerSetupDialog().let { true }
            else ->
                super.onOptionsItemSelected(item)
        }
    }


    private fun showTriggerSetupDialog() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            Toast.makeText(this, "ProximityTrigger works only on devices with Android 8.0+", Toast.LENGTH_SHORT).show()
        } else {
            createTriggerDialog().show()
        }
    }

    private fun createTriggerDialog() =
            AlertDialog.Builder(this)
                    .setTitle("ProximityTrigger setup")
                    .setMessage("The ProximityTrigger will display your notification when the user" +
                            " has entered the proximity of beacons. " +
                            "You can leave your beacons range, enable the trigger, kill your app, " +
                            "and go back - see what happens!")
                    .setPositiveButton("Enable", { _, _ ->
                        val notification = NotificationCreator().createTriggerNotification(this)
                        ProximityTriggerBuilder(this)
                                .displayNotificationWhenInProximity(notification)
                                .build()
                                .start()
                        Toast.makeText(this, "Trigger enabled!", Toast.LENGTH_SHORT).show()
                    })
                    .setNegativeButton("Disable", { _, _ ->
                        val notification = NotificationCreator().createTriggerNotification(this)
                        ProximityTriggerBuilder(this).displayNotificationWhenInProximity(notification)
                                .build()
                                .start().stop()
                        Toast.makeText(this, "Trigger disabled.", Toast.LENGTH_SHORT).show()
                    }).create()


}
