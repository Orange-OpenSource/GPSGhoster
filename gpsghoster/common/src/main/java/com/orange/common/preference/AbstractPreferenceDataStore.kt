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
import android.content.SharedPreferences
import android.location.Criteria
import android.util.Log
import androidx.annotation.Nullable
import androidx.preference.PreferenceDataStore
import com.google.android.gms.location.LocationRequest

abstract class AbstractPreferenceDataStore : PreferenceDataStore() {

    private val TAG : String? = AbstractPreferenceDataStore::class.java.simpleName

    protected lateinit var mContext: Context
    protected lateinit var mSharedPreferences : SharedPreferences

    fun getSharedPreferences() : SharedPreferences = mSharedPreferences

    override fun putString(key: String?, @Nullable value: String?) {
        mSharedPreferences.edit().putString(key, value).apply()
    }

    override fun putStringSet(key: String?, @Nullable values: Set<String?>?) {
        mSharedPreferences.edit().putStringSet(key, values).apply()
    }

    override fun putInt(key: String?, value: Int) {
        mSharedPreferences.edit().putInt(key, value).apply()
    }

    override fun putLong(key: String?, value: Long) {
        mSharedPreferences.edit().putLong(key, value).apply()
    }

    override fun putFloat(key: String?, value: Float) {
        mSharedPreferences.edit().putFloat(key, value).apply()
    }

    override fun putBoolean(key: String?, value: Boolean) {
        mSharedPreferences.edit().putBoolean(key, value).apply()
    }

    @Nullable
    override fun getString(key: String?, @Nullable defValue: String?): String? {
        return mSharedPreferences.getString(key, defValue)
    }

    @Nullable
    override fun getStringSet(key: String?, @Nullable defValues: Set<String?>?): Set<String?>? {
        return mSharedPreferences.getStringSet(key, defValues)
    }

    override fun getInt(key: String?, defValue: Int): Int {
        return mSharedPreferences.getInt(key, defValue)
    }

    override fun getLong(key: String?, defValue: Long): Long {
        return mSharedPreferences.getLong(key, defValue)
    }

    override fun getFloat(key: String?, defValue: Float): Float {
        return mSharedPreferences.getFloat(key, defValue)
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean {
        return mSharedPreferences.getBoolean(key, defValue)
    }

    //

    fun contains(key : String) : Boolean {
        return mSharedPreferences.contains(key)
    }

    fun remove(key: String?) {
        mSharedPreferences.edit().remove(key).apply()
    }

    fun getBooleanField(key : Int, defaultValue : Int): Boolean {
        var booleanValue = false

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : Boolean = mSharedPreferences.getBoolean(stringKey, stringDefaultValue.toBoolean())
            booleanValue = value
            Log.d(TAG, stringKey + " : " + value + " : " + booleanValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return booleanValue
    }

    fun getIntField(key : Int, defaultValue : Int): Int {
        var intValue = 0

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            intValue = value?.toInt() ?: 0
            Log.d(TAG, stringKey + " : " + value + " : " + intValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return intValue
    }

    fun getLongField(key : Int, defaultValue : Int): Long {
        var longValue = 0L

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            longValue = value?.toLong() ?: 0L
            Log.d(TAG, stringKey + " : " + value + " : " + longValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return longValue
    }

    fun getFloatField(key : Int, defaultValue : Int): Float {
        var floatValue = 0.0F

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            floatValue = value?.toFloat() ?: 0.0F
            Log.d(TAG, stringKey + " : " + value + " : " + floatValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return floatValue
    }

    fun getDoubleField(key : Int, defaultValue : Int): Double {
        var doubleValue = 0.0

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            doubleValue = value?.toDouble() ?: 0.0
            Log.d(TAG, stringKey + " : " + value + " : " + doubleValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return doubleValue
    }

    fun getStringField(key : Int, defaultValue : Int): String {
        var stringValue = ""

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            stringValue = value ?: ""
            Log.d(TAG, stringKey + " : " + value + " : " + stringValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return stringValue
    }

    // Android location

    fun getCriteriaField(key : Int, defaultValue : Int): Int {
        var intValue  = 0

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            val field = Criteria::class.java.getField(value!!)
            intValue = field.getInt(field)
            Log.d(TAG, stringKey + " : " + value + " : " + intValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return intValue
    }

    // Google location

    fun getPriorityField(key : Int, defaultValue : Int): Int {
        var intValue = 0

        try {
            val stringKey : String = mContext.getString(key)
            val stringDefaultValue : String = mContext.getString(defaultValue)
            val value : String? = mSharedPreferences.getString(stringKey, stringDefaultValue)
            val field = LocationRequest::class.java.getField(value!!)
            intValue = field.getInt(field)
            Log.d(TAG, stringKey + " : " + value + " : " + intValue)
        } catch(e : NoSuchFieldException) {
            Log.e(TAG,"Invalid field. $e")
        }

        return intValue
    }

}
