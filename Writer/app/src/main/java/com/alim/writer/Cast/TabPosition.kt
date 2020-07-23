package com.alim.writer.Cast

import android.content.Context
import android.util.Log
import com.alim.writer.Database.AdminData

class TabPosition {

    companion object { var category = "" }

    fun setCat(p: Int, context: Context) {
        if (p > 2) {
            category = AdminData(context).getCategory()[p-3]
            Log.println(Log.ASSERT, "CAT", category)
        }
    }
}