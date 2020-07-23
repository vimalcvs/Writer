package com.alim.writer.Model

import android.os.Parcel
import android.os.Parcelable

class IndexModel(): Parcelable {

    var ad = 0
    var pos = ""
    var uid = ""
    var content = ""
    var category = ""

    constructor(parcel: Parcel) : this() {
        ad = parcel.readInt()
        pos = parcel.readString()!!
        uid = parcel.readString()!!
        content = parcel.readString()!!
        category = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(ad)
        dest.writeString(pos)
        dest.writeString(uid)
        dest.writeString(content)
        dest.writeString(category)
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