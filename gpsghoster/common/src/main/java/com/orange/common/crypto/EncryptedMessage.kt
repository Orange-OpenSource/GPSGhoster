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

import android.os.Parcel
import android.os.Parcelable

/**
 * Parcelable Data class with encrypted text, the initializationVector from the Cipher and timestamp
 */
data class EncryptedMessage(
    val cipherText: ByteArray?,
    val initializationVector: ByteArray?,
    val savedAt: Long = System.currentTimeMillis()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createByteArray(),
        parcel.createByteArray(),
        parcel.readLong()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByteArray(cipherText)
        parcel.writeByteArray(initializationVector)
        parcel.writeLong(savedAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedMessage

        if (cipherText != null) {
            if (other.cipherText == null) return false
            if (!cipherText.contentEquals(other.cipherText)) return false
        } else if (other.cipherText != null) return false
        if (initializationVector != null) {
            if (other.initializationVector == null) return false
            if (!initializationVector.contentEquals(other.initializationVector)) return false
        } else if (other.initializationVector != null) return false
        if (savedAt != other.savedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cipherText?.contentHashCode() ?: 0
        result = 31 * result + (initializationVector?.contentHashCode() ?: 0)
        result = 31 * result + savedAt.hashCode()
        return result
    }

    companion object CREATOR : Parcelable.Creator<EncryptedMessage> {
        override fun createFromParcel(parcel: Parcel): EncryptedMessage {
            return EncryptedMessage(parcel)
        }

        override fun newArray(size: Int): Array<EncryptedMessage?> {
            return arrayOfNulls(size)
        }
    }
}
