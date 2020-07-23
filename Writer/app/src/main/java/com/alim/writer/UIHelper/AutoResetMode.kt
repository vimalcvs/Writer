package com.alim.writer.UIHelper

import androidx.annotation.IntDef
import com.alim.writer.UIHelper.AutoResetMode
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.SOURCE)
/*@IntDef(
    AutoResetMode.NEVER,
    AutoResetMode.UNDER,
    AutoResetMode.OVER,
    AutoResetMode.ALWAYS
)*/
annotation class AutoResetMode {

    object Parser {
        @AutoResetMode
        fun fromInt(value: Int): Int {
            return when (value) {
                OVER -> OVER
                ALWAYS -> ALWAYS
                NEVER -> NEVER
                else -> UNDER
            }
        }
    }

    companion object {
        var UNDER = 0
        var OVER = 1
        var ALWAYS = 2
        var NEVER = 3
    }
}