package com.alim.writer

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.CommentAdapter
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.Model.CommentModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.snackbar.Snackbar
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_details.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class DetailsActivity : YouTubeBaseActivity() {

    var loop = -1
    lateinit var nRef: DatabaseReference
    lateinit var cache: DiskCacheStrategy
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentAdapter
    var commentData : ArrayList<CommentModel> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val thread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                commentData.clear()
                loop = data.child("0").value.toString().toInt()
                findViewById<TextView>(R.id.comments).text = "$loop"
                for (p in 1 .. loop) {
                    if (data.child("$p").exists()) {
                        val commentModel = CommentModel()
                        commentModel.uid = data.child("$p").child("UID").value.toString()
                        commentModel.name = data.child("$p").child("Name").value.toString()
                        commentModel.date = data.child("$p").child("Date").value.toString()
                        commentModel.photo = data.child("$p").child("Photo").value.toString()
                        commentModel.comment = data.child("$p").child("Comment").value.toString()
                        commentData.add(commentModel)
                    }
                }
                runOnUiThread {
                    nRef.child("Comments").setValue(data.childrenCount.toInt()-1)
                    findViewById<NestedScrollView>(R.id.scroll_view).fullScroll(View.FOCUS_DOWN)
                    adapter.notifyItemInserted(loop)
                }
                Log.println(Log.ASSERT,"DATA", "$data")
            }
        }).listen(myRef,0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        cache = when(ApplicationData(this).cache) {
            true -> DiskCacheStrategy.AUTOMATIC
            false -> DiskCacheStrategy.NONE
        }
        toolbar.setNavigationOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
        }

        toolbar.menu.getItem(0).isVisible =
            intent.getStringExtra("UID") == UserData(this).uid

        toolbar.setOnMenuItemClickListener {
            val editActivity  = Intent(this, EditActivity::class.java)
            editActivity.putExtra("CONTENT", intent.getStringExtra("CONTENT"))
            editActivity.putExtra("UID", intent.getStringExtra("UID"))
            editActivity.putExtra("PHOTO", intent.getStringExtra("PHOTO"))
            editActivity.putExtra("NAME", intent.getStringExtra("NAME"))
            editActivity.putExtra("DATE", intent.getStringExtra("DATE"))
            editActivity.putExtra("TITLE", intent.getStringExtra("TITLE"))
            editActivity.putExtra("DESCRIPTION", intent.getStringExtra("DESCRIPTION"))
            editActivity.putExtra("POS",  intent.getStringExtra("POS"))
            startActivity(editActivity)
            overridePendingTransition(
                R.anim.slide_in,
                R.anim.slide_out
            )
            finish()
            true
        }

        val youTubePlayerView: YouTubePlayerView = findViewById(R.id.player)
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("Comments").child(intent.getStringExtra("LINK")!!)

        checkContent(youTubePlayerView)

        val edit = findViewById<EditText>(R.id.comment_edit)
        var liked = intent.getIntExtra("LIKED", 0)
        var like =  intent.getStringExtra("LIKES")!!.toInt()
        val likes = findViewById<TextView>(R.id.likes)
        val thumb = findViewById<ImageView>(R.id.thumb)

        //Primary Data
        nRef = FirebaseDatabase.getInstance().getReference("User Data")
            .child(intent.getStringExtra("UID")!!).child("Post").child(intent.getStringExtra("POS")!!)
        Glide.with(this).load(intent.getStringExtra("PHOTO"))
            .dontAnimate()
            .diskCacheStrategy(cache)
            .centerCrop().into(findViewById(R.id.profile))
        toolbar.title = intent.getStringExtra("NAME")
        //VIEWS
        findViewById<TextView>(R.id.views).text = intent.getStringExtra("VIEWS")
        findViewById<TextView>(R.id.category_title).text = intent.getStringExtra("CATEGORY")
        findViewById<TextView>(R.id.name).text = intent.getStringExtra("NAME")
        findViewById<TextView>(R.id.date).text = intent.getStringExtra("DATE")
        likes.text = intent.getStringExtra("LIKES")
        findViewById<TextView>(R.id.title).text = intent.getStringExtra("TITLE")
        findViewById<TextView>(R.id.desc).text = intent.getStringExtra("DESCRIPTION")
        findViewById<TextView>(R.id.comments).text = intent.getStringExtra("COMMENTS")
        if (intent.getIntExtra("LIKED", 0) == 1)
            thumb.setImageDrawable(ContextCompat.getDrawable(this, (R.drawable.ic_liked)))
        Log.println(Log.ASSERT,"Liked", intent.getIntExtra("LIKED", 0).toString())
        findViewById<LinearLayout>(R.id.like).setOnClickListener {
            if (liked == 0) {
                like++
                liked = 1
                likes.text = "$like"
                nRef.child("Likes").child(UserData(this).uid).setValue(1)
                thumb.setImageDrawable(ContextCompat.getDrawable(this, (R.drawable.ic_liked)))
            } else {
                like--
                liked = 0
                likes.text = "$like"
                nRef.child("Likes").child(UserData(this).uid).removeValue()
                thumb.setImageDrawable(ContextCompat.getDrawable(this, (R.drawable.ic_like)))
            }
            nRef.child("Views").child(UserData(this).uid).setValue(true)
        }
        findViewById<LinearLayout>(R.id.comment).setOnClickListener {
            edit.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(edit, InputMethodManager.SHOW_FORCED)
        }

        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.comment_recycler)
        recyclerView.suppressLayout(true)

        recyclerView.layoutManager = layoutManager
        adapter = CommentAdapter(this, commentData)
        recyclerView.adapter = adapter

        findViewById<NestedScrollView>(R.id.scroll_view)
            .isVerticalScrollBarEnabled = ApplicationData(this).scrollbar

        findViewById<FrameLayout>(R.id.open_profile).setOnClickListener {
            val profile = Intent(this, ProfileActivity::class.java)
            profile.putExtra("UID", intent.getStringExtra("UID"))
            profile.putExtra("NAME",intent.getStringExtra("NAME"))
            startActivity(profile)
        }

        findViewById<ImageView>(R.id.image).setOnClickListener {
            val im = Intent(this, ImageActivity::class.java)
            im.putExtra("IMAGE", intent.getStringExtra("IMAGE"))
            startActivity(im)
        }

        if (intent.getStringExtra("VERIFIED")=="True")
            findViewById<ImageView>(R.id.verified_logo).visibility = View.VISIBLE

        findViewById<Button>(R.id.send_comment).setOnClickListener {
            val com = edit.text.toString()
            if(AdminData(this).commentsEnabled) {
                when {
                    com.isEmpty() -> edit.error = "Write Something"
                    loop > -1 -> {
                        loop++
                        val userData = UserData(this)
                        myRef.child("$loop").child("Name").setValue(userData.name)
                        myRef.child("$loop").child("Photo").setValue(userData.image)
                        myRef.child("$loop").child("UID").setValue(userData.uid)
                        myRef.child("$loop").child("Date").setValue(
                            SimpleDateFormat("dd MMM yyyy").format(Calendar.getInstance().time)
                                .toString()
                        )
                        myRef.child("$loop").child("Comment").setValue(com)
                        myRef.child("0").setValue(loop)
                        database.getReference("User Data").child(intent.getStringExtra("UID")!!)
                            .child("Post").child(intent.getStringExtra("POS")!!)
                            .child("Comments").setValue(loop)
                        edit.setText("")
                        edit.clearFocus()
                        val imm =
                            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
                    }
                    else -> Toast.makeText(
                        this, "Something gone wrong please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else Snackbar.make(it, "Comments are disabled", Snackbar.LENGTH_INDEFINITE).show()
        }

        nRef.child("Views").child(UserData(this).uid).setValue(true)

        if (AdminData(this).bannerAdEnabled) {
            val adView = AdView(this)
            adView.adSize = AdSize.BANNER
            adView.adUnitId = AdminData(this).bannerAd
            banner_layout.addView(adView)

            val adRequest: AdRequest = AdRequest.Builder().build()

            adView.loadAd(adRequest)
        }

        Thread(thread).start()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }

    private fun checkContent(youTubePlayerView: YouTubePlayerView) {
        if (intent.getStringExtra("CONTENT")=="Youtube") {
            findViewById<ImageView>(R.id.image).visibility = View.GONE
            youTubePlayerView.initialize("YOUR API KEY",
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(
                        p0: YouTubePlayer.Provider?,
                        p1: YouTubePlayer?,
                        p2: Boolean) {
                        p1!!.cueVideo(intent.getStringExtra("IMAGE"))
                    }

                    override fun onInitializationFailure(
                        p0: YouTubePlayer.Provider?,
                        p1: YouTubeInitializationResult?
                    ) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            youTubePlayerView.visibility = View.GONE
            Glide.with(this).load(intent.getStringExtra("IMAGE"))
                .dontAnimate()
                .diskCacheStrategy(cache)
                .centerCrop().into(findViewById(R.id.image))
        }
    }
}