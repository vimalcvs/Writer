package com.alim.writer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.ProfileAdapter
import com.alim.writer.DataHolder.ProfileData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.FollowingData
import com.alim.writer.Database.UserData
import com.alim.writer.Model.PostModel
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileActivity : AppCompatActivity() {

    var totalLike = 0
    val USER_DATA = "User Data"
    lateinit var follow: Button
    lateinit var email: TextView
    lateinit var ver: FrameLayout
    lateinit var toolbar: Toolbar
    lateinit var userData: UserData
    lateinit var progress: ProgressBar
    private val following = "Following"
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    lateinit var recyclerView: RecyclerView
    lateinit var listener: ValueEventListener
    private lateinit var adapter: ProfileAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private fun setText(d: DataSnapshot) {
        findViewById<TextView>(R.id.name).text = d.child("Name").value.toString()
        findViewById<TextView>(R.id.following).text =
            if (d.child(following).exists()) {
                d.child(following).childrenCount.toString()
            } else "0"
        findViewById<TextView>(R.id.followers).text =
            if (d.child("Followers").exists()) {
                if (d.child("Followers").child(userData.uid).exists())
                    follow.text = "Following"
                d.child("Followers").childrenCount.toString()
            } else "0"
        findViewById<TextView>(R.id.likes).text =
            if (d.child("Likes").exists()) d.child("Likes").value.toString()
            else "0"
    }

    private val thread = Thread {
        listener = myRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Toast.makeText(this@ProfileActivity,p0.message,Toast.LENGTH_SHORT).show()
            }

            override fun onDataChange(d: DataSnapshot) {
                email.text = d.child("Email").value.toString()
                toolbar.title = d.child("Name").value.toString()
                setText(d)
                Glide.with(this@ProfileActivity).load(d.child("Photo").value.toString())
                    .into(findViewById(R.id.profile_image))
                if (d.child("User").exists()) {
                    if (d.child("User").value.toString() == "Verified User")
                        ver.visibility = View.VISIBLE
                    else
                        ver.visibility = View.GONE
                }
                val data = d.child("Post")
                val loop = try { data.child("0").value.toString().toInt() }
                catch (e: Exception) { 0 }
                totalLike = 0
                ProfileData().clear()
                if (loop==0) {
                    runOnUiThread {
                        findViewById<TextView>(R.id.post_total).text = "0"
                        findViewById<TextView>(R.id.likes).text = "0"
                        progress.visibility = View.GONE
                        adapter.notifyDataSetChanged()
                    }
                }
                for ((post, x) in (loop downTo 1).withIndex()) {
                    try {
                        val model = PostModel()
                        val d = data.child("$x")
                        model.title = d.child("Title").value.toString()
                        model.date = d.child("Date").value.toString()
                        model.content = d.child("Content").value.toString()
                        model.description = d.child("Desc").value.toString()
                        model.link = d.child("Url").value.toString()
                        model.name = d.child("Name").value.toString()
                        model.profile = d.child("Photo").value.toString()
                        model.uid = intent.getStringExtra("UID")!!
                        model.pos = x.toString()
                        model.views = d.child("Views").childrenCount.toInt()
                        model.category = d.child("Category").value.toString()
                        model.comment = d.child("Comment").value.toString()
                        model.like = d.child("Likes").childrenCount.toInt()
                        model.comments = d.child("Comments").value.toString().toInt()
                        if (d.child("Likes").child(userData.uid).exists()) model.liked = 1

                        ProfileData().setData(model)

                        totalLike += d.child("Likes").childrenCount.toInt()
                        Log.println(Log.ASSERT, "Notify", "True")
                        runOnUiThread {
                            findViewById<TextView>(R.id.post_total).text = "$loop"
                            findViewById<TextView>(R.id.likes).text = "$totalLike"
                            progress.visibility = View.GONE
                            adapter.notifyDataSetChanged()
                        }
                    } catch(e: Exception) {
                        Log.println(Log.ASSERT,"Exception","$e")
                    }
                }
            }
        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        ProfileData().clear()

        val id = intent.getStringExtra("UID")
        toolbar.title = intent.getStringExtra("NAME")
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference(USER_DATA).child(id!!)
        Log.println(Log.ASSERT, "UID", id)

        userData = UserData(this)

        follow = findViewById(R.id.follow)
        email = findViewById(R.id.email)
        ver = findViewById(R.id.verified_logo)

        if (id == FirebaseAuth.getInstance().currentUser!!.uid) follow.visibility = View.GONE

        if (AdminData(this).interstitialAdEnabled) {
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = AdminData(this).interstitialAd
            mInterstitialAd.loadAd(AdRequest.Builder().build())
        }

        follow.setOnClickListener {
            if (follow.text.toString() == "Follow") {
                database.getReference(USER_DATA).child(userData.uid)
                    .child(following).child(id).setValue(true)
                myRef.child("Followers").child(userData.uid)
                    .setValue(true)
                FollowingData(this).setFollowing(id, true)
                follow.text = "Following"
            } else {
                database.getReference("User Data").child(userData.uid)
                    .child(following).child(id).removeValue()
                myRef.child("Followers").child(userData.uid)
                    .removeValue()
                FollowingData(this).setFollowing(id, false)
                follow.text = "Follow"
            }
        }

        findViewById<LinearLayout>(R.id.following_button).setOnClickListener {
            val follow = Intent(this, FollowActivity::class.java)
            follow.putExtra("TITLE","Following")
            follow.putExtra("UID", id)
            startActivity(follow)
        }

        findViewById<LinearLayout>(R.id.followers_button).setOnClickListener {
            val follow = Intent(this, FollowActivity::class.java)
            follow.putExtra("TITLE","Followers")
            follow.putExtra("UID", id)
            startActivity(follow)
        }

        progress = findViewById(R.id.loading)
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.my_recycle)

        recyclerView.layoutManager = layoutManager
        adapter = ProfileAdapter(this, mInterstitialAd)
        recyclerView.adapter = adapter

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                UserData(this@ProfileActivity).interval = 0
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                super.onAdClosed()
            }
        }

        Thread(thread).start()
    }

    override fun onDestroy() {
        myRef.removeEventListener(listener)
        super.onDestroy()
    }
}