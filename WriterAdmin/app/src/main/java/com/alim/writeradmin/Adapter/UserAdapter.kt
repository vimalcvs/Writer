package com.alim.writeradmin.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Model.UserModel
import com.alim.writeradmin.ProfileActivity
import com.alim.writeradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*

class UserAdapter(
    context: Context, mData: ArrayList<UserModel>, mDatabase: DatabaseReference)
    : RecyclerView.Adapter<UserAdapter.ViewHolder>(){

    private val data = mData
    val database = mDatabase
    private val mContext = context

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.user_name)
        val email = view.findViewById<TextView>(R.id.user_email)
        val image = view.findViewById<ImageView>(R.id.user_image)
        val verified = view.findViewById<ImageView>(R.id.verified)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.user_view, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.verified.visibility = View.GONE
        database.child(data[position].uid).child("User")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.println(Log.ASSERT, "error", "$error")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value.toString() == "Verified User")
                        holder.verified.visibility = View.VISIBLE
                }

            })
        holder.name.text = data[position].name
        database.child(data[position].uid).child("Email")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    holder.email.text = snapshot.value.toString()
                }

            })

        database.child(data[position].uid).child("Photo")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        Glide.with(holder.itemView.context).load(
                            snapshot
                                .value.toString()
                        ).into(holder.image)
                    } catch (e: Exception) { Log.println(Log.ASSERT, "Exception", "$e") }
                }
            })

        holder.itemView.setOnClickListener {
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("UID", data[position].uid)
            mContext.startActivity(intent)
            mContext as Activity
            mContext.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}