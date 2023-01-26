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

import android.util.Log
import androidx.lifecycle.*
import com.orange.gpsghoster.db.repository.BaseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseViewModel<T>(private val repository : BaseRepository<T>) : ViewModel() {

    val entities: LiveData<List<T>> = repository.entities.asLiveData()

    fun insert(entity: T) : LiveData<Long> {
        Log.d("!!!", "insert : " + entity.toString())
        val result = MutableLiveData<Long>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.insert(entity)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun insert(entities: List<T>) : LiveData<List<Long>> {
        val result = MutableLiveData<List<Long>>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.insert(entities)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun update(entity: T) : LiveData<Int> {
        val result = MutableLiveData<Int>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.update(entity)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun update(entities: List<T>) : LiveData<Int> {
        val result = MutableLiveData<Int>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.update(entities)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun delete(entity: T) : LiveData<Int> {
        Log.d("!!!", "delete : " + entity.toString())
        val result = MutableLiveData<Int>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.delete(entity)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun delete(entities: List<T>) : LiveData<Int> {
        val result = MutableLiveData<Int>()
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val resultRepo = repository.delete(entities)
                result.postValue(resultRepo)
            }
        }
        return result
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}
