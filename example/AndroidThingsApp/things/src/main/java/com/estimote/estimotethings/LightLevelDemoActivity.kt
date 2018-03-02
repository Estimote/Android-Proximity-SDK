package com.estimote.estimotethings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.estimote.internal_plugins_api.scanning.ScanHandler
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.scanning_plugin.api.EstimoteBluetoothScannerFactory


/**
 * This use case is about checking light level. Put your beacon identifier here as well as desired pin to set to high state.
 * You can also provide light level threshold.
 * Example use case:
 * I put two beacons outside my windows - if the light level outside drops (evening/night),
 * then my NXP board will trigger my window shutters to close (using GPIO pin connected to some chip to control shutters).
 * */
class LightLevelDemoActivity : Activity() {

    private val TAG = LightLevelDemoActivity::class.java.simpleName
    private var scanHandle: ScanHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_light_level_demo)
        val gpioPin = MainActivity.getGpioPinName(intent)
        val beaconId = MainActivity.getBeaconIdentifier(intent)
        val lightLevelThreshold = MainActivity.getLightLevelThreshold(intent)
        RequirementsWizardFactory.createEstimoteRequirementsWizardForAndroidThings().fulfillRequirements(
                this,
                onRequirementsFulfilled = { whenLightLevelIsBelowThresholdThenSetGpioPinToHigh(gpioPin, beaconId, lightLevelThreshold) },
                onRequirementsMissing = { Log.d(TAG, "Unable to start scan. Requirements not fulfilled: ${it.joinToString()}") },
                onError = { Log.d(TAG, "Error while checking requirements: ${it.message}") })
    }

    private fun whenLightLevelIsBelowThresholdThenSetGpioPinToHigh(pinName: String, beaconIdentifier: String, lightLevelThreshold: Double = 10.0) {
        initPin(pinName) { pin ->
            val scanner = EstimoteBluetoothScannerFactory(applicationContext).getSimpleScanner()
            scanHandle = scanner
                    .estimoteTelemetryFrameBScan()
                    .withLowLatencyPowerMode()
                    .withOnPacketFoundAction {
                        if (beaconIdentifier.startsWith(it.shortId, ignoreCase = true)) {
                            Log.d(TAG, "New light level: ${it.ambientLightInLux}")
                            pin.value = it.ambientLightInLux <= lightLevelThreshold
                        }
                    }.start()
        }
    }

    override fun onDestroy() {
        scanHandle?.stop()
        super.onDestroy()
    }
}
