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
import androidx.annotation.NonNull
import com.orange.gpsghoster.App
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.repository.SensitiveAreaRepository
import org.osmdroid.util.GeoPoint
import java.util.*

class MockLocationHelper {

    companion object {
        private const val EARTH_RADIUS = 6371 * 1000 // unité m
    }

    private var mContext : Context
    private var mSensitiveAreaRepository : SensitiveAreaRepository
    private var mPreferenceDataStore : AppPreferenceDataStore
    private val mEpsilon : Double
    private val mDistance : Float

    constructor (@NonNull context: Context) {
        mContext = context
        mSensitiveAreaRepository = (mContext.applicationContext as App).sensitiveAreaRepository
        mPreferenceDataStore = AppPreferenceDataStore.getInstance(mContext)
        mEpsilon = mPreferenceDataStore.getDoubleField(
            R.string.mock_epsilon_key,
            R.string.mock_epsilon_default_value
        )
        mDistance = mPreferenceDataStore.getFloatField(
            R.string.mock_distance_key,
            R.string.mock_distance_default_value
        )
    }

    private var locationOld : Location? = null
    private var locationFakeOld : Location? = null

    suspend fun computeMockLocation(@NonNull location : Location) : Location? {
        val sensitiveAreas = mSensitiveAreaRepository.contains(GeoPoint(location))
        return if (sensitiveAreas.isNotEmpty()) {
            val centerGeoPoint = sensitiveAreas[0].center
            val centerLocation = Location(location)
            centerLocation.latitude = centerGeoPoint.latitude
            centerLocation.longitude = centerGeoPoint.longitude
            locationOld = centerLocation
            centerLocation
        } else {
            if (locationOld != null && locationFakeOld != null && location.distanceTo(locationOld) < mDistance) {
                locationFakeOld
            } else {
                val locationFake = computeGeoI(location)
                locationOld = location
                locationFakeOld = locationFake
                locationFake
            }
        }
    }

    private fun computeGeoI(@NonNull location : Location) : Location {
        val latitude = Math.toRadians(location.latitude) // unité radians
        val longitude = Math.toRadians(location.longitude) // unité radians
        val altitude = location.altitude // unité m

        val cosLatitude : Double = Math.cos(latitude)
        val sinLatitude : Double = Math.sin(latitude)
        val cosLongitude : Double = Math.cos(longitude)
        val sinLongitude : Double = Math.sin(longitude)
        val d = EARTH_RADIUS + altitude

        val rand = Random()
        val randomU : Double = rand.nextDouble()
        val randomV : Double = randomU - 0.5
        val r : Double = ( 1 / mEpsilon ) * Math.signum(randomV) * Math.log(1 - 2 * Math.abs(randomV))
        val t : Double = 2 * Math.PI * rand.nextDouble()

        // transformation M/R[0,u,v,w]=[d,0,0] => [d,r.cos(t),r.sin(t)]
        val u = d
        val v = r * Math.cos(t)
        val w = r * Math.sin(t)

        // changement de base R[0,u,v,w] -> R[0,i,j,k]
        val x = cosLongitude * cosLatitude * u - sinLongitude * v - cosLongitude * sinLatitude * w
        val y = sinLongitude * cosLatitude * u + cosLongitude * v - sinLongitude * sinLatitude * w
        val z = sinLatitude * u + cosLatitude * w

        // conversion coordonnées cartésiennes -> coordonnées GPS
        val newLocation = Location(location)
        newLocation.latitude = Math.toDegrees(Math.asin(z / d))
        newLocation.longitude = Math.toDegrees(Math.atan2(y, x))

        return newLocation
    }

}
