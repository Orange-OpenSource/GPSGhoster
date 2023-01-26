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

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.*
import com.orange.gpsghoster.R
import com.orange.gpsghoster.db.parse.ParseRoute
import org.jetbrains.annotations.NotNull

class GoogleLocationManager : LocationManagerFactory {
    private var TAG: String = GoogleLocationManager::class.java.simpleName

    private var mInterval: Long
    private var mFastestInterval: Long
    private var mPriority: Int

    private var mFusedLocationProviderClient: FusedLocationProviderClient
    private var mLocationRequest: LocationRequest
    private var mLocationCallback: LocationCallback

    constructor(@NonNull context: Context, @NonNull parseRoute: ParseRoute?) : super(context, parseRoute) {
        mInterval = mPreferenceDataStore.getLongField(
            R.string.google_location_interval_key,
            R.string.google_location_interval_default_value
        )
        mFastestInterval = mPreferenceDataStore.getLongField(
            R.string.google_location_fastest_interval_key,
            R.string.google_location_fastest_interval_default_value
        )
        mPriority = mPreferenceDataStore.getPriorityField(
            R.string.google_location_priority_key,
            R.string.google_location_priority_default_value
        )

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        mLocationRequest = LocationRequest.create().apply {
            interval = mInterval
            fastestInterval = mFastestInterval
            priority = mPriority
        }
        mLocationCallback = object : LocationCallback() {
            @RequiresPermission(
                anyOf = [
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ]
            )
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                Log.i(TAG, "onLocationResult : " + location!!.toString())
                onLocationUpdated(location)
            }
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    override fun startLocationUpdates() {
        if (!mIsRunning && !mIsForced) {
            mFusedLocationProviderClient.setMockMode(false)
            mFusedLocationProviderClient.requestLocationUpdates(
                mLocationRequest,
                mLocationCallback,
                Looper.getMainLooper()
            )
            mIsRunning = true
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    override fun stopLocationUpdates(isForced: Boolean) {
        mIsForced = isForced
        if (mIsRunning) {
            mFusedLocationProviderClient.setMockMode(false)
            mFusedLocationProviderClient.removeLocationUpdates(
                mLocationCallback
            )
            mIsRunning = false
        }
    }

    @RequiresPermission(
        anyOf = [
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
        ]
    )
    override fun setMockLocation(@NotNull location: Location?) {
        mFusedLocationProviderClient.setMockMode(true)
        mFusedLocationProviderClient.setMockLocation(location!!)
    }

}
