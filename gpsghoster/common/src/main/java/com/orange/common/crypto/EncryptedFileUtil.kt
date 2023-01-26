/*
 * Software Name: GPSGhoster
 * SPDX-FileCopyrightText: Copyright (c) 2023 Orange
 * SPDX-License-Identifier: BSD 3-Clause "New"
 *
 * This software is distributed under the BSD-3-Clause license.
 *
 * Author: Franck SEROT <frank.serot@orange.com> et al.
*/
package com.orange.common.crypto

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import org.jetbrains.annotations.NotNull
import java.io.File
import java.net.URLDecoder
import java.net.URLEncoder

class EncryptedFileUtil private constructor(@NotNull context: Context) {

    companion object {
        @Volatile private var INSTANCE: EncryptedFileUtil? = null

        fun getInstance(context: Context): EncryptedFileUtil =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: EncryptedFileUtil(context).also { INSTANCE = it }
            }
    }

    private lateinit var context: Context
    private lateinit var masterKey: MasterKey
    private lateinit var fileEncryptionScheme: EncryptedFile.FileEncryptionScheme

    init {
        try {
            this.context = context
            this.masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            this.fileEncryptionScheme = EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        } catch (e: Exception) {

        }
    }

    /**
     * Get an [EncryptedFile], used to encrypt and decrypt files using Jetpack Security
     */
    private fun getEncryptedFile(name: String): EncryptedFile {
        return EncryptedFile.Builder(
            context,
            File(context.filesDir, name.urlEncode()),
            masterKey,
            fileEncryptionScheme
        ).build()
    }

    /**
     * Encrypt a file using the title and body of this fragment's text fields.
     *
     * If an existing file is currently being edited, delete and replace it.
     */
    fun encryptFile(title : String, body : String) {
//        val title = binding.titleEditText.text.toString()
//        val body = binding.bodyEditText.text.toString()

        if (title.isBlank()) return

        try {
//            deleteFile(existingFileTitle)
            deleteFile(title)
            val encryptedFile = getEncryptedFile(title)
            encryptedFile.openFileOutput().use { output ->
                output.write(body.toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
//            showSnackbar(R.string.error_unable_to_save_file)
        }
    }

    /**
     * Delete a file from the directory.
     */
    private fun deleteFile(title: String) {
        if (title.isBlank()) return
        val file = File(context.filesDir, title.urlEncode())
        if (file.exists()) file.delete()
    }

    /**
     * Decrypt an encrypted file's body and return the plain text String.
     */
    fun decryptFile(title: String): String {
        val encryptedFile = getEncryptedFile(title)

        try {
            encryptedFile.openFileInput().use { input ->
                return String(input.readBytes(), Charsets.UTF_8)
            }
        } catch (e: Exception) {
            e.printStackTrace()
//            showSnackbar(R.string.error_unable_to_decrypt)
            return ""
        }
    }

    /**
     * Extension method to decode a URL encoded a string.
     */
    fun String.urlDecode():String = URLDecoder.decode(this, "UTF-8")

    /**
     * Extension method to URL encode a string.
     */
    fun String.urlEncode():String = URLEncoder.encode(this, "UTF-8")

}
