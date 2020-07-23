package com.alim.writer.UIHelper

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.RecyclerView

class NonScrollRecycler : RecyclerView {
    constructor(context: Context) : super(context) {}
    constructor(
        context: Context,
        attrs: AttributeSet?
    ) : super(context, attrs) {
    }

    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int
    ) : super(context, attrs, defStyleAttr) {
    }

    override fun onMeasure(widthSpec: Int, heightSpec: Int) {
        val custom = MeasureSpec.makeMeasureSpec(Int.MAX_VALUE shr 2, MeasureSpec.AT_MOST)
        super.onMeasure(widthSpec, custom)
        val params = layoutParams
        params.height = measuredHeight
    }
}