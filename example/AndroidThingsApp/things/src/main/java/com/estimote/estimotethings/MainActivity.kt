package com.estimote.estimotethings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle

/**
 * Hello! This activity will let you start one of the demos we prepared for you.
 * The idea is to show some use cases for beacons and Android Things platform.
 *
 * You can choose one of four demos. Please uncomment only one at a time!
 *
 * 1. Proximity demo - will trigger action when the beacon enters/exits the range of your board.
 * By default it will log the state change to logcat, but you can hook your own custom action there.
 *
 * 2. Light sensor demo - will set given GPIO PIN to HIGH when the light level is below given threshold.
 *
 * 3. Motion sensor demo - will set given GPIO PIN to HIGH when given beacon is in motion.
 *
 * 4. Temperature sensor demo - will set the given GPIO PIN to HIGH when a beacon reports temperature below the given level.
 *
 * Read more about GPIO pin setup for each board:
 * RaspberryPi 3 -> https://developer.android.com/things/hardware/raspberrypi-io.html
 * NXP iMX7D -> https://developer.android.com/things/hardware/imx7d-pico-io.html
 *
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Get your app_id and app_token from your Estimote Cloud account ("apps" section).
        // Also your beacon should have tag ZONE_TAG assigned in Estimote Cloud.
        startProximityDemo(YOUR_APP_ID, YOUR_APP_TOKEN, ZONE_TAG)
//        startLightSensorDemo("", "", 20.0)
//        startMotionDemo("", "")
//        startTemperatureDemo("", "", 20.0)
    }

    fun startProximityDemo(appId: String, appToken: String, zoneKey: String) =
            startActivity(createProximityIntent(this, ProximityDemoActivity::class.java, appId, appToken, zoneKey))

    fun startLightSensorDemo(gpioPinName: String, beaconId: String, lightLevelThreshold: Double) =
            startActivity(createSensorsIntent(this, LightLevelDemoActivity::class.java, gpioPinName, beaconId, lightLevelThreshold = lightLevelThreshold))

    fun startMotionDemo(gpioPinName: String, beaconId: String) =
            startActivity(createSensorsIntent(this, MotionDemoActivity::class.java, gpioPinName, beaconId))

    fun startTemperatureDemo(gpioPinName: String, beaconId: String, temperatureThreshold: Double) =
            startActivity(createSensorsIntent(this, TemperatureDemoActivity::class.java, gpioPinName, beaconId, temperatureThreshold = temperatureThreshold))

    companion object {
        private val GPIO_PIN_NAME_KEY = "gpio_pin_name"
        private val BEACON_IDENTIFIER_KEY = "beacon_identifier"
        private val LIGHT_LEVEL_THRESHOLD_KEY = "light_level_threshold"
        private val TEMPERATURE_THRESHOLD_KEY = "temperature_threshold"
        private val APP_ID_KEY = "app_id"
        private val APP_TOKEN_KEY = "app_token"
        private val ZONE_TAG_KEY = "zone_key"

        fun <T> createSensorsIntent(context: Context,
                                    activityClass: Class<T>,
                                    gpioPinName: String,
                                    beaconId: String,
                                    lightLevelThreshold: Double = 0.0,
                                    temperatureThreshold: Double = 20.0): Intent {
            val intent = Intent(context, activityClass)
            intent.putExtra(GPIO_PIN_NAME_KEY, gpioPinName)
            intent.putExtra(BEACON_IDENTIFIER_KEY, beaconId)
            intent.putExtra(LIGHT_LEVEL_THRESHOLD_KEY, lightLevelThreshold)
            intent.putExtra(TEMPERATURE_THRESHOLD_KEY, temperatureThreshold)
            return intent
        }

        fun <T> createProximityIntent(context: Context,
                                      activityClass: Class<T>,
                                      appId: String,
                                      appToken: String,
                                      zoneTag: String): Intent {
            val intent = Intent(context, activityClass)
            intent.putExtra(APP_ID_KEY, appId)
            intent.putExtra(APP_TOKEN_KEY, appToken)
            intent.putExtra(ZONE_TAG_KEY, zoneTag)
            return intent
        }

        fun getGpioPinName(intent: Intent) = intent.extras.getString(GPIO_PIN_NAME_KEY)
        fun getBeaconIdentifier(intent: Intent) = intent.extras.getString(BEACON_IDENTIFIER_KEY)
        fun getLightLevelThreshold(intent: Intent) = intent.extras.getDouble(LIGHT_LEVEL_THRESHOLD_KEY)
        fun getTemperatureThreshold(intent: Intent) = intent.extras.getDouble(TEMPERATURE_THRESHOLD_KEY)
        fun getAppId(intent: Intent) = intent.extras.getString(APP_ID_KEY)
        fun getAppToken(intent: Intent) = intent.extras.getString(APP_TOKEN_KEY)
        fun getZoneTag(intent: Intent) = intent.extras.getString(ZONE_TAG_KEY)
    }

}
