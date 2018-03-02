package com.estimote.estimotethings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.estimote.internal_plugins_api.scanning.BluetoothScanner
import com.estimote.internal_plugins_api.scanning.ScanHandler
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.scanning_plugin.api.EstimoteBluetoothScannerFactory

/**
 * This use case is about moving beacons. Put your beacon identifier here, as well as desired pin to set to high state.
 * Maybe you can create some awesome use-case on top of that?
 * Example use case:
 * I made an app that detects if my handbag is in motion (by putting beacon inside it)
 * If somebody grabs it without my permission, then the speaker starts screaming: "Handbag is in danger!"
 * */
class MotionDemoActivity : Activity() {

    private val TAG = MotionDemoActivity::class.java.simpleName
    private var scanHandle: ScanHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_demo)
        val gpioPin = MainActivity.getGpioPinName(intent)
        val beaconId = MainActivity.getBeaconIdentifier(intent)
        RequirementsWizardFactory.createEstimoteRequirementsWizardForAndroidThings().fulfillRequirements(
                this,
                onRequirementsFulfilled = { whenBeaconIsInMotionThenSetGpioPinToHigh(gpioPin, beaconId) },
                onRequirementsMissing = { Log.d(TAG, "Unable to start scan. Requirements not fulfilled: ${it.joinToString()}") },
                onError = { Log.d(TAG, "Error while checking requirements: ${it.message}") })
    }

    private fun whenBeaconIsInMotionThenSetGpioPinToHigh(pinName: String, beaconIdentifier: String) {
        initPin(pinName) { pin ->
            val scanner = EstimoteBluetoothScannerFactory(applicationContext).getSimpleScanner()
            scanHandle = scanner.estimoteTelemetryFrameAScan()
                    .withLowLatencyPowerMode()
                    .withOnPacketFoundAction {
                        if (beaconIdentifier.startsWith(it.shortId, ignoreCase = true)) {
                            Log.d(TAG, "Beacon is in motion:  ${it.motionState}")
                            pin.value = it.motionState
                        }
                    }.start()
        }
    }

}
