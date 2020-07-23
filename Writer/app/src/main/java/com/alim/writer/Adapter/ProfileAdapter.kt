package com.alim.writer.Adapter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Class.CircleImageView
import com.alim.writer.CommentActivity
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.DataHolder.ProfileData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.DetailsActivity
import com.alim.writer.Model.PostModel
import com.alim.writer.ProfileActivity
import com.alim.writer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileAdapter(context: Context, interstitial: InterstitialAd)
    : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    private val mContext = context
    val mInterstitialAd = interstitial
    private val flat = ApplicationData(context).flat
    private val data = ProfileData().getData()

    private val cache = when(ApplicationData(context).cache) {
        true -> DiskCacheStrategy.AUTOMATIC
        false -> DiskCacheStrategy.NONE
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name = view.findViewById<TextView>(R.id.name)
        val play = view.findViewById<ImageView>(R.id.play)
        val date = view.findViewById<TextView>(R.id.date)
        val desc = view.findViewById<TextView>(R.id.desc)
        val views = view.findViewById<TextView>(R.id.views)
        val likes = view.findViewById<TextView>(R.id.likes)
        val title = view.findViewById<TextView>(R.id.title)
        val image = view.findViewById<ImageView>(R.id.image)
        val thumb = view.findViewById<ImageView>(R.id.thumb)
        val like = view.findViewById<LinearLayout>(R.id.like)
        val comments = view.findViewById<TextView>(R.id.comments)
        val comment = view.findViewById<LinearLayout>(R.id.comment)
        val profile = view.findViewById<CircleImageView>(R.id.profile)
        val progress = view.findViewById<ProgressBar>(R.id.image_loading)
        val category_title = view.findViewById<TextView>(R.id.category_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileAdapter.ViewHolder {
        return if (flat) ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_view_linear, parent, false))
        else ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.post_view_card, parent, false))
    }

    override fun onBindViewHolder(holder: ProfileAdapter.ViewHolder, position: Int) {
        val temp = data[position]
        holder.image.visibility = View.GONE
        holder.progress.visibility = View.VISIBLE
        holder.category_title.visibility = View.VISIBLE
        try {
            holder.name.text = temp.name
            holder.date.text = temp.date
            holder.title.text = temp.title
            holder.desc.text = temp.description
            holder.likes.text = temp.like.toString()
            holder.views.text = temp.views.toString()
            holder.comments.text = temp.comments.toString()
            holder.category_title.text = temp.category
            Glide.with(mContext).load(temp.profile)
                .dontAnimate()
                .diskCacheStrategy(cache)
                .centerCrop().into(holder.profile)
            Glide.with(mContext).load(temp.link)
                .dontAnimate()
                .diskCacheStrategy(cache)
                .centerCrop().into(holder.image)
        } catch (e: Exception) {
            val loading = "Loading..."
            holder.name.text = loading
            holder.date.text = loading
            holder.likes.text = loading
            holder.title.text = loading
            holder.comments.text = loading
            holder.desc.text = loading
        }

        if (temp.liked == 1) {
            holder.thumb.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    (R.drawable.ic_liked)
                )
            )
        } else {
            holder.thumb.setImageDrawable(
                ContextCompat.getDrawable(
                    mContext,
                    (R.drawable.ic_like)
                )
            )
        }
        try {
            Glide.with(mContext).load(temp.profile)
                .dontAnimate()
                .diskCacheStrategy(cache)
                .centerCrop().into(holder.profile)
        } catch (e: Exception) {
            Log.println(Log.ASSERT, "Exception Recycler", e.toString())
        }
        try {
            if (temp.content == "Image") {
                Glide.with(mContext)
                    .load(temp.link)
                    .dontAnimate()
                    .diskCacheStrategy(cache)
                    .centerCrop().into(holder.image)
                holder.play.visibility = View.GONE
            } else if (temp.content == "Youtube") {
                Glide.with(mContext)
                    .load("https://img.youtube.com/vi/${temp.link}/0.jpg")
                    .dontAnimate()
                    .diskCacheStrategy(cache)
                    .centerCrop().into(holder.image)
                holder.play.visibility = View.VISIBLE
            }
            holder.image.visibility = View.VISIBLE
            holder.progress.visibility = View.GONE
        } catch (e: Exception) {
            Log.println(Log.ASSERT, "Exception Recycler", e.toString())
        }

        holder.like.setOnClickListener {
            val mRef = FirebaseDatabase.getInstance().getReference("User Data")
                    .child(temp.uid).child("Post")
                    .child(temp.pos)
            if (temp.liked != 1) {
                mRef.child("Likes").child(UserData(mContext).uid).setValue(1)
                temp.liked = 1
            } else {
                mRef.child("Likes").child(UserData(mContext).uid)
                    .removeValue()
                temp.liked = 0
            }
            mRef.child("Views").child(UserData(mContext).uid)
                .setValue(true)
        }

        holder.comment.setOnClickListener {
            val mRef = FirebaseDatabase.getInstance().getReference("User Data")
                .child(temp.uid).child("Post")
                .child(temp.pos)
            mRef.child("Views").child(UserData(mContext).uid)
                .setValue(true)
            val intent = Intent(mContext, CommentActivity::class.java)
            intent.putExtra("LINK", temp.comment)
            intent.putExtra("UID", temp.uid)
            intent.putExtra("POS", temp.pos)
            mContext.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            if (AdminData(mContext).interstitialAdEnabled && mInterstitialAd.isLoaded
                && AdminData(mContext).interstitialInterval <= UserData(mContext).interval)
                mInterstitialAd.show()
            else if (UserData(mContext).interval < AdminData(mContext).interstitialInterval)
                UserData(mContext).interval++
            val intent = Intent(mContext, DetailsActivity::class.java)
            intent.putExtra("UID", temp.uid)
            intent.putExtra("POS", temp.pos)
            intent.putExtra("NAME", temp.name)
            intent.putExtra("DATE", temp.date)
            intent.putExtra("IMAGE", temp.link)
            intent.putExtra("LIKED", temp.liked)
            intent.putExtra("CONTENT", temp.content)
            intent.putExtra("TITLE", temp.title)
            intent.putExtra("LINK", temp.comment)
            intent.putExtra("PHOTO", temp.profile)
            intent.putExtra("LIKES", temp.like.toString())
            intent.putExtra("DESCRIPTION", temp.description)
            intent.putExtra("COMMENTS", temp.comments.toString())
            val activity = mContext as Activity
            mContext.startActivity(intent)
            activity.overridePendingTransition(
                R.anim.slide_in,
                R.anim.slide_out
            )
        }
    }

    override fun getItemCount(): Int {
        return data.size
    }
}