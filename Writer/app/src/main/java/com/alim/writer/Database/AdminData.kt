package com.alim.writer.Database

import android.content.Context

class AdminData(val context: Context) {

    private val IMAGE = "IMAGE"
    private val LENGTH = "LENGTH"
    private val INTERVAL = "INTERVAL"
    private val APPROVAL = "APPROVAL"
    private val CATEGORY = "CATEGORY"
    private val COMMENTS = "COMMENTS"
    private val ADMIN_DATA = "ADMIN_DATA"
    private val PUBLIC_POST = "PUBLIC_POST"
    private val BANNER_AD_ID = "BANNER_AD_ID"
    private val NATIVE_AD_ID = "NATIVE_AD_ID"
    private val BANNER_AD_ENABLED = "BANNER_AD_ENABLED"
    private val NATIVE_AD_ENABLED = "NATIVE_AD_ENABLED"
    private val INTERSTITIAL_AD_ID = "INTERSTITIAL_AD_ID"
    private val INTERSTITIAL_AD_ENABLED = "INTERSTITIAL_AD_ENABLED"

    var interstitialInterval: Int
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getInt(INTERVAL, 1) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putInt(INTERVAL, value)
            editor.apply() }

    var nativeAdEnabled: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(NATIVE_AD_ENABLED, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(NATIVE_AD_ENABLED, value)
            editor.apply() }

    var nativeAd: String
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getString(NATIVE_AD_ID, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putString(NATIVE_AD_ID, value)
            editor.apply() }

    var interstitialAdEnabled: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(INTERSTITIAL_AD_ENABLED, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(INTERSTITIAL_AD_ENABLED, value)
            editor.apply() }

    var interstitialAd: String
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getString(INTERSTITIAL_AD_ID, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putString(INTERSTITIAL_AD_ID, value)
            editor.apply() }

    var bannerAdEnabled: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(BANNER_AD_ENABLED, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(BANNER_AD_ENABLED, value)
            editor.apply() }

    var bannerAd: String
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getString(BANNER_AD_ID, "")!! }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putString(BANNER_AD_ID, value)
            editor.apply() }

    var publicPost: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(PUBLIC_POST, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(PUBLIC_POST, value)
            editor.apply() }

    var commentsEnabled: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(COMMENTS, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(COMMENTS, value)
            editor.apply() }

    var approval: Boolean
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getBoolean(APPROVAL, false) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putBoolean(APPROVAL, value)
            editor.apply() }

    var imageSize: Int
        get() {
            val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
            return prefs.getInt(IMAGE, 1024) }
        set(value) {
            val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
            val editor = sharedPref.edit()
            editor.putInt(IMAGE, value)
            editor.apply() }

    fun setCategory(list: ArrayList<String>) {
        for (x in 0 until list.size) {
            addData(x, list[x])
        }
        setLength(list.size)
    }

    fun getCategory(): ArrayList<String> {
        val arr: ArrayList<String> = ArrayList()
        for (x in 0 until getLength()) {
            arr.add(getData(x))
        }
        return arr
    }

    private fun addData(pos: Int, data: String) {
        val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
        val editor = sharedPref.edit()
        editor.putString("$CATEGORY : $pos", data)
        editor.apply()
    }

    private fun getData(pos: Int): String {
        val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
        return prefs.getString("$CATEGORY : $pos", "")!!
    }

    private fun setLength(length: Int) {
        val sharedPref = context.getSharedPreferences(ADMIN_DATA, 0)
        val editor = sharedPref.edit()
        editor.putInt(LENGTH, length)
        editor.apply()
    }

    private fun getLength(): Int {
        val prefs = context.getSharedPreferences(ADMIN_DATA, 0)
        return prefs.getInt(LENGTH, 0)
    }
}