package com.estimote.estimotethings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.estimote.cloud_plugin.common.EstimoteCloudCredentials
import com.estimote.internal_plugins_api.cloud.CloudCredentials
import com.estimote.internal_plugins_api.cloud.proximity.ProximityAttachment
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.proximity_sdk.proximity.ProximityObserver
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder

class ProximityDemoActivity : Activity() {

    private val TAG = ProximityDemoActivity::class.java.simpleName
    private var proximityObservationHandle: ProximityObserver.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proximity_demo)
        val cloudCredentials = EstimoteCloudCredentials(MainActivity.getAppId(intent), MainActivity.getAppToken(intent))
        val zoneKey = MainActivity.getZoneKey(intent)
        val zoneValue = MainActivity.getZoneValue(intent)
        RequirementsWizardFactory.createEstimoteRequirementsWizardForAndroidThings().fulfillRequirements(
                this,
                onRequirementsFulfilled = {
                    whenBeaconIsCloseThenTriggerAnAction(
                            zoneKey,
                            zoneValue,
                            cloudCredentials,
                            { Log.d(TAG, "Beacon is now close!") },
                            { Log.d(TAG, "Beacon is now far!") })
                },
                onRequirementsMissing = { Log.d(TAG, "Unable to start scan. Requirements not fulfilled: ${it.joinToString()}") },
                onError = { Log.d(TAG, "Error while checking requirements: ${it.message}") })
    }

    private fun whenBeaconIsCloseThenTriggerAnAction(key: String,
                                                     value: String,
                                                     cloudCredentials: CloudCredentials,
                                                     actionToTriggerWhenBeaconComesClose: (ProximityAttachment) -> Unit,
                                                     actionToTriggerWhenBeaconGetsOutOfRange: (ProximityAttachment) -> Unit) {
        val proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
                .withLowLatencyPowerMode()
                .withOnErrorAction { Log.d(TAG, "Proximity observation error: ${it.message}") }
                .withAnalyticsReportingDisabled()
                .build()
        val beaconZone = proximityObserver.zoneBuilder()
                .forAttachmentKeyAndValue(key, value)
                .inNearRange()
                .withOnEnterAction(actionToTriggerWhenBeaconComesClose)
                .withOnExitAction(actionToTriggerWhenBeaconGetsOutOfRange)
                .create()
        proximityObservationHandle = proximityObserver.addProximityZone(beaconZone).start()
    }


}
