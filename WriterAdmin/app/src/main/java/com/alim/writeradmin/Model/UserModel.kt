package com.alim.writeradmin.Model

import android.os.Parcel
import android.os.Parcelable

class UserModel(): Parcelable {

    var uid = ""
    var user = ""
    var name = ""
    var email = ""
    var profile = ""

    constructor(parcel: Parcel) : this() {
        uid = parcel.readString()!!
        user = parcel.readString()!!
        name = parcel.readString()!!
        email = parcel.readString()!!
        profile = parcel.readString()!!
    }

    override fun writeToParcel(p0: Parcel, p1: Int) {
        p0.writeString(uid)
        p0.writeString(user)
        p0.writeString(name)
        p0.writeString(email)
        p0.writeString(profile)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}