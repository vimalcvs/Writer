package com.alim.writer.Database

import android.content.Context

class FollowingData(val context: Context) {

    private val LOADED = "LOADED"
    private val FOLLOW_DATA = "FOLLOW_DATA"

    fun setFollowing(uid: String, bol: Boolean) {
        val sharedPref = context.getSharedPreferences(FOLLOW_DATA, 0)
        val editor = sharedPref.edit()
        editor.putBoolean(uid, bol)
        editor.apply()
    }

    fun getFollowing(uid: String): Boolean {
        val prefs = context.getSharedPreferences(FOLLOW_DATA, 0)
        return prefs.getBoolean(uid, false)
    }

    var loaded: Boolean
        get() {
            val prefs = context.getSharedPreferences(FOLLOW_DATA, 0)
            return prefs.getBoolean(LOADED, false)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(FOLLOW_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(LOADED, value)
        }
}