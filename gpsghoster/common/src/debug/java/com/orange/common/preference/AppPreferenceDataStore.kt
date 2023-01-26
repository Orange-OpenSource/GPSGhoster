/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.common.preference

import android.content.Context
import androidx.preference.PreferenceManager
import org.jetbrains.annotations.NotNull

class AppPreferenceDataStore private constructor(@NotNull context: Context) : AbstractPreferenceDataStore() {

    companion object {

        @Volatile private var INSTANCE: AppPreferenceDataStore? = null

        @JvmStatic
        fun getInstance(context: Context): AppPreferenceDataStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppPreferenceDataStore(context).also { INSTANCE = it }
            }
    }

    init {
        mContext = context
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext)
    }

    private fun getDefaultSharedPreferencesName(context: Context): String {
        return context.packageName + "_preferences"
    }
}
