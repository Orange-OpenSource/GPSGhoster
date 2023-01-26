/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orange.gpsghoster.db.dao.SensitiveAreaDao
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.orange.gpsghoster.util.DataBaseUtils
import io.requery.android.database.sqlite.RequerySQLiteOpenHelperFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


@TypeConverters(Converters::class)
@Database(
    entities = [
        SensitiveAreaEntity::class
    ],
    version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun sensitiveAreaDao() : SensitiveAreaDao

    fun saveDatabasePassword(context: Context, database_password_key : String) {
        // Do nothing because no encryption is required.
    }

    companion object {
        @Volatile
        private var INSTANCE : AppDatabase? = null

        private lateinit var mDatabaseName : String

        fun getInstance(
            context: Context,
            coroutineScope: CoroutineScope,
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, coroutineScope).also { INSTANCE = it }
            }
        }

        fun destroyInstance() {
            if (INSTANCE?.isOpen == true) {
                INSTANCE?.close()
            }
            INSTANCE = null
        }

        private fun buildDatabase(
            context : Context,
            coroutineScope : CoroutineScope,
        ) : AppDatabase {
            mDatabaseName = context.packageName + "_not_encrypted.db"
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                mDatabaseName
            )
            val factory = RequerySQLiteOpenHelperFactory()
            builder.openHelperFactory(factory)
            builder.addCallback(Callback(context, coroutineScope))
            // ref :
            // - https://stackoverflow.com/questions/69249717/it-is-possible-to-stop-creating-db-shm-and-db-wal-files-in-sqlite-database-and
            // - https://androiderrors.com/disabling-sqlite-write-ahead-logging-in-android-pie/
//            builder.setJournalMode(JournalMode.TRUNCATE)
            INSTANCE = builder.build()

            return INSTANCE as AppDatabase
        }
    }

    private class Callback(
        private val context: Context,
        private val coroutineScope : CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                coroutineScope.launch {
                    Log.d("!!!", "populateDatabase")
                    populateDatabase(database)
                }
            }
        }

        override fun onOpen(db: SupportSQLiteDatabase) {
            super.onOpen(db)
            coroutineScope.launch {
                Log.d("!!!", "DataBaseUtils config")
                // DataBaseUtils.showDebugDBAddressLogToast(context)
                DataBaseUtils.setCustomDatabaseFiles(context, mDatabaseName, "")
                // DataBaseUtils.setInMemoryRoomDatabases(db_name, database.openHelper.writableDatabase)
            }
        }

        suspend fun populateDatabase(database : AppDatabase) {
            // TODO : ...
            // val userDao = database.userDao()
//            val query = ParseQuery.getQuery<ParseObject>("SensitiveArea")
//            query.whereEqualTo("user", ParseUser.getCurrentUser().get("objectId"))
//            query.findInBackground { scoreList, e ->
//                if (e == null) {
//                    Log.d("score", "Retrieved " + scoreList.size + " scores")
//                } else {
//                    Log.d("score", "Error: " + e.message)
//                }
//            }

        }
    }

    fun getName() : String {
        return mDatabaseName
    }
}

