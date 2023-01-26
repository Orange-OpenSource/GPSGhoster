/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.ui.osm

import android.util.Log
import com.orange.gpsghoster.R
import com.orange.common.crypto.EncryptedFileUtil
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import org.jetbrains.annotations.NotNull
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import java.util.*
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import kotlin.math.*


class OpenStreetMapDataSource(var mPreferenceDataStore: AppPreferenceDataStore, var mEncryptedFileUtil : EncryptedFileUtil) {

    companion object {
        public const val DEFAULT_SIZE : Int = 1000 // unity m
        public const val DEFAULT_DISCRET_SIZE : Int = 100 // no unity
        private const val DEFAULT_DISCRET_SIZE_MINUS_ONE : Int = 99 // no unity
        private const val EARTH_RADIUS = 6371 * 1000 // unity m
    }

    private var mOverpassApiEndpoint : String // "https://overpass-api.de/api/interpreter"
    private var mBuildingFilter : String // "yes|house|residential|apartments|detached"
    private var mNumberBuildingsThreshold : Int // 10

    init {
        mOverpassApiEndpoint = mPreferenceDataStore.getStringField(
            R.string.osm_endpoint_key,
            R.string.osm_endpoint_default_value)

        mBuildingFilter = mPreferenceDataStore.getStringField(
            R.string.osm_building_filter_key,
            R.string.osm_building_filter_default_value)

        mNumberBuildingsThreshold = mPreferenceDataStore.getIntField(
            R.string.osm_building_threshold_key,
            R.string.osm_building_threshold_default_value)
    }

    private lateinit var mCenter : GeoPoint
    private var mSize : Int = DEFAULT_SIZE // unity m
    private lateinit var mBBox : ArrayList<GeoPoint>
    private var mDiscreteSize : Int = DEFAULT_DISCRET_SIZE // no unity
    private var mLatitudeStep : Double = 0.0 // unity m
    private var mLongitudeStep : Double = 0.0 // unity m
    private lateinit var mURL : String
    private var mMatrix = Array(1) { Array(1, { _ : Int -> 0}) }

    fun buildURLtoFetch(
        @NotNull position : GeoPoint,
        @NotNull size : Int = DEFAULT_SIZE,
        @NotNull discreteSize : Int = DEFAULT_DISCRET_SIZE) : String {

        Log.d("!!!", "buildURLtoFetch : " + position.toString())

        mCenter = position
        mSize = size
        mBBox = computeBBox()
        mDiscreteSize = discreteSize
        mLatitudeStep = (mBBox[1].latitude - mBBox[0].latitude) / mDiscreteSize
        mLongitudeStep = (mBBox[1].longitude - mBBox[0].longitude) / mDiscreteSize
        mURL = String.format(
            mOverpassApiEndpoint + "?data=[out:json];way[\"building\"~\"(%s)\"](%s, %s, %s, %s);out center;",
            mBuildingFilter,
            mBBox[0].latitude.toString(),
            mBBox[0].longitude.toString(),
            mBBox[1].latitude.toString(),
            mBBox[1].longitude.toString()
        )
        return mURL
    }

    private fun computeBBox() : ArrayList<GeoPoint> {
        val aBBox : ArrayList<GeoPoint> = ArrayList<GeoPoint>()

        // center of the bbox in the base R[0,i,j,k]
        val latitude = Math.toRadians(mCenter.latitude) // unity radians
        val longitude = Math.toRadians(mCenter.longitude) // unity radians
        val altitude = mCenter.altitude // unity m

        val cosLatitude : Double = cos(latitude)
        val sinLatitude : Double = sin(latitude)
        val cosLongitude : Double = cos(longitude)
        val sinLongitude : Double = sin(longitude)
        val d = EARTH_RADIUS + altitude

        // lower left corner of the bbox in the base R[0,u,v,w]
        val u1 = d
        val v1 = -mSize / 2
        val w1 = -mSize / 2

        // base change R[0,u,v,w] -> R[0,i,j,k] of lower left corner of the bbox
        val x1 = cosLongitude * cosLatitude * u1 - sinLongitude * v1 - cosLongitude * sinLatitude * w1
        val y1 = sinLongitude * cosLatitude * u1 + cosLongitude * v1 - sinLongitude * sinLatitude * w1
        val z1 = sinLatitude * u1 + cosLatitude * w1

        // coordinate conversion
        val lowerLeftConer = GeoPoint(mCenter)
        lowerLeftConer.latitude = Math.toDegrees(asin(z1 / d)) // unity Degrees
        lowerLeftConer.longitude = Math.toDegrees(atan2(y1, x1)) // unity Degrees

        aBBox.add(lowerLeftConer)

        // upper right corner of the bbox in the base R[0,u,v,w]
        val u2 = d
        val v2 = mSize / 2
        val w2 = mSize / 2

        // base change R[0,u,v,w] -> R[0,i,j,k] of upper right corner of the bbox
        val x2 = cosLongitude * cosLatitude * u2 - sinLongitude * v2 - cosLongitude * sinLatitude * w2
        val y2 = sinLongitude * cosLatitude * u2 + cosLongitude * v2 - sinLongitude * sinLatitude * w2
        val z2 = sinLatitude * u2 + cosLatitude * w2

        // coordinate conversion
        val upperRightCorner = GeoPoint(mCenter)
        upperRightCorner.latitude = Math.toDegrees(asin(z2 / d)) // unity Degrees
        upperRightCorner.longitude = Math.toDegrees(atan2(y2, x2)) // unity Degrees

        aBBox.add(upperRightCorner)

        return aBBox
    }

