package com.alim.writer.Firebase

import android.content.Context
import com.alim.writer.DataHolder.FollowIndexData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.DataHolder.VideoIndexData
import com.alim.writer.Database.FollowingData
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.Interfaces.OnScroll
import com.alim.writer.Model.IndexModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference

class Indexer {

    companion object { var running = false }

    var pos = 0
    var array : ArrayList<Int> = ArrayList()
    lateinit var knock: OnScroll
    lateinit var iRef: DatabaseReference
    lateinit var followingData: FollowingData

    fun index (context: Context, p : Int, ref: DatabaseReference, scroll: OnScroll) {
        pos = p
        iRef = ref
        knock = scroll
        followingData = FollowingData(context)
        if (!array.contains(p)) {
            Thread(threadIndex).start()
            array.add(p)
        }
    }

    private val threadIndex = Thread {
        running = true
        DataReader(object: Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                Thread() {
                    for (x in 10 downTo 1) {
                        if (data.child("$x").exists()) {
                            val indexModel = IndexModel()
                            indexModel.pos = data.child("$x").child("POS").value.toString()
                            indexModel.uid = data.child("$x").child("UID").value.toString()
                            indexModel.content = data.child("$x").child("Content").value.toString()
                            indexModel.category = data.child("$x").child("Category").value.toString()
                            process(data, indexModel, x)
                        }
                    }
                    knock.scrolled()
                    running = false
                }.start()
            }
        }).listenOnce(iRef.child("$pos"))
    }

    private fun process(data: DataSnapshot, indexModel: IndexModel, x: Int) {
        if (data.child("$x").child("Approved").value.toString().toBoolean()) {
            IndexData.index.add(indexModel)
            if (IndexData.index.size % 4 == 0) IndexData.index.add(indexModel)
            if (indexModel.content == "Youtube") {
                VideoIndexData.index.add(indexModel)
                if (VideoIndexData.index.size % 4 == 0) VideoIndexData.index.add(
                    indexModel
                )
            }
            if (followingData.getFollowing(indexModel.uid)) {
                FollowIndexData.index.add(indexModel)
                if (FollowIndexData.index.size % 4 == 0) FollowIndexData.index.add(
                    indexModel
                )
            }
        }
    }
}