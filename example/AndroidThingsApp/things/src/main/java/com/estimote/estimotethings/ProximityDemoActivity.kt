package com.estimote.estimotethings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.proximity_sdk.proximity.EstimoteCloudCredentials
import com.estimote.proximity_sdk.proximity.ProximityContext
import com.estimote.proximity_sdk.proximity.ProximityObserver
import com.estimote.proximity_sdk.proximity.ProximityObserverBuilder

class ProximityDemoActivity : Activity() {

    private val TAG = ProximityDemoActivity::class.java.simpleName
    private var proximityObservationHandle: ProximityObserver.Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_proximity_demo)
        val cloudCredentials = EstimoteCloudCredentials(MainActivity.getAppId(intent), MainActivity.getAppToken(intent))
        val zoneKey = MainActivity.getZoneTag(intent)
        RequirementsWizardFactory.createEstimoteRequirementsWizardForAndroidThings().fulfillRequirements(
                this,
                onRequirementsFulfilled = {
                    whenBeaconIsCloseThenTriggerAnAction(
                            zoneKey,
                            cloudCredentials,
                            { Log.d(TAG, "Beacon is now close!") },
                            { Log.d(TAG, "Beacon is now far!") })
                },
                onRequirementsMissing = { Log.d(TAG, "Unable to start scan. Requirements not fulfilled: ${it.joinToString()}") },
                onError = { Log.d(TAG, "Error while checking requirements: ${it.message}") })
    }

    private fun whenBeaconIsCloseThenTriggerAnAction(key: String,
                                                     cloudCredentials: EstimoteCloudCredentials,
                                                     actionToTriggerWhenBeaconComesClose: (ProximityContext) -> Unit,
                                                     actionToTriggerWhenBeaconGetsOutOfRange: (ProximityContext) -> Unit) {
        val proximityObserver = ProximityObserverBuilder(applicationContext, cloudCredentials)
                .withLowLatencyPowerMode()
                .withOnErrorAction { Log.d(TAG, "Proximity observation error: ${it.message}") }
                .withAnalyticsReportingDisabled()
                .build()
        val beaconZone = proximityObserver.zoneBuilder()
                .forTag(key)
                .inNearRange()
                .withOnEnterAction(actionToTriggerWhenBeaconComesClose)
                .withOnExitAction(actionToTriggerWhenBeaconGetsOutOfRange)
                .create()
        proximityObservationHandle = proximityObserver.addProximityZone(beaconZone).start()
    }


}
