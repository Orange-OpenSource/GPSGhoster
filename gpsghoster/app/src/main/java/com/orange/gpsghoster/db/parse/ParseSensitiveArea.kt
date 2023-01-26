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

import com.parse.*
import com.parse.ktx.getAs

@ParseClassName("SensitiveArea")
class ParseSensitiveArea : ParseObject() {

    var createdBy : ParseUser?
        get () = getParseUser("createdBy" )
        set (value) {
            put("createdBy" , value!!)
        }

    var bbox : ParseBBox?
        get () = getAs("bbox" )
        set (value) {
            put("bbox" , value!!)
        }

    var polygon : ParsePolygon?
        get () = getParsePolygon("polygon" )
        set (value) {
            put("polygon" , value!!)
        }

    var center : ParseGeoPoint?
        get () = getParseGeoPoint("center" )
        set (value) {
            put("center" , value!!)
        }

}
