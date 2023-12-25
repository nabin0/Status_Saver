package com.zoomcodez.statussaver_downloadvideo

import android.content.Context
import android.content.SharedPreferences
import android.provider.SyncStateContract.Constants


class PreferenceManager(context: Context) {

    private val sharedPreferences: SharedPreferences

    init {
        sharedPreferences =
            context.getSharedPreferences("data", Context.MODE_PRIVATE)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getBoolean(key: String): Boolean {
        return sharedPreferences.getBoolean(key, false)
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getString(key: String): String? {
        return sharedPreferences.getString(key, "null")
    }

    fun clearAll() {
        sharedPreferences.edit().clear().apply()
    }

    fun getSharedPref(): SharedPreferences = sharedPreferences
}