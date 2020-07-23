package com.alim.writer.Model

import android.os.Parcel
import android.os.Parcelable

class FollowModel(): Parcelable {

    var uid = ""
    var name = ""
    var email = ""
    var photo = ""

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()!!
        name = parcel.readString()!!
        email = parcel.readString()!!
        photo = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(uid)
        dest.writeString(name)
        dest.writeString(email)
        dest.writeString(photo)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<FollowModel> {
        override fun createFromParcel(parcel: Parcel): FollowModel {
            return FollowModel(parcel)
        }

        override fun newArray(size: Int): Array<FollowModel?> {
            return arrayOfNulls(size)
        }
    }
}