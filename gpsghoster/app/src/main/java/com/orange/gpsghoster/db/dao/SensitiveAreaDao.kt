/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import kotlinx.coroutines.flow.Flow
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

@Dao
interface SensitiveAreaDao : BaseDao<SensitiveAreaEntity> {

    @Query("SELECT * FROM t_sensitive_area")
    override fun getAll() : Flow<List<SensitiveAreaEntity>>

    @Query("DELETE FROM t_sensitive_area")
    override suspend fun deleteAll()

    @Transaction
    fun get(marker : GeoPoint) : List<SensitiveAreaEntity> {
        return get(marker.latitude, marker.longitude)
    }

    @Query("SELECT * FROM t_sensitive_area where " +
            "json_extract(marker, '$.mLatitude')  == :latitude " +
            "AND json_extract(marker, '$.mLongitude') == :longitude ")
    fun get(latitude: Double, longitude: Double) : List<SensitiveAreaEntity>

    @Transaction
    fun get(boundingBox : BoundingBox) : List<SensitiveAreaEntity> {
        return get(boundingBox.latNorth, boundingBox.latSouth, boundingBox.lonEast, boundingBox.lonWest)
    }

    @Query("SELECT * FROM t_sensitive_area where " +
            "json_extract(marker, '$.mLatitude')  <= :latNorth " +
            "AND json_extract(marker, '$.mLatitude')  >= :latSouth " +
            "AND json_extract(marker, '$.mLongitude') <= :lonEast " +
            "AND json_extract(marker, '$.mLongitude') >= :lonWest ")
    fun get(latNorth: Double, latSouth: Double, lonEast: Double, lonWest: Double) : List<SensitiveAreaEntity>

    @Transaction
    fun contains(position : GeoPoint) : List<SensitiveAreaEntity> {
        return contains(position.latitude, position.longitude)
    }

    @Query("SELECT * FROM t_sensitive_area where " +
            "json_extract(polygon, '$[0].mLatitude')  <= :latitude " +
            "AND json_extract(polygon, '$[3].mLatitude')  >= :latitude " +
            "AND json_extract(polygon, '$[0].mLongitude') <= :longitude " +
            "AND json_extract(polygon, '$[1].mLongitude') >= :longitude")
    fun contains(latitude: Double, longitude: Double) : List<SensitiveAreaEntity>

    @Query("SELECT * FROM t_sensitive_area where objectId  == :objectId ")
    fun contains(objectId : String) : List<SensitiveAreaEntity>

}
