package com.alim.writeradmin.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.DetailsActivity
import com.alim.writeradmin.Model.IndexModel
import com.alim.writeradmin.R
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.post_view_linear.view.*

class PostAdapter(
    context: Context, mData: ArrayList<IndexModel>, mDatabase: DatabaseReference)
    : RecyclerView.Adapter<PostAdapter.ViewHolder>(){

    private val data = mData
    private val mContext = context
    private val database = mDatabase

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(mContext).inflate(R.layout.post_view_linear, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val l = holder.itemView
        val d = data[data.size-position-1]
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
                    l.comments.text = snapshot.child("Comments").value.toString()
                    l.likes.text = snapshot.child("Likes").childrenCount.toInt().toString()
                    l.category_title.text = snapshot.child("Category").value.toString()
                    l.views.text = snapshot.child("Views").childrenCount.toInt().toString()
                    Glide.with(mContext).load(snapshot.child("Photo").value.toString()).into(l.profile)
                    try {
                        if (snapshot.child("Content").value.toString() == "Youtube") {
                            Glide.with(mContext)
                                .load("https://img.youtube.com/vi/${snapshot.child("Url").value}/0.jpg")
                                .centerCrop().into(l.image)
                            l.play.visibility = View.VISIBLE
                        } else {
                            l.play.visibility = View.GONE
                            Glide.with(mContext).load(snapshot.child("Url").value.toString())
                                .centerCrop().into(l.image)
                        }
                    } catch (E: Exception) { Log.println(Log.ASSERT, "Ex", "$E") }

                    holder.itemView.setOnClickListener {
                        val details = Intent(mContext, DetailsActivity::class.java)
                        details.putExtra("UID", d.uid)
                        details.putExtra("POS", d.pos)
                        details.putExtra("LIKES", snapshot.child("Likes").childrenCount.toInt().toString())
                        details.putExtra("PHOTO", snapshot.child("Photo").value.toString())
                        details.putExtra("NAME", snapshot.child("Name").value.toString())
                        details.putExtra("DATE", snapshot.child("Date").value.toString())
                        details.putExtra("DESC", snapshot.child("Desc").value.toString())
                        details.putExtra("TITLE", snapshot.child("Title").value.toString())
                        details.putExtra("URL", snapshot.child("Url").value.toString())
                        details.putExtra("CONTENT", snapshot.child("Content").value.toString())
                        details.putExtra("VIEWS", snapshot.child("Views").childrenCount.toInt().toString())
                        details.putExtra("COMMENTS", snapshot.child("Comments").value.toString())
                        details.putExtra("CATEGORY", snapshot.child("Category").value.toString())
                        details.putExtra("LINK", snapshot.child("Comment").value.toString())
                        mContext.startActivity(details)
                        mContext as Activity
                        mContext.overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }
                }
            })
    }

    override fun getItemCount(): Int {
        return data.size
    }
}