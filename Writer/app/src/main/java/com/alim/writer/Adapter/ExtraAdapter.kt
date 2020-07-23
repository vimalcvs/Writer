package com.alim.writer.Adapter

import android.annotation.SuppressLint
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
import com.alim.writer.DataHolder.AdapterData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.DataHolder.PostData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.DetailsActivity
import com.alim.writer.Firebase.Indexer
import com.alim.writer.Interfaces.OnScroll
import com.alim.writer.Model.PostModel
import com.alim.writer.ProfileActivity
import com.alim.writer.R
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.ads.nativetemplates.TemplateView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.database.*

class ExtraAdapter(context: Context, cat: String, interstitial: InterstitialAd)
    : RecyclerView.Adapter<ExtraAdapter.ViewHolder>() {

    companion object {
        var insert = IndexData.index.size
        private var listenerObj: ArrayList<DatabaseReference> = ArrayList()
    }

    val USER_DATA = "User Data"
    private val category = cat
    private val NATIVE = 1
    val mInterstitialAd = interstitial
    private val mContext = context
    private val flat = ApplicationData(context).flat

    val iRef = FirebaseDatabase.getInstance().getReference("Index")
    private val myRef = FirebaseDatabase.getInstance().getReference(USER_DATA)

    private val cache = when (ApplicationData(context).cache) {
        true -> DiskCacheStrategy.AUTOMATIC
        false -> DiskCacheStrategy.NONE
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when {
            viewType == NATIVE -> {
                if (flat)
                    ViewHolder(
                        LayoutInflater.from(mContext).inflate(R.layout.ad_unified, parent, false)
                    )
                else
                    ViewHolder(
                        LayoutInflater.from(mContext).inflate(R.layout.ad_unified_card, parent, false)
                    )
            }
            flat -> {
                ViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.post_view_linear, parent, false)
                )
            }
            else -> {
                ViewHolder(
                    LayoutInflater.from(mContext).inflate(R.layout.post_view_card, parent, false)
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, pos: Int) {
        val postData = PostData()
        if (getItemViewType(pos) != NATIVE && IndexData.index[pos].category == category) {
            holder.itemView.visibility = View.VISIBLE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT)
            Log.println(Log.ASSERT,"POS", IndexData.index[pos].pos)
            val name = holder.itemView.findViewById<TextView>(R.id.name)
            val date = holder.itemView.findViewById<TextView>(R.id.date)
            val desc = holder.itemView.findViewById<TextView>(R.id.desc)
            val likes = holder.itemView.findViewById<TextView>(R.id.likes)
            val title = holder.itemView.findViewById<TextView>(R.id.title)
            val image = holder.itemView.findViewById<ImageView>(R.id.image)
            val comments = holder.itemView.findViewById<TextView>(R.id.comments)
            val profile = holder.itemView.findViewById<CircleImageView>(R.id.profile)
            val progress = holder.itemView.findViewById<ProgressBar>(R.id.image_loading)
            image.visibility = View.GONE
            progress.visibility = View.VISIBLE
            try {
                val temp = postData.getData()[pos]
                name.text = temp.name
                date.text = temp.date
                likes.text = temp.like.toString()
                title.text = temp.title
                comments.text = temp.comments.toString()
                desc.text = temp.description
                Glide.with(mContext).load(temp.profile)
                    .dontAnimate()
                    .diskCacheStrategy(cache)
                    .centerCrop().into(profile)
                Glide.with(mContext).load(temp.link)
                    .dontAnimate()
                    .diskCacheStrategy(cache)
                    .centerCrop().into(image)
            } catch (e: Exception) {
                val loading = "Loading..."
                name.text = loading
                date.text = loading
                likes.text = loading
                title.text = loading
                comments.text = loading
                desc.text = loading
            }

            if (!listenerObj.contains(
                    myRef.child(IndexData.index[pos].uid).child("Post")
                        .child(IndexData.index[pos].pos)
                )
            ) {
                myRef.child(IndexData.index[pos].uid).child("Post").child(IndexData.index[pos].pos)
                    .addValueEventListener(object : ValueEventListener {
                        val x = pos
                        override fun onCancelled(p0: DatabaseError) {
                            Toast.makeText(mContext, p0.message, Toast.LENGTH_SHORT).show()
                        }

                        override fun onDataChange(data: DataSnapshot) {
                            dataChanged(x, postData, data, holder)
                        }
                    })
            } else listenerObj.add(
                myRef.child(IndexData.index[pos].uid).child("Post").child(IndexData.index[pos].pos)
            )
        } else if (getItemViewType(pos) == NATIVE) {
            if (AdminData(mContext).nativeAdEnabled) {
                Thread {
                    val template = holder.itemView.findViewById<TemplateView>(R.id.my_template)
                    val styles = NativeTemplateStyle.Builder().build()
                    val adLoader =
                        AdLoader.Builder(mContext, AdminData(holder.itemView.context).nativeAd)
                            .forUnifiedNativeAd {
                                template.setStyles(styles)
                                template.setNativeAd(it)
                            }.withAdListener(object : AdListener() {
                                override fun onAdLoaded() {
                                    super.onAdLoaded()
                                    template.visibility = View.VISIBLE
                                }
                            })
                            .build()
                    adLoader.loadAd(AdRequest.Builder().build())
                }.start()
            } else {
                holder.itemView.visibility = View.GONE
                holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
            }
        } else {
            holder.itemView.visibility = View.GONE
            holder.itemView.layoutParams = RecyclerView.LayoutParams(0, 0)
        }
        //listener.add(listen)

        if (IndexData.index.size == pos + 1 && AdapterData.pos > 0) {
            insert = IndexData.index.size
            Indexer().index(mContext, AdapterData.pos, iRef, object : OnScroll {
                override fun scrolled() {
                    AdapterData.pos--
                    notifyItemInserted(insert)
                }
            })
            notifyDataSetChanged()
        }
        AdapterData.aPos = pos
    }

    override fun getItemViewType(position: Int): Int {
        return if ((position)%4==0 && position!=0) NATIVE
        else super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        Log.println(Log.ASSERT,"Item Extra", "${IndexData.index.size}")
        return IndexData.index.size
    }

    private fun dataChanged(x: Int, postData: PostData, data: DataSnapshot, holder: ViewHolder) {
        try {
            val name = holder.itemView.findViewById<TextView>(R.id.name)
            val date = holder.itemView.findViewById<TextView>(R.id.date)
            val desc = holder.itemView.findViewById<TextView>(R.id.desc)
            val play = holder.itemView.findViewById<ImageView>(R.id.play)
            val likes = holder.itemView.findViewById<TextView>(R.id.likes)
            val title = holder.itemView.findViewById<TextView>(R.id.title)
            val image = holder.itemView.findViewById<ImageView>(R.id.image)
            val thumb = holder.itemView.findViewById<ImageView>(R.id.thumb)
            val like = holder.itemView.findViewById<LinearLayout>(R.id.like)
            val comments = holder.itemView.findViewById<TextView>(R.id.comments)
            val comment = holder.itemView.findViewById<LinearLayout>(R.id.comment)
            val profile = holder.itemView.findViewById<CircleImageView>(R.id.profile)
            val progress = holder.itemView.findViewById<ProgressBar>(R.id.image_loading)
            val openProfile = holder.itemView.findViewById<FrameLayout>(R.id.open_profile)

            val postModel = PostModel()
            postModel.name = data.child("Name").value.toString()
            postModel.date = data.child("Date").value.toString()
            postModel.like = data.child("Likes").childrenCount.toInt()
            postModel.title = data.child("Title").value.toString()
            postModel.comments = data.child("Comments").value.toString().toInt()
            postModel.description = data.child("Desc").value.toString()

            name.text = postModel.name
            date.text = postModel.date
            likes.text = "${postModel.like}"
            title.text = postModel.title
            comments.text = "${postModel.comments}"
            desc.text = postModel.description

            holder.itemView.findViewById<TextView>(R.id.views).text =
                data.child("Views").childrenCount.toInt().toString()

            if (data.child("Likes").child(UserData(mContext).uid).exists()) {
                thumb.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        (R.drawable.ic_liked)
                    )
                )
                postModel.liked = 1
            } else {
                thumb.setImageDrawable(
                    ContextCompat.getDrawable(
                        mContext,
                        (R.drawable.ic_like)
                    )
                )
                postModel.liked = 0
            }
            try {
                postModel.profile = data.child("Photo").value.toString()
                Glide.with(mContext).load(postModel.profile)
                    .dontAnimate()
                    .diskCacheStrategy(cache)
                    .centerCrop().into(profile)
            } catch (e: Exception) {
                Log.println(Log.ASSERT, "Exception Recycler", e.toString())
            }
            try {
                postModel.link = data.child("Url").value.toString()
                postModel.content = data.child("Content").value.toString()
                if (postModel.content == "Image") {
                    Glide.with(mContext)
                        .load(postModel.link)
                        .dontAnimate()
                        .diskCacheStrategy(cache)
                        .centerCrop().into(image)
                    play.visibility = View.GONE
                } else if (postModel.content == "Youtube") {
                    Glide.with(mContext)
                        .load("https://img.youtube.com/vi/${postModel.link}/0.jpg")
                        .dontAnimate()
                        .diskCacheStrategy(cache)
                        .centerCrop().into(image)
                    play.visibility = View.VISIBLE
                }
                image.visibility = View.VISIBLE
                progress.visibility = View.GONE
            } catch (e: Exception) {
                Log.println(Log.ASSERT, "Exception Recycler", e.toString())
            }

            openProfile.setOnClickListener {
                val intent = Intent(mContext, ProfileActivity::class.java)
                intent.putExtra("UID", IndexData.index[x].uid)
                intent.putExtra("NAME", postModel.name)
                mContext.startActivity(intent)
            }

            like.setOnClickListener {
                val mRef =
                    FirebaseDatabase.getInstance().getReference(USER_DATA)
                        .child(IndexData.index[x].uid).child("Post")
                        .child(IndexData.index[x].pos)
                if (!data.child("Likes").child(UserData(mContext).uid)
                        .exists()
                ) {
                    mRef.child("Likes").child(UserData(mContext).uid)
                        .setValue(1)
                } else {
                    mRef.child("Likes").child(UserData(mContext).uid)
                        .removeValue()
                }
                mRef.child("Views").child(UserData(mContext).uid)
                    .setValue(true)
            }

            comment.setOnClickListener {
                val mRef =
                    FirebaseDatabase.getInstance().getReference(USER_DATA)
                        .child(IndexData.index[x].uid).child("Post")
                        .child(IndexData.index[x].pos)
                mRef.child("Views").child(UserData(mContext).uid)
                    .setValue(true)
                val intent = Intent(mContext, CommentActivity::class.java)
                intent.putExtra("LINK", data.child("Comment").value.toString())
                intent.putExtra("UID", IndexData.index[x].uid)
                intent.putExtra("POS", IndexData.index[x].pos)
                mContext.startActivity(intent)
            }

            holder.itemView.setOnClickListener {
                val intent = Intent(mContext, DetailsActivity::class.java)
                intent.putExtra("UID", IndexData.index[x].uid)
                intent.putExtra("POS", IndexData.index[x].pos)
                intent.putExtra("CATEGORY", IndexData.index[x].category)
                intent.putExtra("VIEWS", data.child("Views").childrenCount.toInt().toString())
                intent.putExtra("NAME", data.child("Name").value.toString())
                intent.putExtra("DATE", data.child("Date").value.toString())
                intent.putExtra("IMAGE", data.child("Url").value.toString())
                if (data.child("Likes").child(UserData(mContext).uid).exists())
                    intent.putExtra("LIKED", 1)
                else intent.putExtra("LIKED", 0)
                intent.putExtra("CONTENT", postModel.content)
                intent.putExtra("TITLE", data.child("Title").value.toString())
                intent.putExtra("LINK", data.child("Comment").value.toString())
                intent.putExtra("PHOTO", data.child("Photo").value.toString())
                intent.putExtra(
                    "LIKES",
                    data.child("Likes").childrenCount.toString()
                )
                intent.putExtra(
                    "DESCRIPTION",
                    data.child("Desc").value.toString()
                )
                intent.putExtra(
                    "COMMENTS",
                    data.child("Comments").value.toString()
                )
                val activity = mContext as Activity
                mContext.startActivity(intent)
                activity.overridePendingTransition(
                    R.anim.slide_in,
                    R.anim.slide_out
                )
                showAd()
            }
            postData.setData(postModel)
        } catch (e: Exception) {
            Log.println(Log.ASSERT, "Exception", "$e")
        }
    }

    private fun showAd() {
        if (AdminData(mContext).interstitialAdEnabled && mInterstitialAd.isLoaded
            && AdminData(mContext).interstitialInterval <= UserData(mContext).interval)
            mInterstitialAd.show()
        else if (UserData(mContext).interval < AdminData(mContext).interstitialInterval)
            UserData(mContext).interval++
    }
}