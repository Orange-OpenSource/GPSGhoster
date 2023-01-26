/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.model

import androidx.lifecycle.*
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.orange.gpsghoster.db.repository.SensitiveAreaRepository
import kotlinx.coroutines.*
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

class SensitiveAreaViewModel(private val repository : SensitiveAreaRepository) : BaseViewModel<SensitiveAreaEntity>(repository) {

    fun get(position : GeoPoint) : LiveData<List<SensitiveAreaEntity>> {
        val result = MutableLiveData<List<SensitiveAreaEntity>>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.get(position)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun get(boundingBox : BoundingBox) : LiveData<List<SensitiveAreaEntity>> {
        val result = MutableLiveData<List<SensitiveAreaEntity>>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.get(boundingBox)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun contains(position : GeoPoint) : LiveData<List<SensitiveAreaEntity>> {
        val result = MutableLiveData<List<SensitiveAreaEntity>>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.contains(position)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun contains(objectId : String) : LiveData<List<SensitiveAreaEntity>> {
        val result = MutableLiveData<List<SensitiveAreaEntity>>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.contains(objectId)
                result.postValue(resultRepo)
            }
        }
        return result
    }
}

