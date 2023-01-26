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

package com.orange.common.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec

/**
 * Helper class to create and manage cryptography related functions
 */
object CryptographyUtil {

  private const val YOUR_SECRET_KEY_NAME = "Y0UR$3CR3TK3YN@M3"

  private const val KEY_SIZE = 128 // TODO : TBC 128 -> 256
  private const val ANDROID_KEYSTORE = "AndroidKeyStore"
  private const val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
  private const val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
  private const val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

  /**
   * Creates centralised SecretKey using the KeyStore
   */
  fun getOrCreateSecretKey(keyName: String): SecretKey {
    // If Secretkey was previously created for that keyName, then grab and return it.
    val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
    keyStore.load(null) // Keystore must be loaded before it can be accessed
    keyStore.getKey(keyName, null)?.let { return it as SecretKey }

    // if you reach here, then a new SecretKey must be generated for that keyName
    val paramsBuilder = KeyGenParameterSpec.Builder(
        keyName,
        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    )
    paramsBuilder.apply {
      setBlockModes(ENCRYPTION_BLOCK_MODE)
      setEncryptionPaddings(ENCRYPTION_PADDING)
      setKeySize(KEY_SIZE)
      setUserAuthenticationRequired(true)
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
        setInvalidatedByBiometricEnrollment(true)
      }
      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
        setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG or KeyProperties.AUTH_DEVICE_CREDENTIAL)
      } else {
       @Suppress("DEPRECATION")
        setUserAuthenticationValidityDurationSeconds(-1)
      }
    }

    val keyGenParams = paramsBuilder.build()
    val keyGenerator = KeyGenerator.getInstance(
        KeyProperties.KEY_ALGORITHM_AES,
        ANDROID_KEYSTORE
    )
    keyGenerator.init(keyGenParams)

    return keyGenerator.generateKey()
  }

  /**
   * Returns Cipher instance that uses the SecretKey to encrypt / decrypt data
   */
  fun getCipher(): Cipher {
    val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"

    return Cipher.getInstance(transformation)
  }

  /**
   * Prepares Cipher instance to encrypt data with the SecretKey
   */
  @JvmStatic
  fun getInitializedCipherForEncryption(): Cipher {
    val cipher = getCipher()
    val secretKey = getOrCreateSecretKey(YOUR_SECRET_KEY_NAME)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey)

    return cipher
  }

  /**
   * Returns the same Cipher for decrypting data which was used for encryption
   */
  @JvmStatic
  fun getInitializedCipherForDecryption(
      initializationVector: ByteArray? = null
  ): Cipher {
    val cipher = getCipher()
    val secretKey = getOrCreateSecretKey(YOUR_SECRET_KEY_NAME)
    cipher.init(
        Cipher.DECRYPT_MODE,
        secretKey,
        GCMParameterSpec(KEY_SIZE, initializationVector)
    )

    return cipher
  }

  /**
   * Encrypts text with a Cipher and converts to EncryptedMessage
   */
  @JvmStatic
  fun encryptData(plaintext: String, cipher: Cipher): EncryptedMessage {
    val ciphertext = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
    return EncryptedMessage(ciphertext, cipher.iv)
  }

  /**
   * Decrypts text with a Cipher and converts to plain String
   */
  @JvmStatic
  fun decryptData(ciphertext: ByteArray, cipher: Cipher): String {
    val plaintext = cipher.doFinal(ciphertext)
    return String(plaintext, Charset.forName("UTF-8"))
  }

  @JvmStatic
  fun computeKey(input: String): ByteArray {
//    val digest: MessageDigest = MessageDigest.getInstance("SHA-256")
//    val hash: ByteArray = digest.digest(input.toByteArray(StandardCharsets.UTF_8))
//    return hash

    val random = SecureRandom()
    val salt = ByteArray(256)
    random.nextBytes(salt)
    val pbKeySpec = PBEKeySpec(input.toCharArray(), salt, 100000, 256)
    val secretKeyFactory = SecretKeyFactory.getInstance("PBEwithHmacSHA256AndAES_256")
    return secretKeyFactory.generateSecret(pbKeySpec).encoded
  }

  @JvmStatic
  fun bytesToHex(hash: ByteArray): String {
    val hexString = StringBuilder(2 * hash.size)
    for (i in hash.indices) {
      val hex = Integer.toHexString(0xff and hash[i].toInt())
      if (hex.length == 1) {
        hexString.append('0')
      }
      hexString.append(hex)
    }
    return hexString.toString()
  }

}

