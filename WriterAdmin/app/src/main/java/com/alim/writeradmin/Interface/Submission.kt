package com.alim.writeradmin.Interface

import com.google.firebase.database.DataSnapshot

interface Submission {
    fun approved(position: Int)
    fun details(snapshot: DataSnapshot, uid: String, pos: Int, x: Int, y: Int, dpos: String)
}