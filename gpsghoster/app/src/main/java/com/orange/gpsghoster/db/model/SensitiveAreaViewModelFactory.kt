/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.db.model

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.orange.gpsghoster.db.repository.SensitiveAreaRepository

class SensitiveAreaViewModelFactory(private val repository: SensitiveAreaRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SensitiveAreaViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SensitiveAreaViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
