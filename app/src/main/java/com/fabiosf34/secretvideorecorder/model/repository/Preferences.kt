package com.fabiosf34.secretvideorecorder.model.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class Preferences(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences("myPreferences",
        Context.MODE_PRIVATE)

    fun store(key: String, value: Boolean) {
        preferences.edit { putBoolean(key, value) }
    }

    fun store(key: String, value: String) {
        preferences.edit { putString(key, value) }
    }


    fun retrieve(key: String, defaultValue: Boolean): Boolean {
        return preferences.getBoolean(key, defaultValue)
    }

    fun retrieve(key: String, defaultValue: String): String? {
        return preferences.getString(key, defaultValue)
    }

}