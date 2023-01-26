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
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.location.provider.ProviderProperties
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.RequiresPermission
import com.orange.gpsghoster.R
import com.orange.gpsghoster.db.parse.ParseRoute
import java.lang.Exception

class AndroidLocationManager : LocationManagerFactory {
    private var TAG: String = AndroidLocationManager::class.java.simpleName

    private var mLocationListener : LocationListener
    private var minTimeMs : Long
    private var minDistanceM : Float
    private var mCriteriaIsChecked : Boolean
    private var mCriteria : Criteria
    private var mProviderName : String

    constructor(@NonNull context: Context, @NonNull parseRoute: ParseRoute?) : super(context, parseRoute) {
        mLocationListener = object : LocationListener {
            @RequiresPermission(anyOf = [
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ])
            override fun onLocationChanged(location: Location) {
                Log.i(TAG, "onLocationChanged : " + location.toString())
                onLocationUpdated(location)
            }

            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
            }
        }
        minTimeMs = mPreferenceDataStore.getLongField(
            R.string.android_location_min_time_ms_key,
            R.string.android_location_min_time_ms_default_value
        )
        minDistanceM = mPreferenceDataStore.getFloatField(
            R.string.android_location_min_distance_m_key,
            R.string.android_location_min_distance_m_default_value
        )
        mCriteriaIsChecked = mPreferenceDataStore.getBooleanField(
            R.string.criteria_key,
            R.string.criteria_default_value
        )
        mCriteria = getCriteria()
        mProviderName = mLocationManager.getBestProvider(mCriteria, true).toString()
    }

    private fun getCriteria(): Criteria {
        val criteria = Criteria()
        criteria.accuracy = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_accuracy_key,
            R.string.android_location_accuracy_default_value
        )
        criteria.horizontalAccuracy = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_horizontal_accuracy_key,
            R.string.android_location_horizontal_accuracy_default_value
        )
        criteria.isAltitudeRequired = mPreferenceDataStore.getBooleanField(
            R.string.android_location_is_altitude_required_key,
            R.string.android_location_is_altitude_required_default_value
        )
        criteria.verticalAccuracy = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_vertical_accuracy_key,
            R.string.android_location_vertical_accuracy_default_value
        )
        criteria.isBearingRequired = mPreferenceDataStore.getBooleanField(
            R.string.android_location_is_bearing_required_key,
            R.string.android_location_is_bearing_required_default_value
        )
        criteria.bearingAccuracy = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_bearing_accuracy_key,
            R.string.android_location_bearing_accuracy_default_value
        )
        criteria.isCostAllowed = mPreferenceDataStore.getBooleanField(
            R.string.android_location_is_cost_allowed_key,
            R.string.android_location_is_cost_allowed_default_value
        )
        criteria.powerRequirement = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_power_requirement_key,
            R.string.android_location_power_requirement_default_value
        )
        criteria.isSpeedRequired = mPreferenceDataStore.getBooleanField(
            R.string.android_location_is_speed_required_key,
            R.string.android_location_is_speed_required_default_value
        )
        criteria.speedAccuracy = mPreferenceDataStore.getCriteriaField(
            R.string.android_location_speed_accuracy_key,
            R.string.android_location_speed_accuracy_default_value
        )
        return criteria
    }

    @RequiresPermission(anyOf = [
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.ACCESS_FINE_LOCATION
    ])
    override fun startLocationUpdates() {
        if (!mIsRunning && !mIsForced) {
            removeTestProviders()

            if (mCriteriaIsChecked) {
                mLocationManager.requestLocationUpdates(
                    mProviderName,
                    minTimeMs,
                    minDistanceM,
                    mLocationListener,
                    Looper.getMainLooper()
                )
            } else {
                val providers = mLocationManager.allProviders
                for (provider in providers) {
                    if (mLocationManager.isProviderEnabled(provider) && (
                        provider.equals(LocationManager.NETWORK_PROVIDER) ||
                        provider.equals(LocationManager.GPS_PROVIDER)
                    )) {
                        mLocationManager.requestLocationUpdates(
                            provider,
                            minTimeMs,
                            minDistanceM,
                            mLocationListener,
                            Looper.getMainLooper()
                        )
                    }
                }
            }

            mIsRunning = true
        }
    }

    override fun stopLocationUpdates(isForced: Boolean) {
        mIsForced = isForced
        if (mIsRunning) {
            removeTestProviders()
            mLocationManager.removeUpdates(mLocationListener)
            mIsRunning = false
        }
    }

    override fun setMockLocation(location: Location?) {
        val providers = mLocationManager.allProviders
        for (provider in providers) {
            if (mLocationManager.isProviderEnabled(provider) && (
                provider.equals(LocationManager.NETWORK_PROVIDER) ||
                provider.equals(LocationManager.GPS_PROVIDER)
            ) && (location?.provider == provider)) {
                mLocationManager.addTestProvider(
                    provider,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    true,
                    ProviderProperties.POWER_USAGE_HIGH,
                    ProviderProperties.ACCURACY_FINE
                )
                mLocationManager.setTestProviderEnabled(provider, true)
                mLocationManager.setTestProviderLocation(provider, location)
            }
        }
    }

    private fun removeTestProviders() {
        if (mIsMocked) {
            val providers = mLocationManager.allProviders
            for (provider in providers) {
                if (mLocationManager.isProviderEnabled(provider) && (
                    provider.equals(LocationManager.NETWORK_PROVIDER) ||
                    provider.equals(LocationManager.GPS_PROVIDER)
                )) {
                    try {
                        mLocationManager.removeTestProvider(provider)
                    } catch (exception : Exception) {
                        Log.d(TAG, "removeTestProvider : " + provider + " - " + exception)
                    }
                }
            }
        }
    }
}
