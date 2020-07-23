package com.alim.writer.Database

import android.content.Context

class UserData( val context: Context) {

    private val DATA_NAME = "USER_DATA"
    private val SESSION = "SESSION"
    private val NAME = "NAME"
    private val EMAIL = "EMAIL"
    private val IMAGE = "IMAGE"
    private val UID = "UID"
    private val INTERVAL = "INTERVAL"
    private val VERIFIED = "VERIFIED"

    var interval: Int
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getInt(INTERVAL, 0) }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putInt(INTERVAL, value)
            editor.apply() }

    var verified: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(VERIFIED, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(VERIFIED, value)
            editor.apply() }

    var uid: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(UID, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(UID, value)
            editor.apply() }

    var name: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(NAME, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(NAME, value)
            editor.apply() }

    var email: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(EMAIL, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(EMAIL, value)
            editor.apply() }

    var image: String
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getString(IMAGE, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putString(IMAGE, value)
            editor.apply() }

    var session: Boolean
        get() {
            val prefs = context.getSharedPreferences(DATA_NAME, 0)
            return prefs.getBoolean(SESSION, false)
        }
        set(value) {
            val sharedPref = context.getSharedPreferences(DATA_NAME, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(SESSION, value)
            editor.apply()
        }
}