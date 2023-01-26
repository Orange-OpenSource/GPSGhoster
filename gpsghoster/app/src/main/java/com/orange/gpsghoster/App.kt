/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
 */
package com.orange.gpsghoster

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.orange.common.app.IApp
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.db.AppDatabase
import com.orange.gpsghoster.db.parse.*
import com.orange.gpsghoster.db.repository.SensitiveAreaRepository
import com.parse.Parse
import com.parse.ParseObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class App : Application(), IApp {

    // No need to cancel this scope as it'll be torn down with the process
    private val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getInstance(this, applicationScope) }
    val sensitiveAreaRepository by lazy { SensitiveAreaRepository(database.sensitiveAreaDao()) }

    override fun saveDatabasePassword(database_password_key: String) {
        var preferenceDataStore = AppPreferenceDataStore.getInstance(this)
        if (preferenceDataStore.getString(resources.getString(R.string.database_password_key), null) == null) {
            preferenceDataStore.putString(this.resources.getString(R.string.database_password_key), database_password_key)
        } else {
            database.saveDatabasePassword(this, database_password_key);
            preferenceDataStore.putString(this.resources.getString(R.string.database_password_key), database_password_key)
        }
    }

    override fun deleteUserData() {
        applicationScope.launch {
            database.sensitiveAreaDao().deleteAll()
        }
    }

    override fun onCreate() {
        super.onCreate()
        initParse()
        createNotificationChannel()
    }

    private fun initParse() {
        val configuration = Parse.Configuration.Builder(this)
            .applicationId(getString(R.string.parse_app_id))
            .clientKey(getString(R.string.parse_client_key))
            .server(getString(R.string.parse_server))
            .clientBuilder(CustomTrust(this.applicationContext).getclientBuilder())
            .build()

        ParseObject.registerSubclass(ParseSensitiveArea::class.java)
        ParseObject.registerSubclass(ParseBBox::class.java)
        ParseObject.registerSubclass(ParseRoute::class.java)
        ParseObject.registerSubclass(ParsePosition::class.java)
        ParseObject.registerSubclass(ParseFakePosition::class.java)

        Parse.initialize(configuration)

        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE)
    }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel
            val id = getString(R.string.channel_id)
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(id, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
    }

}
