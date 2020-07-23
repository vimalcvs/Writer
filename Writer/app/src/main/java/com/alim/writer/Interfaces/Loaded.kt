package com.alim.writer.Interfaces

import com.google.firebase.database.DataSnapshot

interface Loaded {
    fun onLoaded(x: Int, data: DataSnapshot)
}