package com.alim.writer.Interfaces

import com.google.firebase.database.DataSnapshot

interface Knock {
    fun knocked(data: DataSnapshot)
}