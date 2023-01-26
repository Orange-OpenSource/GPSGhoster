/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.ui.osm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.volley.RequestQueue
import com.orange.common.crypto.EncryptedFileUtil
import com.orange.common.preference.AppPreferenceDataStore

class OpenStreetMapModelFactory(var mPreferenceDataStore: AppPreferenceDataStore, var mEncryptedFileUtil : EncryptedFileUtil, var mRequestQueue: RequestQueue) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OpenStreetMapModel::class.java)) {
            return OpenStreetMapModel(mPreferenceDataStore, mEncryptedFileUtil, mRequestQueue) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
