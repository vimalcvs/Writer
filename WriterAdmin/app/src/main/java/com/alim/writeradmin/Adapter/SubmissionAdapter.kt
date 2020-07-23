package com.alim.writeradmin.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.DetailsActivity
import com.alim.writeradmin.Interface.Submission
import com.alim.writeradmin.Model.IndexModel
import com.alim.writeradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.post_view_sub.view.*

class SubmissionAdapter(
    context: Context, mData: ArrayList<IndexModel>, mSubmission: Submission,
    mDatabase: DatabaseReference) : RecyclerView.Adapter<SubmissionAdapter.ViewHolder>(){

    private val data = mData
    private val mContext = context
    private val database = mDatabase
    private val submission = mSubmission

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_view_sub, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val l = holder.itemView
        val d = data[position]
        database.child(d.uid).child("Post").child(d.pos)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    l.image_loading.visibility = View.GONE
                    if (snapshot.child("Verified").value.toString()=="True")
                        l.verified_logo.visibility = View.VISIBLE
                    else l.verified_logo.visibility = View.GONE
                    l.title.text = snapshot.child("Title").value.toString()
                    l.desc.text = snapshot.child("Desc").value.toString()
                    l.name.text = snapshot.child("Name").value.toString()
                    l.date.text = snapshot.child("Date").value.toString()
                    l.category_title.text = snapshot.child("Category").value.toString()
                    l.views.text = snapshot.child("Views").childrenCount.toInt().toString()
                    Glide.with(mContext).load(snapshot.child("Photo").value.toString()).into(l.profile)
                    if (snapshot.child("Content").value.toString()=="Youtube") {
                        Glide.with(mContext)
                            .load("https://img.youtube.com/vi/${snapshot.child("Url").value}/0.jpg")
                            .centerCrop().into(l.image)
                        l.play.visibility = View.VISIBLE
                    }
                    else {
                        l.play.visibility = View.GONE
                        Glide.with(mContext).load(snapshot.child("Url").value.toString())
                            .centerCrop().into(l.image)
                    }
                    l.approve_post.setOnClickListener {
                        AlertDialog.Builder(mContext)
                            .setTitle("Post Approval")
                            .setMessage("Are you sure want to Approve post from ${snapshot.child("Name").value} ?")
                            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                            .setPositiveButton("Yes") { dialog, _ ->
                                FirebaseDatabase.getInstance().getReference("Index").child(""+d.x)
                                    .child(""+d.y).child("Approved").setValue("True")
                                submission.approved(position)
                                dialog.dismiss()
                            }.show()
                    }

                    holder.itemView.setOnClickListener {
                        submission.details(snapshot, d.uid, position, d.x, d.y, d.pos)
                    }
                }
            })
    }

    override fun getItemCount(): Int {
        return data.size
    }
}