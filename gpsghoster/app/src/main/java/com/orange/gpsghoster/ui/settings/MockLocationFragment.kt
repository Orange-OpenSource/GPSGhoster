/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.gpsghoster.ui.settings

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.preference.*
import com.orange.gpsghoster.R
import com.orange.common.preference.AppPreferenceDataStore
import com.orange.gpsghoster.location.MockLocationUtil
import org.jetbrains.annotations.NotNull

class MockLocationFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.preferenceDataStore =
            AppPreferenceDataStore.getInstance(requireContext())

        setPreferencesFromResource(R.xml.mock_location_preferences, rootKey)

        val mockLocation : SwitchPreferenceCompat? = findPreference(getString(R.string.mock_location_key))
        if (mockLocation != null) {
            if (mockLocation.isChecked && !mockLocationIsAllowed(requireActivity())) {
                mockLocation.isChecked = false
            }
            mockLocation.onPreferenceChangeListener =
                Preference.OnPreferenceChangeListener { _: Preference, _: Any ->
                    return@OnPreferenceChangeListener mockLocationIsAllowed(requireActivity())
                }
        }

        if ((preferenceManager.preferenceDataStore as AppPreferenceDataStore).getBoolean(
                getString(R.string.location_service_key),
                getString(R.string.location_service_default_value).toBoolean()) == true) {
            if (mockLocation != null) {
                mockLocation.isEnabled = false
            }
        }
    }

    private fun mockLocationIsAllowed(@NotNull context : Context) : Boolean {
        if (MockLocationUtil.hasAccessMockLocation(context) &&
            MockLocationUtil.hasDevelopmentSetting(context) &&
            MockLocationUtil.isAllowMockLocation(context) ){
            return true
        } else {
            val builder: AlertDialog.Builder = AlertDialog.Builder(context)
            builder.setMessage(R.string.alert_message_mock_location)
            builder.setCancelable(false)
            builder.setNegativeButton(R.string.alert_negative_button_mock_location) {
                    dialog, _ -> dialog.dismiss()
            }
            builder.setPositiveButton(R.string.alert_positive_button_mock_location) {
                    _, _ -> startActivity(Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS))
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.show()
            return false
        }
    }

}
