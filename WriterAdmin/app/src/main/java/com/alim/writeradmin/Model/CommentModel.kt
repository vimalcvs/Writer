package com.alim.writer.Model

import android.os.Parcel
import android.os.Parcelable

class CommentModel(): Parcelable {

    var pos = 0
    var uid = ""
    var name = ""
    var date = ""
    var photo = ""
    var comment = ""

    constructor(parcel: Parcel) : this() {
        pos = parcel.readInt()
        uid = parcel.readString()!!
        name = parcel.readString()!!
        date = parcel.readString()!!
        photo = parcel.readString()!!
        comment = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(pos)
        dest.writeString(uid)
        dest.writeString(name)
        dest.writeString(date)
        dest.writeString(photo)
        dest.writeString(comment)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<CommentModel> {
        override fun createFromParcel(parcel: Parcel): CommentModel {
            return CommentModel(parcel)
        }

        override fun newArray(size: Int): Array<CommentModel?> {
            return arrayOfNulls(size)
        }
    }
}