    fun computeSensitiveArea(@NotNull obj : JSONObject) : SensitiveAreaEntity {
        // mEncryptedFileUtil.encryptFile()
        var sensitiveArea = SensitiveAreaEntity(
            id = 0,
            objectId = "",
            marker = mCenter,
            size = mSize,
            bbox = mBBox,
            discreteSize = mDiscreteSize,
            latitudeStep = mLatitudeStep,
            longitudeStep = mLongitudeStep,
            request = mURL,
            // TODO créer un blob ou créer un fichier et mettre une référence sur ce fichier
            // https://stackoverflow.com/questions/46337519/how-insert-image-in-room-persistence-library
            response = "...", // obj.toString(),
            threshold = mNumberBuildingsThreshold,
        )
        var sensitiveAreaIsFound = false

        // compute matrix
        mMatrix = Array(mDiscreteSize) { Array(mDiscreteSize) { _: Int -> 0 } }
        val elements : JSONArray = obj.getJSONArray("elements")
        for (index in 0..elements.length()-1 step 1) {
            val way : JSONObject = elements.getJSONObject(index)
            val center = way.getJSONObject("center")
            val i : Int = getLatitudeIndex(center.getDouble("lat"))
            val j : Int = getLongitudeIndex(center.getDouble("lon"))
            mMatrix[i][j] = mMatrix[i][j] + 1
        }

// TODO créer un BLOLB
//        val type = object : TypeToken<ArrayList<ArrayList<Int>>>() {}.type
//        sensitiveArea.matrix = Gson().toJson(mMatrix, type).toString()

        // compute sensitiveArea with adaptative method
        val i0 = getLatitudeIndex(mCenter.latitude)
        val j0 = getLongitudeIndex(mCenter.longitude)
        var c = 0
        var s = 0
        val rand = Random()
        val x = rand.nextDouble()
        val y = rand.nextDouble()
        while (c < mNumberBuildingsThreshold && s < (mDiscreteSize / 2) ) {
            s += 1
            val relativeLatitude : Array<Double?> = arrayOfNulls(s)
            val relativeLongitude : Array<Double?> = arrayOfNulls(s)
            for (k in 1..s step 1) {
                relativeLatitude.set(k - 1, abs(
                    (mCenter.latitude - (mBBox[0].latitude + (i0 - (s - k)) * mLatitudeStep)) / (s * mLatitudeStep) - x)
                )
                relativeLongitude.set(k - 1, abs(
                    (mCenter.longitude - (mBBox[0].longitude + (j0 - (s - k)) * mLongitudeStep)) / (s * mLongitudeStep) - y)
                )
            }
            val ki = ( relativeLatitude.indices.minByOrNull { relativeLatitude[it]!! } )?.plus(1)
            val kj = ( relativeLongitude.indices.minByOrNull {  relativeLongitude[it]!! } )?.plus(1)
            val i =  i0 - (s - ki!!)
            val j =  j0 - (s - kj!!)
            c = 0
            for (index1 in i..i+s step 1) {
                for (index2 in j..j+s step 1) {
                    if ((0 < index1 && index1 < mDiscreteSize) &&
                        (0 < index2 && index2 < mDiscreteSize)) {
                        c += mMatrix[index1][index2]
                    }
                }
            }
            if (c >= mNumberBuildingsThreshold) {
                sensitiveArea.x = x
                sensitiveArea.y = y

                val polygon: ArrayList<GeoPoint> = ArrayList()
                polygon.add(buildGeoPoint(i, j))
                polygon.add(buildGeoPoint(i + s, j))
                polygon.add(buildGeoPoint(i + s, j + s))
                polygon.add(buildGeoPoint(i, j + s))
                sensitiveArea.polygon = polygon

                sensitiveArea.center = GeoPoint.fromCenterBetween(sensitiveArea.polygon.get(0), sensitiveArea.polygon.get(2))
                sensitiveAreaIsFound = true
            }
        }

        // if sensitiveArea is NOT found, compute sensitiveArea with fixed method
        if (!sensitiveAreaIsFound) {
            sensitiveArea = computeSensitiveAreaWithFixedMethod(mCenter)
        }

        return sensitiveArea
    }

