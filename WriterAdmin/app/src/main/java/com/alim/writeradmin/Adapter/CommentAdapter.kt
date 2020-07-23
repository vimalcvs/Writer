package com.alim.writer.Adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Class.CircleImageView
import com.alim.writer.Model.CommentModel
import com.alim.writeradmin.ProfileActivity
import com.alim.writeradmin.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class CommentAdapter(context: Context, mData: ArrayList<CommentModel>, ref: DatabaseReference)
    : RecyclerView.Adapter<CommentAdapter.ViewHolder>(){

    private val myRef = ref
    private val mContext = context
    private val data = mData

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val date = view.findViewById<TextView>(R.id.date)
        val comment = view.findViewById<TextView>(R.id.comment)
        val profile = view.findViewById<CircleImageView>(R.id.profile)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.comment_layout, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fData = data[position]
        holder.name.text = fData.name
        holder.date.text = fData.date
        holder.comment.text = fData.comment

        try {
            Glide.with(mContext).load(fData.photo)
                .dontAnimate()
                .centerCrop().into(holder.profile)
        } catch (e: Exception) {
            Log.println(Log.ASSERT,"Exception Comment", "$e")
        }

        holder.profile.setOnClickListener {
            val intent = Intent(mContext, ProfileActivity::class.java)
            intent.putExtra("UID", fData.uid)
            intent.putExtra("NAME", fData.name)
            mContext.startActivity(intent)
        }

        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(mContext)
                .setTitle("Remove comment")
                .setMessage("Are you sure want to remove the comment from ${fData.name}" +
                        "\n\nComment : ${fData.comment}")
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Yes") { dialog, _ ->
                    Log.println(Log.ASSERT,"POS", "$position")
                    myRef.child("${fData.pos}").removeValue()
                    dialog.dismiss()
                }.show()
            true
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}