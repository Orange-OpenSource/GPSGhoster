/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.location

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.orange.gpsghoster.MainActivity
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.parse.ParseRoute
import com.parse.ParseUser

class LocationService : Service() {

    private var TAG: String = LocationService::class.java.simpleName

    private lateinit var mPreferenceDataStore : AppPreferenceDataStore
    private lateinit var mLocationManagerList : ArrayList<LocationManagerFactory>

    protected var mIsMocked : Boolean = false

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG, "onCreate")

        mPreferenceDataStore = AppPreferenceDataStore.getInstance(this)

        mIsMocked = mPreferenceDataStore.getBooleanField(
            R.string.mock_location_key,
            R.string.mock_location_default_value
        )
        if (mIsMocked) {
            val parseRoute = ParseRoute()
            parseRoute.createdBy = ParseUser.getCurrentUser()
            parseRoute?.saveInBackground { e ->
                if (e == null) {
                    buildLocaltionManagers(parseRoute)
                } else {
                    Log.wtf("Error", e.localizedMessage)
                }
            }
        } else {
            buildLocaltionManagers(null)
        }
    }

    private fun buildLocaltionManagers(parseRoute: ParseRoute?) {
        mLocationManagerList = ArrayList()

        if (mPreferenceDataStore.getBooleanField(
                R.string.android_location_key,
                R.string.android_location_default_value
            )
        ) {
            mLocationManagerList.add(AndroidLocationManager(this, parseRoute))
        }

        if (mPreferenceDataStore.getBooleanField(
                R.string.google_location_key,
                R.string.google_location_default_value
            )
        ) {
            mLocationManagerList.add(GoogleLocationManager(this, parseRoute))
        }

        for (index in 0 until mLocationManagerList.size) {
            val locationManager = mLocationManagerList.get(index)
            locationManager.startLocationUpdates()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE)
                }
            val channelId = getString(R.string.channel_id)
            val notification: Notification = NotificationCompat.Builder(this, channelId)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(getText(R.string.notification_title))
                .setContentText(getText(R.string.notification_message))
                .setTicker(getText(R.string.notification_ticker))
                .setContentIntent(pendingIntent)
                .build()
            val id = 1
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(id, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
            } else {
                startForeground(id, notification)
            }
        }

//        return super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.i(TAG, "onDestroy")

        for (index in 0 until mLocationManagerList.size) {
            val locationManager = mLocationManagerList.get(index)
            locationManager.stopLocationUpdates(true)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        // TODO("Not yet implemented")
        return null
    }

//    fun test(location : Location) {
//
//        val sensitiveAreaEntities =
//            (this.applicationContext as App).sensitiveAreaRepository.contains(
//                GeoPoint(location)
//            )
//    }
}
