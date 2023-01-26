/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow

interface BaseDao<T> {

    fun getAll() : Flow<List<T>>

    /**
     * Insert an object in the database.
     *
     * @param entity the object to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: T) : Long

    /**
     * Insert an array of objects in the database.
     *
     * @param entities the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entities: List<T>) : List<Long>

    /**
     * Update an object from the database.
     *
     * @param entity the object to be updated
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(entity: T) : Int

    /**
     * Update an array of objects from the database.
     *
     * @param entities the objects to be updated
     */
    @Update(onConflict = OnConflictStrategy.IGNORE)
    suspend fun update(entities: List<T>) : Int

    /**
     * Delete an object from the database
     *
     * @param entity the object to be deleted
     */
    @Delete
    suspend fun delete(entity: T) : Int

    /**
     * Delete an array of objects from the database
     *
     * @param entities the objects to be deleted
     */
    @Delete
    suspend fun delete(entities: List<T>) : Int

    /**
     * Delete all objects from the database
     *
     */
    suspend fun deleteAll()

}
