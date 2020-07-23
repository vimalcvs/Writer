package com.alim.writer.Model

import android.os.Parcel
import android.os.Parcelable

class PostModel(): Parcelable {

    var ver = ""
    var pos = ""
    var uid = ""
    var like = 0
    var name = ""
    var views = 0
    var liked = 0
    var link = ""
    var date = ""
    var title = ""
    var content = ""
    var profile = ""
    var comment = ""
    var comments = 0
    var category = ""
    var description = ""

    constructor(parcel: Parcel) : this() {
        like = parcel.readInt()
        views = parcel.readInt()
        liked = parcel.readInt()
        ver = parcel.readString()!!
        pos = parcel.readString()!!
        uid = parcel.readString()!!
        name = parcel.readString()!!
        link = parcel.readString()!!
        comments = parcel.readInt()
        date = parcel.readString()!!
        title = parcel.readString()!!
        profile = parcel.readString()!!
        content = parcel.readString()!!
        comment = parcel.readString()!!
        category = parcel.readString()!!
        description = parcel.readString()!!
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(like)
        dest.writeInt(views)
        dest.writeInt(liked)
        dest.writeString(ver)
        dest.writeString(uid)
        dest.writeString(pos)
        dest.writeString(name)
        dest.writeString(date)
        dest.writeInt(comments)
        dest.writeString(link)
        dest.writeString(title)
        dest.writeString(profile)
        dest.writeString(content)
        dest.writeString(comment)
        dest.writeString(category)
        dest.writeString(description)
    }

    override fun describeContents(): Int {
        TODO("Not yet implemented")
    }

    companion object CREATOR : Parcelable.Creator<PostModel> {
        override fun createFromParcel(parcel: Parcel): PostModel {
            return PostModel(parcel)
        }

        override fun newArray(size: Int): Array<PostModel?> {
            return arrayOfNulls(size)
        }
    }
}