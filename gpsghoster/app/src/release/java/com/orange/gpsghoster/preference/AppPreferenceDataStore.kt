/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.preference

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.orange.common.preference.AbstractPreferenceDataStore
import org.jetbrains.annotations.NotNull

class AppPreferenceDataStore private constructor(@NotNull context: Context) : AbstractPreferenceDataStore() {

    companion object {
        @Volatile private var INSTANCE: AppPreferenceDataStore? = null

        fun getInstance(context: Context): AppPreferenceDataStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferenceDataStore(context).also { INSTANCE = it }
            }
    }

    init {
        try {
            mContext = context
            val masterKey = MasterKey.Builder(mContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            mSharedPreferences = EncryptedSharedPreferences.create(
                mContext,
                getDefaultSharedPreferencesName(mContext),
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // Fallback, default mode is Context.MODE_PRIVATE !
            mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
        }
    }

    private fun getDefaultSharedPreferencesName(context: Context): String {
        return context.packageName + "_encrypted_preferences"
    }

}
