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

import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.util.Log
import androidx.annotation.NonNull
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.parse.ParseFakePosition
import com.orange.gpsghoster.db.parse.ParsePosition
import com.orange.gpsghoster.db.parse.ParseRoute
import com.orange.gpsghoster.db.parse.ParseUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NotNull

abstract class LocationManagerFactory {

    private var TAG: String = LocationManagerFactory::class.java.simpleName

    protected var mContext : Context
    protected var mPreferenceDataStore : AppPreferenceDataStore
    protected var mLocationManager : LocationManager
    protected var mIsRunning : Boolean = false
    protected var mIsForced : Boolean = false
    protected var mIsMocked : Boolean = false
    protected var mMockLocationHelper : MockLocationHelper
    protected lateinit var mParseRoute : ParseRoute

    constructor (@NonNull context: Context, parseRoute: ParseRoute?) {
        mContext = context
        if (parseRoute != null) {
            mParseRoute = parseRoute
        }
        mPreferenceDataStore = AppPreferenceDataStore.getInstance(mContext)
        mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = mLocationManager.allProviders
        for (provider in providers) {
            Log.i(TAG,"provider " + provider + " is enabled : " + mLocationManager.isProviderEnabled(provider))
        }
        mIsMocked = mPreferenceDataStore.getBooleanField(
            R.string.mock_location_key,
            R.string.mock_location_default_value
        )
        mMockLocationHelper = MockLocationHelper(context)
    }

    abstract fun startLocationUpdates()

    abstract fun stopLocationUpdates(isForced : Boolean = false)

    @Synchronized protected fun onLocationUpdated(location: Location) {
        // ref
        // - https://www.raywenderlich.com/20123726-android-services-getting-started ???
        // - https://kt.academy/article/cc-constructing-scope
        val scope = CoroutineScope(Dispatchers.IO + Job())
        scope.launch {

            if (mIsMocked) {
                stopLocationUpdates()
                var mockLocation = mMockLocationHelper.computeMockLocation(location)!!
                setMockLocation(mockLocation)
                val parsePosition = ParsePosition()
                parsePosition.route = mParseRoute
                parsePosition.point = ParseUtils.convertLocationToParseGeoPoint(location)
                parsePosition.provider = location.provider
                parsePosition.fakePosition = ParseFakePosition()
                parsePosition.fakePosition!!.route = mParseRoute
                parsePosition.fakePosition!!.point = ParseUtils.convertLocationToParseGeoPoint(mockLocation)
                parsePosition.fakePosition!!.provider = mockLocation.provider
                parsePosition.fakePosition!!.distance = location.distanceTo(mockLocation)
                parsePosition?.saveInBackground { e ->
                    if (e == null) {
                        Log.i(TAG,
                            "setMockLocation : " + mockLocation.provider + " - " + location.toString() + " -> " + mockLocation.toString() + " - " + location.distanceTo(mockLocation)
                        )
                    } else {
                        Log.wtf( TAG, "Error : " + e.localizedMessage)
                    }
                    startLocationUpdates()
                }
            }
        }
    }

    protected abstract fun setMockLocation(@NotNull location: Location?)
}
