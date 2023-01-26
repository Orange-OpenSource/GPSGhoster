/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.entity

import androidx.annotation.NonNull
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.orange.gpsghoster.ui.osm.OpenStreetMapDataSource
import org.osmdroid.util.GeoPoint

// t_sensitive_area
// id | marker | polygon | center | ...
// 1  | ...
// ...
@Entity(tableName = "t_sensitive_area")
data class SensitiveAreaEntity(
    @NonNull
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id : Long,

    @NonNull
    @ColumnInfo(name = "objectId")
    var objectId: String,

    @NonNull
    @ColumnInfo(name = "marker")
    var marker: GeoPoint,

    @NonNull
    @ColumnInfo(name = "polygon")
    var polygon : List<GeoPoint> = emptyList(),

    @NonNull
    @ColumnInfo(name = "center")
    var center: GeoPoint = GeoPoint(0.0, 0.0),

    // The following data must not be provided to the application

    @ColumnInfo(name = "size")
    var size : Int = OpenStreetMapDataSource.DEFAULT_SIZE,

    @ColumnInfo(name = "bbox")
    var bbox : List<GeoPoint> = emptyList(),

    @ColumnInfo(name = "discreteSize")
    var discreteSize : Int = OpenStreetMapDataSource.DEFAULT_DISCRET_SIZE,

    @ColumnInfo(name = "latitudeStep")
    var latitudeStep : Double = 0.0,

    @ColumnInfo(name = "longitudeStep")
    var longitudeStep : Double = 0.0,

    @ColumnInfo(name = "request")
    var request : String = "",

    @ColumnInfo(name = "response") // TODO creer BLOB
    var response : String = "",

    @ColumnInfo(name = "matrix") // TODO creer BLOB
    var matrix : String = "",

    @ColumnInfo(name = "x")
    var x : Double = 0.0,

    @ColumnInfo(name = "y")
    var y : Double = 0.0,

    @ColumnInfo(name = "threshold")
    var threshold : Int = 0,

    @ColumnInfo(name = "r")
    var r : Int = -1,

)
