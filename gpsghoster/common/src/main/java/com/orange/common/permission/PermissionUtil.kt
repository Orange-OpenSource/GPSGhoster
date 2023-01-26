/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.common.permission

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat

abstract class PermissionUtil {
    companion object {
        fun checkSelfPermission(
            @NonNull context: Context,
            @NonNull permissions: Array<String>
        ): Boolean {
            for (permission in permissions) {
                if (PackageManager.PERMISSION_GRANTED != ActivityCompat.checkSelfPermission(
                        context,
                        permission
                    )
                ) {
                    return false
                }
            }
            return true
        }

        fun shouldShowRequestPermissionRationale(
            @NonNull activity: Activity,
            @NonNull permissions: Array<String>
        ): Boolean {
            for (permission in permissions) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                    return true
                }
            }
            return false
        }

        fun requestPermissions(
            @NonNull activity: Activity,
            @NonNull permissions: Array<String>,
            requestCode: Int) {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
            )
        }

        fun verifyPermissions(
            grantResults: IntArray
        ): Boolean {
            // At least one result must be checked.
            if (grantResults.size < 1) {
                return false
            }

            // Verify that each required permission has been granted, otherwise return false.
            for (result in grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
            return true
        }
    }
}
