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
import com.parse.ktx.getAs

@ParseClassName("FakePosition")
class ParseFakePosition : ParseObject() {

    var route : ParseRoute?
        get () = getAs("route" )
        set (value) {
            put("route" , value!!)
        }

    var point : ParseGeoPoint?
        get () = getParseGeoPoint("point" )
        set (value) {
            put("point" , value!!)
        }

    var provider : String?
        get () = getString("provider" )
        set (value) {
            put("provider" , value!!)
        }

    var distance : Number?
        get () = getNumber("distance" )
        set (value) {
            put("distance" , value!!)
        }
}