    fun computeSensitiveAreaWithFixedMethod(
        @NotNull position : GeoPoint,
        @NotNull size : Int = DEFAULT_SIZE,
        @NotNull discreteSize : Int = DEFAULT_DISCRET_SIZE) : SensitiveAreaEntity  {

        Log.d("!!!", "computeSensitiveAreaWithFixedMethod :" + position.toString())

        mCenter = position
        mSize = size
        mBBox = computeBBox()
        mDiscreteSize = discreteSize
        mLatitudeStep = (mBBox[1].latitude - mBBox[0].latitude) / mDiscreteSize
        mLongitudeStep = (mBBox[1].longitude - mBBox[0].longitude) / mDiscreteSize

        val i0 = getLatitudeIndex(mCenter.latitude)
        val j0 = getLongitudeIndex(mCenter.longitude)

        val sensitiveArea = SensitiveAreaEntity(
            id = 0,
            objectId = "",
            marker = mCenter,
            size = mSize,
            bbox = mBBox,
            discreteSize = mDiscreteSize,
            latitudeStep = mLatitudeStep,
            longitudeStep = mLongitudeStep,
        )

        val polygon: ArrayList<GeoPoint> = ArrayList()
        // If there is no enough buildings in the area we increase it
        // uniformly distributed int value between 0 (inclusive) and the specified value (exclusive)
        sensitiveArea.r = Random().nextInt(9)
        when (sensitiveArea.r) {
            0 -> {
                polygon.add(buildGeoPoint(i0 - 1, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 2, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 2, j0 + 2))
                polygon.add(buildGeoPoint(i0 - 1, j0 + 2))
            }
            1 -> {
                polygon.add(buildGeoPoint(i0 - 2, j0))
                polygon.add(buildGeoPoint(i0 + 1, j0))
                polygon.add(buildGeoPoint(i0 + 1, j0 + 3))
                polygon.add(buildGeoPoint(i0 - 2, j0 + 3))
            }
            2 -> {
                polygon.add(buildGeoPoint(i0 - 2, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 1, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 1, j0 + 2))
                polygon.add(buildGeoPoint(i0 - 2, j0 + 2))
            }
            3 -> {
                polygon.add(buildGeoPoint(i0 - 2, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 1, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 1, j0 + 1))
                polygon.add(buildGeoPoint(i0 - 2, j0 + 1))
            }
            4 -> {
                polygon.add(buildGeoPoint(i0 - 1, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 2, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 2, j0 + 1))
                polygon.add(buildGeoPoint(i0 - 1, j0 + 1))
            }
            5 -> {
                polygon.add(buildGeoPoint(i0, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 3, j0 - 2))
                polygon.add(buildGeoPoint(i0 + 3, j0 + 1))
                polygon.add(buildGeoPoint(i0, j0 + 1))
            }
            6 -> {
                polygon.add(buildGeoPoint(i0, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 3, j0 - 1))
                polygon.add(buildGeoPoint(i0 + 3, j0 + 2))
                polygon.add(buildGeoPoint(i0, j0 + 2))
            }
            7 -> {
                polygon.add(buildGeoPoint(i0, j0))
                polygon.add(buildGeoPoint(i0 + 3, j0))
                polygon.add(buildGeoPoint(i0 + 3, j0 + 3))
                polygon.add(buildGeoPoint(i0, j0 + 3))
            }
            else -> {
                polygon.add(buildGeoPoint(i0 - 1, j0))
                polygon.add(buildGeoPoint(i0 + 2, j0))
                polygon.add(buildGeoPoint(i0 + 2, j0 + 3))
                polygon.add(buildGeoPoint(i0 - 1, j0 + 3))
            }
        }

        sensitiveArea.polygon = polygon

        sensitiveArea.center = GeoPoint.fromCenterBetween(sensitiveArea.polygon.get(0), sensitiveArea.polygon.get(2))

        return sensitiveArea
    }

    private fun getLatitudeIndex(@NotNull latitude : Double) : Int {
        return max(0, min(mDiscreteSize - 1, floor((latitude - mBBox[0].latitude) / mLatitudeStep).toInt()))
    }

    private fun getLongitudeIndex(@NotNull longitude : Double) : Int {
        return max(0, min(mDiscreteSize - 1, floor((longitude - mBBox[0].longitude) / mLongitudeStep).toInt()))
    }

    private fun buildGeoPoint(@NotNull @Min(0) @Max(DEFAULT_DISCRET_SIZE_MINUS_ONE.toLong()) i : Int,
                              @NotNull @Min(0) @Max(DEFAULT_DISCRET_SIZE_MINUS_ONE.toLong()) j : Int) : GeoPoint {
        return GeoPoint(
            mBBox[0].latitude + i * mLatitudeStep,
            mBBox[0].longitude + j * mLongitudeStep)
    }

}
