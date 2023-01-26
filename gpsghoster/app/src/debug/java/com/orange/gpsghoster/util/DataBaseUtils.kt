/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.util

import android.content.Context
import android.util.Pair
import android.widget.Toast
import androidx.sqlite.db.SupportSQLiteDatabase
import com.orange.gpsghoster.BuildConfig
import java.io.File
import java.lang.reflect.Method


object DataBaseUtils {

    fun showDebugDBAddressLogToast(context: Context?) {
        if (BuildConfig.DEBUG) {
            try {
                val debugDB = Class.forName("com.amitshekhar.DebugDB")
                val getAddressLog: Method = debugDB.getMethod("getAddressLog")
                val value: Any? = getAddressLog.invoke(null)
                Toast.makeText(context, value as String, Toast.LENGTH_LONG).show()
            } catch (exception: Exception) {
            }
        }
    }

    fun setCustomDatabaseFiles(context: Context, databaseName : String, databasePassword : String) {
        if (BuildConfig.DEBUG) {
            try {
                val debugDB = Class.forName("com.amitshekhar.DebugDB")
                val argTypes = arrayOf<Class<*>>(HashMap::class.java)
                val setCustomDatabaseFiles = debugDB.getMethod("setCustomDatabaseFiles", *argTypes)
                val customDatabaseFiles: HashMap<String, Pair<File, String>> = HashMap()
                // set your custom database files
                customDatabaseFiles[databaseName] = Pair(
                    File(
                        context.getDatabasePath(databaseName).toString()
                    ), databasePassword
                )
                setCustomDatabaseFiles.invoke(null, customDatabaseFiles)
            } catch (exception: Exception) {
            }
        }
    }

    fun setInMemoryRoomDatabases(databaseName : String, vararg database: SupportSQLiteDatabase?) {
        if (BuildConfig.DEBUG) {
            try {
                val debugDB = Class.forName("com.amitshekhar.DebugDB")
                val argTypes = arrayOf<Class<*>>(HashMap::class.java)
                val inMemoryDatabases: HashMap<String, SupportSQLiteDatabase?> = HashMap()
                // set your inMemory databases
                inMemoryDatabases[databaseName] = database[0]
                val setRoomInMemoryDatabase =
                    debugDB.getMethod("setInMemoryRoomDatabases", *argTypes)
                setRoomInMemoryDatabase.invoke(null, inMemoryDatabases)
            } catch (exception: Exception) {
            }
        }
    }
}
