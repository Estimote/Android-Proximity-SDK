package com.estimote.proximityapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat

/**
 * This class will create proper Notification for given OS version.
 * Notice, that Android Oreo needs to have additional NotificationChannel created.
 * Feel free to use this class as you want.
 *
 * @author Estimote Inc. (contact@estimote.com)
 */
class NotificationCreator {

    private val CHANNEL_ID = "ESTIMOTE_SCAN"
    private val CHANNEL_NAME = "Estimote bluetooth scan notifications"
    private val CHANNEL_DESCRIPTION = "Blah blah blah"

    fun createTriggerNotification(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.beacon_gray)
                .setContentTitle("Proximity App")
                .setContentText("You are in the beacons range! Click here to run our app!")
                .setContentIntent(PendingIntent.getActivity(context, 234235, Intent(context, MainActivity::class.java), PendingIntent.FLAG_UPDATE_CURRENT))
                .build()
    }


    fun createNotification(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) createNotificationChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.beacon_gray)
                .setContentTitle("Estimote Inc. \u00AE")
                .setContentText( "Scan is running...")
                .build()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(context: Context) {
        val id = CHANNEL_ID
        val name = CHANNEL_NAME
        val description = CHANNEL_DESCRIPTION
        val importance = android.app.NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(id, name, importance)
        mChannel.description = description
        val mNotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
        mNotificationManager.createNotificationChannel(mChannel)
    }


}