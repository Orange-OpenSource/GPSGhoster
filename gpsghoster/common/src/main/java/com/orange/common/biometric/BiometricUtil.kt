/*
 *
 *  * Copyright (c) 2020 Razeware LLC
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in
 *  * all copies or substantial portions of the Software.
 *  *
 *  * Notwithstanding the foregoing, you may not use, copy, modify, merge, publish,
 *  * distribute, sublicense, create a derivative work, and/or sell copies of the
 *  * Software in any work that is designed, intended, or marketed for pedagogical or
 *  * instructional purposes related to programming, coding, application development,
 *  * or information technology.  Permission for such use, copying, modification,
 *  * merger, publication, distribution, sublicensing, creation of derivative works,
 *  * or sale is expressly withheld.
 *  *
 *  * This project and source code may use libraries or frameworks that are
 *  * released under various Open-Source licenses. Use of those libraries and
 *  * frameworks are governed by their own individual licenses.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 *  * THE SOFTWARE.
 *
 *
 */

package com.orange.common.biometric

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

/**
 * Helper class for managing Biometric Authentication Process
 */
object BiometricUtil {

  /**
   * Checks if the device has Biometric support
   */
  fun hasBiometricCapability(context: Context): Int {
    val biometricManager = BiometricManager.from(context)
    return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//        biometricManager.canAuthenticate(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
        biometricManager.canAuthenticate(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
    } else {
        @Suppress("DEPRECATION")
        biometricManager.canAuthenticate()
    }
  }

  /**
   * Checks if Biometric Authentication (example: Fingerprint) is set in the device
   */
  @JvmStatic
  fun isBiometricReady(context: Context) =
      hasBiometricCapability(context) == BiometricManager.BIOMETRIC_SUCCESS

  /**
   * Prepares PromptInfo dialog with provided configuration
   */
  fun setBiometricPromptInfo(
      title: String,
      subtitle: String,
      description: String,
      allowDeviceCredential: Boolean
  ): BiometricPrompt.PromptInfo {
    val builder = BiometricPrompt.PromptInfo.Builder()
        .setTitle(title)
        .setSubtitle(subtitle)
        .setDescription(description)

    // Use Device Credentials if allowed, otherwise show Cancel Button
    builder.apply {
      if (allowDeviceCredential) {
          if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//              setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK or DEVICE_CREDENTIAL)
              setAllowedAuthenticators(BIOMETRIC_STRONG or DEVICE_CREDENTIAL)
          } else if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.R) {
            // Fix : java.lang.IllegalArgumentException: Crypto-based authentication is not supported for device credential prior to API 30.
            @Suppress("DEPRECATION")
            setDeviceCredentialAllowed(false)
            setNegativeButtonText("Cancel")
          }
      } else {
          setNegativeButtonText("Cancel")
      }
    }

    return builder.build()
  }

  /**
   * Initializes BiometricPrompt with the caller and callback handlers
   */
  fun initBiometricPrompt(
      activity: AppCompatActivity,
      listener: BiometricAuthListener
  ): BiometricPrompt {
    // Attach calling Activity
    val executor = ContextCompat.getMainExecutor(activity)

    // Attach callback handlers
    val callback = object : BiometricPrompt.AuthenticationCallback() {
      override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        super.onAuthenticationError(errorCode, errString)
        listener.onBiometricAuthenticationError(errorCode, errString.toString())
      }

      override fun onAuthenticationFailed() {
        super.onAuthenticationFailed()
        Log.w(this.javaClass.simpleName, "Authentication failed for an unknown reason")
      }

      override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        super.onAuthenticationSucceeded(result)
        listener.onBiometricAuthenticationSuccess(result)
      }
    }

    return BiometricPrompt(activity, executor, callback)
  }

  /**
   * Displays a BiometricPrompt with provided configurations
   */
  @JvmStatic
  fun showBiometricPrompt(
      title: String = "Biometric Authentication",
      subtitle: String = "Enter biometric credentials to proceed.",
      description: String = "Input your Fingerprint or FaceID to ensure it's you!",
      activity: AppCompatActivity,
      listener: BiometricAuthListener,
      cryptoObject: BiometricPrompt.CryptoObject? = null,
      allowDeviceCredential: Boolean = false
  ) {
    // Prepare BiometricPrompt Dialog
    val promptInfo = setBiometricPromptInfo(
        title,
        subtitle,
        description,
        allowDeviceCredential
    )

    // Attach with caller and callback handler
    val biometricPrompt = initBiometricPrompt(activity, listener)

    // Authenticate with a CryptoObject if provided, otherwise default authentication
    biometricPrompt.apply {
      if (cryptoObject == null) authenticate(promptInfo)
      else authenticate(promptInfo, cryptoObject)
    }
  }

  /**
   * Navigates to Device's Settings screen Biometric Setup
   */
  @JvmStatic
  fun lunchBiometricSettings(context: Context) {
    ActivityCompat.startActivity(
        context,
        Intent(android.provider.Settings.ACTION_SECURITY_SETTINGS),
        null
    )
  }

}
