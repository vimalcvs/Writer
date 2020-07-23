package com.alim.writeradmin.Database

import android.content.Context

class Settings (val context: Context) {

    private val NAME = "NAME"
    private val PHOTO = "PHOTO"
    private val THEME = "THEME"
    private val COLORFUL = "COLORFUL"
    private val DATA_NAME = "APP_DATA"

    var image: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(PHOTO, "")!!
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(PHOTO, value)
            editor.apply()
        }

    var name: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(NAME, "")!!
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(NAME, value)
            editor.apply()
        }

    var theme: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(THEME, false)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(THEME, value)
            editor.apply()
        }

    var colorful: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(COLORFUL, true)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(COLORFUL, value)
            editor.apply()
        }
}