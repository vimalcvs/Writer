package com.alim.writer.Cast

import android.util.Log
import com.alim.writer.Interfaces.OnScroll
import java.lang.Exception

class PostCast {

    companion object {
        lateinit var knock: OnScroll
    }

    fun knock() {
        try { knock.scrolled() }
        catch (e: Exception) { Log.println(Log.ASSERT, "PostCast","$e") }
    }
    fun register(scroll: OnScroll) {
        Log.println(Log.ASSERT,"Register","Done")
        knock = scroll
    }
}