package com.alim.writer.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Class.CircleImageView
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.FollowingData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.ProfileActivity
import com.alim.writer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class FollowAdapter(context: Context, mData: ArrayList<String>)
    : RecyclerView.Adapter<FollowAdapter.ViewHolder>(){

    val USER_DATA = "User Data"
    private val mContext = context
    private val userData = UserData(context)
    private val followData = FollowingData(context)
    private val list = mData

    private val cache = when(ApplicationData(context).cache) {
        true -> DiskCacheStrategy.AUTOMATIC
        false -> DiskCacheStrategy.NONE
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val follow = view.findViewById<Button>(R.id.follow)
        val follower = view.findViewById<TextView>(R.id.follower)
        val profile = view.findViewById<CircleImageView>(R.id.profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.follow_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val uid = list[position]
        if (uid == userData.uid) {
            holder.follow.isClickable = false
            holder.follow.text = "Self"
        } else holder.follow.isClickable = true
        Log.println(Log.ASSERT,"SAVED UID",followData.getFollowing(uid).toString())

        if (followData.getFollowing(uid))
            holder.follow.text = "Following"
        else
            holder.follow.text = "Follow"

        val nRef = FirebaseDatabase.getInstance()
            .getReference(USER_DATA).child(uid).child("Name")
        val pRef = FirebaseDatabase.getInstance()
            .getReference(USER_DATA).child(uid).child("Photo")
        val fRef = FirebaseDatabase.getInstance()
            .getReference(USER_DATA).child(uid).child("Followers")
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                holder.name.text = data.value.toString()
            }

        }).listenOnce(nRef)
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                try {
                    Glide.with(mContext).load(data.value.toString())
                        .dontAnimate()
                        .diskCacheStrategy(cache)
                        .centerCrop().into(holder.profile)
                } catch (e: Exception) {
                    Log.println(Log.ASSERT,"Exception Comment", "$e")
                }
            }

        }).listenOnce(pRef)

        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                try {
                    if (data.child(userData.uid).exists())
                        holder.follow.text = "Following"
                    else
                        holder.follow.text = "Follow"
                    holder.follower.text = "Followers : ${data.childrenCount.toInt()}"
                } catch (e: Exception) {
                    Log.println(Log.ASSERT,"Follow Adapter Ex","$e")
                }
            }

        }).listenOnce(fRef)

        holder.follow.setOnClickListener {
            if (holder.follow.text == "Follow") {
                followData.setFollowing(uid, true)
                fRef.child(userData.uid).setValue(true)
                FirebaseDatabase.getInstance().getReference(USER_DATA).child(userData.uid)
                    .child("Following").child(uid).setValue(true)
            }
            else {
                followData.setFollowing(uid, false)
                fRef.child(userData.uid).removeValue()
                FirebaseDatabase.getInstance().getReference(USER_DATA).child(userData.uid)
                    .child("Following").child(uid).removeValue()
            }
            notifyItemChanged(position)
        }

        holder.itemView.setOnClickListener {
            val profile = Intent(mContext, ProfileActivity::class.java)
            profile.putExtra("UID", uid)
            profile.putExtra("NAME","")
            mContext.startActivity(profile)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}