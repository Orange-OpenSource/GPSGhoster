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
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log

import androidx.annotation.NonNull
import com.orange.common.R

//import com.orange.gpsghoster.R
import java.util.stream.Collectors

class PermissionHelper(@NonNull var activity: Activity) {
    private var TAG: String? = PermissionHelper::class.java.simpleName

    private enum class RequestCode(val value:Int) {
        PERMISSIONS(1),
        APP_SETTINGS(2)
    }

    private var alertDialog: AlertDialog? = null
    private var isExitWhenFailed = true
    private var callback: PermissionCallback? = null

    fun checkPermission(@NonNull permissions: Array<String>): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (!PermissionUtil.checkSelfPermission(activity, permissions)) {
                Log.d(TAG, "Not all required permissions have been granted.")
                false
            } else {
                Log.d(TAG, "All required permissions have been granted.")
                true
            }
        } else {
            true
        }
    }

    fun requestPermissions(@NonNull permissions: Array<String>, isExitWhenFailed: Boolean, callback: PermissionCallback) {
        // Update flag to determine action when failed
        this.isExitWhenFailed = isExitWhenFailed
        this.callback = callback
        // Request permissions
        if (PermissionUtil.shouldShowRequestPermissionRationale(activity, permissions)) {
            Log.d(TAG, "Displaying permission rationale")
            val alertDialogBuilder = AlertDialog.Builder(activity)
            alertDialogBuilder.setMessage(R.string.permission_alertDialog_message)
                .setCancelable(false)
                .setPositiveButton( android.R.string.ok) {
                    _, _ -> PermissionUtil.requestPermissions(activity, permissions, RequestCode.PERMISSIONS.value)
                }
                .setNegativeButton(android.R.string.cancel) {
                    _, _ -> continueWithFailed()
                }
            alertDialog = alertDialogBuilder.create()
            alertDialog?.show()
        } else {
            PermissionUtil.requestPermissions(activity, permissions, RequestCode.PERMISSIONS.value)
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        @NonNull permissions: Array<String>,
        @NonNull grantResults: IntArray
    ): Boolean {
        return when (requestCode) {
            RequestCode.PERMISSIONS.value -> {
                Log.d(TAG, "Received response for permission request.")
                Log.d(TAG, TextUtils.join(",", permissions) );
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Log.i(TAG, "Permissions have been granted.")
                    true
                } else {
                    Log.i(TAG, "Permissions have not beend granted.")
                    val alertDialogBuilder = AlertDialog.Builder(activity)
                    alertDialogBuilder.setMessage(R.string.permission_alertDialog_message)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok) {
                            _, _ -> goToAppSettings()
                        }
                        .setNegativeButton(android.R.string.cancel) {
                            _, _ -> continueWithFailed()
                        }
                    alertDialog = alertDialogBuilder.create()
                    alertDialog?.show()
                    false
                }
            }
            else -> false
        }
    }

    fun onRequestPermissionsGranted() {
        callback!!.onRequestPermissionsGranted()
    }

    private fun continueWithFailed() {
        if (isExitWhenFailed) {
            callback!!.exitApp()
        }
    }

    private fun goToAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + activity.packageName)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
        )
        activity.startActivityForResult(intent, RequestCode.APP_SETTINGS.value)
    }
}
