package com.alim.writer.DataHolder

import com.alim.writer.Model.IndexModel


class IndexData {

    companion object {
        var loop = 0
        var primaryIndex = -1
        var secondaryIndex = -1
        var index: ArrayList<IndexModel> = ArrayList()
    }

    fun clear() {
        index.clear()
    }
}