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
import androidx.lifecycle.*
import com.android.volley.RequestQueue
import com.orange.common.crypto.EncryptedFileUtil
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.data.Result
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import kotlinx.coroutines.launch
import org.jetbrains.annotations.NotNull
import org.osmdroid.util.GeoPoint


class OpenStreetMapModel(mPreferenceDataStore: AppPreferenceDataStore, mEncryptedFileUtil : EncryptedFileUtil, mRequestQueue: RequestQueue) : ViewModel() {

    private var mDataSource = OpenStreetMapDataSource(mPreferenceDataStore, mEncryptedFileUtil)
    private var mRepository = OpenStreetMapRepository(mRequestQueue, mDataSource)

    fun computeSensitiveArea(@NotNull position : GeoPoint) : LiveData<SensitiveAreaEntity> {
        val sensitiveAreaEntity = MutableLiveData<SensitiveAreaEntity>()
        viewModelScope.launch {
            with(mRepository.computeSensitiveArea(position)) {
                observeForever(object : Observer<Result<SensitiveAreaEntity>> {
                    override fun onChanged(t: Result<SensitiveAreaEntity>) {
                        if (t is Result.Success) {
                            sensitiveAreaEntity.value = t.data
                        } else {
                            Log.d("!!!", "Error :" + t.toString())
                            sensitiveAreaEntity.value = mDataSource.computeSensitiveAreaWithFixedMethod(position)
                        }
                        removeObserver(this)
                    }
                })
            }

        }
        return sensitiveAreaEntity
    }
}
