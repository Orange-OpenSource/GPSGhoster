/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.parse

import android.location.Location
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.parse.ParseGeoPoint
import com.parse.ParsePolygon
import com.parse.ParseUser
import org.osmdroid.util.GeoPoint
import java.util.ArrayList

object ParseUtils {

    fun convertGeoPointToParseGeoPoint(geoPoint: GeoPoint?): ParseGeoPoint? {
        return if (geoPoint != null) ParseGeoPoint(geoPoint.latitude, geoPoint.longitude) else null
    }

    fun convertParseGeoPointToGeoPoint(parseGeoPoint: ParseGeoPoint?): GeoPoint? {
        return if (parseGeoPoint != null) GeoPoint(parseGeoPoint.latitude, parseGeoPoint.longitude) else null
    }

    fun convertLocationToParseGeoPoint(location: Location?): ParseGeoPoint? {
        return if (location != null) ParseGeoPoint(location.latitude, location.longitude) else null
    }

    fun convertGeoPointListToParsePolygon(list: List<GeoPoint>?): ParsePolygon? {
        return if (list != null) {
            val points: MutableList<ParseGeoPoint> = ArrayList()
            for (index in 0..list.size - 1) {
                points.add(convertGeoPointToParseGeoPoint(list.get(index))!!)
            }
            // Fix : java.lang.IllegalArgumentException: Polygon must have at least 3 GeoPoints
            if (list.size < 3) {
                points.add(ParseGeoPoint(0.0, 0.0))
            }
            ParsePolygon(points)
        } else null
    }

    fun convertParsePolygonToGeoPointList(polygon: ParsePolygon?): List<GeoPoint>? {
        return if (polygon != null) {
            val list = polygon.getCoordinates()
            val points: MutableList<GeoPoint> = ArrayList()
            // Fix : java.lang.IllegalArgumentException: Polygon must have at least 3 GeoPoints
            val count = if (list.size == 3) 2 else list.size - 1
            for (index in 0..count) {
                points.add(convertParseGeoPointToGeoPoint(list.get(index))!!)
            }
            points
        } else null
    }

    fun convertSensitiveAreaEntityToParseSensitiveArea(sensitiveAreaEntity : SensitiveAreaEntity): ParseSensitiveArea? {
        return if (sensitiveAreaEntity != null) {
            val parseBBox = ParseBBox()
            parseBBox.marker = convertGeoPointToParseGeoPoint(sensitiveAreaEntity.marker)
            parseBBox.size = sensitiveAreaEntity.size
            parseBBox.bbox = convertGeoPointListToParsePolygon(sensitiveAreaEntity.bbox)
            parseBBox.discreteSize = sensitiveAreaEntity.discreteSize
            parseBBox.latitudeStep = sensitiveAreaEntity.latitudeStep
            parseBBox.longitudeStep = sensitiveAreaEntity.longitudeStep
            parseBBox.request = sensitiveAreaEntity.request
            parseBBox.response = sensitiveAreaEntity.response
            parseBBox.matrix = sensitiveAreaEntity.matrix
            parseBBox.x = sensitiveAreaEntity.x
            parseBBox.y = sensitiveAreaEntity.y
            parseBBox.r = sensitiveAreaEntity.r

            val parseSensitiveArea = ParseSensitiveArea()
            parseSensitiveArea.createdBy = ParseUser.getCurrentUser()
            parseSensitiveArea.bbox = parseBBox
            parseSensitiveArea.polygon = convertGeoPointListToParsePolygon(sensitiveAreaEntity.polygon)
            parseSensitiveArea.center = convertGeoPointToParseGeoPoint(sensitiveAreaEntity.center)

            parseSensitiveArea
        } else null
    }

    fun convertParseSensitiveAreaToSensitiveAreaEntity(parseSensitiveArea : ParseSensitiveArea): SensitiveAreaEntity? {
        return if (parseSensitiveArea != null) {
            val bbox : ParseBBox = parseSensitiveArea.bbox!!.fetchIfNeeded()
            val sensitiveAreaEntity = SensitiveAreaEntity(
                id = 0,
                objectId = parseSensitiveArea.objectId,
                marker = convertParseGeoPointToGeoPoint(parseSensitiveArea.bbox!!.marker)!!,
                polygon = convertParsePolygonToGeoPointList(parseSensitiveArea.polygon)!!,
                center = convertParseGeoPointToGeoPoint(parseSensitiveArea.center)!!,
                size = parseSensitiveArea.bbox!!.size,
                bbox = convertParsePolygonToGeoPointList(parseSensitiveArea.bbox!!.bbox)!!,
                discreteSize = parseSensitiveArea.bbox!!.discreteSize,
                latitudeStep = parseSensitiveArea.bbox!!.latitudeStep,
                longitudeStep = parseSensitiveArea.bbox!!.longitudeStep,
                request = parseSensitiveArea.bbox!!.request!!,
                response = parseSensitiveArea.bbox!!.response!!,
                matrix = parseSensitiveArea.bbox!!.matrix!!,
                x = parseSensitiveArea.bbox!!.x,
                y = parseSensitiveArea.bbox!!.y,
                r = parseSensitiveArea.bbox!!.r,
            )
            sensitiveAreaEntity
        } else null
    }
}
