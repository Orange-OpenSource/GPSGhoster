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
import com.orange.gpsghoster.db.dao.SensitiveAreaDao
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

class SensitiveAreaRepository(private val dao: SensitiveAreaDao) : BaseRepository<SensitiveAreaEntity>(dao) { // {

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun get(position: GeoPoint) : List<SensitiveAreaEntity> {
        return dao.get(position)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun get(boundingBox : BoundingBox) : List<SensitiveAreaEntity> {
        return dao.get(boundingBox)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun contains(position: GeoPoint) : List<SensitiveAreaEntity> {
        return dao.contains(position)
    }

    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun contains(objectId: String) : List<SensitiveAreaEntity> {
        return dao.contains(objectId)
    }

}
