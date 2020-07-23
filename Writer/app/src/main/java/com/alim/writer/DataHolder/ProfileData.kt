package com.alim.writer.DataHolder

import com.alim.writer.Model.PostModel

class ProfileData {

    companion object {
        var pData:  ArrayList<PostModel> = ArrayList()
    }

    fun getLength(): Int {
        return pData.size
    }

    fun setData(data: PostModel) {
        pData.add(data)
    }

    fun getData(): ArrayList<PostModel> {
        return pData
    }

    fun clear() {
        pData.clear()
    }
}