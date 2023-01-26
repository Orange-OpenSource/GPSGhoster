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

import androidx.lifecycle.MutableLiveData
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.orange.gpsghoster.data.Result
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.annotations.NotNull
import org.osmdroid.util.GeoPoint

class OpenStreetMapRepository(var mRequestQueue: RequestQueue, val dataSource : OpenStreetMapDataSource) {

    suspend fun computeSensitiveArea(@NotNull position : GeoPoint) : MutableLiveData<Result<SensitiveAreaEntity>> {
        var result = MutableLiveData<Result<SensitiveAreaEntity>>()

        return withContext(Dispatchers.IO) {
            val jsonObjectRequest = JsonObjectRequest(
                Request.Method.GET,
                dataSource.buildURLtoFetch(position),
                null,
                { response ->
                    result.value = Result.Success(dataSource.computeSensitiveArea(response))
                },
                { error ->
                    result.value = Result.Error(error)
                }
            )
            // Fix : https://stackoverflow.com/questions/22428343/android-volley-double-post-when-have-slow-request
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                10 * DefaultRetryPolicy.DEFAULT_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)

            // Set the tag on the request.
            jsonObjectRequest.tag = "osm"
            // Access the RequestQueue through your singleton class.]
            mRequestQueue.add(jsonObjectRequest)
            //we use the request queue specified in the class contructor.

            return@withContext result
        }
    }
}
