/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint

class Converters {
    @TypeConverter
    fun convertGeoPointToString(geoPoint: GeoPoint?): String? {
        return if (geoPoint != null) Gson().toJson(geoPoint) else null
    }

    @TypeConverter
    fun convertStringToGeoPoint(string : String?) : GeoPoint? {
        return if (string != null) Gson().fromJson(string, GeoPoint::class.java) else null
    }

    @TypeConverter
    fun convertGeoPointListToString(list: List<GeoPoint>?): String? {
        val type = object : TypeToken<List<GeoPoint>>() {}.type
        return if (list != null) Gson().toJson(list, type) else null
    }

    @TypeConverter
    fun convertStringToGeoPointList(string : String?) : List<GeoPoint>? {
        val type = object : TypeToken<List<GeoPoint>>() {}.type
        return if (string != null) Gson().fromJson(string, type) else null
    }

}
