package com.estimote.estimotethings

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.estimote.internal_plugins_api.scanning.ScanHandler
import com.estimote.mustard.rx_goodness.rx_requirements_wizard.RequirementsWizardFactory
import com.estimote.scanning_plugin.api.EstimoteBluetoothScannerFactory

/**
 * This use case is about temperature. Put your beacon identifier here, as well as desired pin to set to high state.
 * You can also provide your custom temperature threshold (in Celsius degrees)
 * Example use case:
 * When the temperature outside drops below the given threshold, then my NXP board turns on my floor heater.
 * I like walking on a warm floor during cold winter mornings.
 * */
class TemperatureDemoActivity : Activity() {

    private val TAG = TemperatureDemoActivity::class.java.simpleName
    private var scanHandle: ScanHandler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_demo)
        val gpioPin = MainActivity.getGpioPinName(intent)
        val beaconId = MainActivity.getBeaconIdentifier(intent)
        val temperatureThreshold = MainActivity.getTemperatureThreshold(intent)
        RequirementsWizardFactory.createEstimoteRequirementsWizardForAndroidThings().fulfillRequirements(
                this,
                onRequirementsFulfilled = { whenTemperatureDropsThenSetGpioPinToHigh(gpioPin, beaconId, temperatureThreshold) },
                onRequirementsMissing = { Log.d(TAG, "Unable to start scan. Requirements not fulfilled: ${it.joinToString()}") },
                onError = { Log.d(TAG, "Error while checking requirements: ${it.message}") })
    }

    private fun whenTemperatureDropsThenSetGpioPinToHigh(pinName: String, beaconIdentifier: String, temperatureThreshold: Double = 0.0) {
        initPin(pinName) { pin ->
            val scanner = EstimoteBluetoothScannerFactory(applicationContext).getSimpleScanner()
            scanHandle = scanner.estimoteTelemetryFrameBScan()
                    .withBalancedPowerMode()
                    .withOnPacketFoundAction {
                        if (beaconIdentifier.startsWith(it.shortId, ignoreCase = true)) {
                            Log.d(TAG, "Temperature:  ${it.temperatureInCelsiusDegrees}")
                            pin.value = it.temperatureInCelsiusDegrees < temperatureThreshold
                        }
                    }.start()
        }
    }

    override fun onDestroy() {
        scanHandle?.stop()
        super.onDestroy()
    }

}
