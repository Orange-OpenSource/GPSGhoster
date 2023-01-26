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

import com.parse.ParseClassName
import com.parse.ParseGeoPoint
import com.parse.ParseObject
import com.parse.ParsePolygon

@ParseClassName("BBox")
class ParseBBox : ParseObject() {

    var marker : ParseGeoPoint?
        get () = getParseGeoPoint("marker" )
        set (value) {
            put("marker" , value!!)
        }

    var size : Int
        get () = getInt("size" )
        set (value) {
            put("size" , value)
        }

    var bbox : ParsePolygon?
        get () = getParsePolygon("bbox" )
        set (value) {
            put("bbox" , value!!)
        }

    var discreteSize : Int
        get () = getInt("discreteSize" )
        set (value) {
            put("discreteSize" , value)
        }

    var latitudeStep : Double
        get () = getDouble("latitudeStep" )
        set (value) {
            put("latitudeStep" , value)
        }

    var longitudeStep : Double
        get () = getDouble("longitudeStep" )
        set (value) {
            put("longitudeStep" , value)
        }

    var request : String?
        get () = getString("request" )
        set (value) {
            put("request" , value!!)
        }

    var response : String?
        get () = getString("response" )
        set (value) {
            put("response" , value!!)
        }

    var matrix : String?
        get () = getString("matrix" )
        set (value) {
            put("matrix" , value!!)
        }

    var x : Double
        get () = getDouble("x" )
        set (value) {
            put("x" , value)
        }

    var y : Double
        get () = getDouble("y" )
        set (value) {
            put("y" , value)
        }

    var threshold : Int
        get () = getInt("threshold" )
        set (value) {
            put("threshold" , value)
        }

    var r : Int
        get () = getInt("r" )
        set (value) {
            put("r" , value)
        }
}
