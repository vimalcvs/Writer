package com.alim.writer.Database

import android.content.Context

class ApplicationData(val context: Context) {

    private val THEME = "THEME"
    private val STYLE = "STYLE"
    private val CACHE = "CACHE"
    private val DATA_NAME = "APP_DATA"
    private val SCROLL_BAR = "SCROLL_BAR"

    var flat: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(STYLE, true)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(STYLE, value)
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

    var scrollbar: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(SCROLL_BAR, true)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(SCROLL_BAR, value)
            editor.apply()
        }

    var cache: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(CACHE, true)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(CACHE, value)
            editor.apply()
        }
}