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
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.dao.SensitiveAreaDao
import com.orange.gpsghoster.db.entity.SensitiveAreaEntity
import com.orange.gpsghoster.util.DataBaseUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.sqlcipher.database.SQLiteDatabase
import net.sqlcipher.database.SQLiteDatabaseHook
import net.sqlcipher.database.SupportFactory

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
        if (context != null && database_password_key != null) {
            // Ref : https://stackoverflow.com/questions/64229489/android-change-password-of-encrypted-room-db-with-sqlcipher
            query("PRAGMA rekey = '$database_password_key';", emptyArray())
            // ref : https://stackoverflow.com/questions/24047248/contentprovider-destruction-lifecycle#:~:text=Content%20Providers%20don't%20have,a%20Content%20Provider(process).
            DataBaseUtils.reset(context, mDatabaseName, database_password_key)
        }
    }

    companion object {
        private lateinit var mDatabaseName : String
        private lateinit var mDatabasePassword : String

        @Volatile
        private var INSTANCE : AppDatabase? = null

        private const val DEFAULT_MEMORY_SECURITY : Boolean = false

        fun getInstance(
            context: Context,
            coroutineScope: CoroutineScope,
            memorySecurity: Boolean = DEFAULT_MEMORY_SECURITY
        ): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context, coroutineScope, memorySecurity).also { INSTANCE = it}
            }
        }

        private fun buildDatabase(
            context : Context,
            coroutineScope : CoroutineScope,
            memorySecurity : Boolean = DEFAULT_MEMORY_SECURITY
        ) : AppDatabase {
            mDatabaseName = context.packageName +  if (memorySecurity) {
                "_encrypted_with_memory_security.db"
            } else {
                "_encrypted.db"
            }
            val builder = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                mDatabaseName
            )
            val preferenceDataStore =  AppPreferenceDataStore.getInstance(context)
            mDatabasePassword = preferenceDataStore.getString(context.resources.getString(R.string.database_password_key), null).toString()
            val passphrase: ByteArray = SQLiteDatabase.getBytes(mDatabasePassword.toCharArray())
            val factory = SupportFactory(passphrase, object : SQLiteDatabaseHook {
                override fun preKey(database: SQLiteDatabase?) = Unit

                override fun postKey(database: SQLiteDatabase?) {
/*
                    // Patch : decrypt with sqlcipher 3.15.2 on Ubuntu => database can be decrypt but is empty
                    // https://github.com/sqlitebrowser/sqlitebrowser/issues/2162
                    // https://discuss.zetetic.net/t/cannot-decrypt-database-which-is-encrypted-using-sqlcipher-4-0-1/3750
                    database?.rawExecSQL(
                        "PRAGMA cipher_compatibility = 3"
                    )
*/
                    if (memorySecurity) {
                        database?.rawExecSQL(
                            "PRAGMA cipher_memory_security = ON"
                        )
                    } else {
                        database?.rawExecSQL(
                            "PRAGMA cipher_memory_security = OFF"
                        )
                    }
                }
            })
            builder.openHelperFactory(factory)
            builder.addCallback(Callback(context, coroutineScope))
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
                DataBaseUtils.setCustomDatabaseFiles(context, mDatabaseName, mDatabasePassword)
                // DataBaseUtils.setInMemoryRoomDatabases(db_name, database.openHelper.writableDatabase)
            }
        }

        suspend fun populateDatabase(database : AppDatabase) {
            Log.d("!!!", "populateDatabase")
            // TODO : ...
            // val userDao = database.userDao()
        }
    }
}
