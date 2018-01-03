package com.estimote.estimotethings

import android.util.Log
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.PeripheralManagerService
import java.io.IOException

/**
 * This is a simple inline function that wraps the given lambda into Pin initialisation.
 * Thanks to this we have less duplicated code in use cases above. Read more about inlined functions here:
 * https://kotlinlang.org/docs/reference/inline-functions.html
 * @author Estimote Inc. (contact@estimote.com)
 */
inline fun initPin(pinName: String, actionToWrap: (Gpio) -> Unit) {
    try {
        with(PeripheralManagerService().openGpio(pinName)) {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
            value = false
            actionToWrap(this)
        }
    } catch (e: IOException) {
        Log.e("ESTIMOTE", "Error on PeripheralIO API", e)
    }
}