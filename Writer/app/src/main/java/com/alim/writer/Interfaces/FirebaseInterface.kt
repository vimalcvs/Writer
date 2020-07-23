package com.alim.writer.Interfaces

import com.google.firebase.database.DataSnapshot

interface FirebaseInterface {
    fun onLoaded(data: DataSnapshot)
}