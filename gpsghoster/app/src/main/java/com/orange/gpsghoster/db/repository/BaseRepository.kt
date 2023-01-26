/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.repository

import androidx.annotation.WorkerThread
import com.orange.gpsghoster.db.dao.BaseDao
import kotlinx.coroutines.flow.Flow

abstract class BaseRepository<T>(private val dao: BaseDao<T>) {

    val entities : Flow<List<T>> = dao.getAll()

    @WorkerThread
    suspend fun insert(entity : T) : Long {
        return dao.insert(entity)
    }

    @WorkerThread
    suspend fun insert(entities : List<T>) : List<Long> {
        return dao.insert(entities)
    }

    @WorkerThread
    suspend fun update(entity : T) : Int {
        return dao.update(entity)
    }

    @WorkerThread
    suspend fun update(entities : List<T>) : Int {
        return dao.update(entities)
    }

    @WorkerThread
    suspend fun delete(entity : T) : Int {
        return dao.delete(entity)
    }

    @WorkerThread
    suspend fun delete(entities : List<T>) : Int {
        return dao.delete(entities)
    }

    @WorkerThread
    suspend fun deleteAll() {
        dao.deleteAll()
    }
}
