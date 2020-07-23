package com.alim.writeradmin.Model

import android.os.Parcel
import android.os.Parcelable

class IndexModel(): Parcelable {

    var x = 0
    var y = 0
    var uid = ""
    var pos = ""
    var content = ""
    var category = ""
    var approved = ""

    constructor(parcel: Parcel) : this() {
        x = parcel.readInt()
        y = parcel.readInt()
        uid = parcel.readString()!!
        pos = parcel.readString()!!
        content = parcel.readString()!!
        category = parcel.readString()!!
        approved = parcel.readString()!!
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeInt(x)
        p0.writeInt(y)
        p0.writeString(uid)
        p0.writeString(pos)
        p0.writeString(content)
        p0.writeString(category)
        p0.writeString(approved)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<IndexModel> {
        override fun createFromParcel(parcel: Parcel): IndexModel {
            return IndexModel(parcel)
        }

        override fun newArray(size: Int): Array<IndexModel?> {
            return arrayOfNulls(size)
        }
    }

}