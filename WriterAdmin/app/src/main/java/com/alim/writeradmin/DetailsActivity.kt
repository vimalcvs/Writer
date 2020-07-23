package com.alim.writeradmin

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.CommentAdapter
import com.alim.writer.Model.CommentModel
import com.alim.writeradmin.Database.Settings
import com.bumptech.glide.Glide
import com.google.android.youtube.player.YouTubeBaseActivity
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_details.*
import kotlinx.android.synthetic.main.activity_settings.back

class DetailsActivity : YouTubeBaseActivity() {

    var loop = -1
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    private lateinit var adapter: CommentAdapter
    var commentData : ArrayList<CommentModel> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val thread = Thread {

        myRef.addValueEventListener(object: ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
            override fun onDataChange(data: DataSnapshot) {
                commentData.clear()
                loop = data.child("0").value.toString().toInt()
                comments.text = "$loop"
                adapter.notifyDataSetChanged()
                for (x in 1 .. loop) {
                    if (data.child("$x").exists()) {
                        val commentModel = CommentModel()
                        commentModel.pos = x
                        commentModel.uid = data.child("$x").child("UID").value.toString()
                        commentModel.name = data.child("$x").child("Name").value.toString()
                        commentModel.date = data.child("$x").child("Date").value.toString()
                        commentModel.photo = data.child("$x").child("Photo").value.toString()
                        commentModel.comment = data.child("$x").child("Comment").value.toString()
                        commentData.add(commentModel)
                    }
                }
                runOnUiThread {
                    scroll_view.fullScroll(View.FOCUS_DOWN)
                    adapter.notifyItemInserted(commentData.size-1)
                }
                Log.println(Log.ASSERT,"DATA", "$data")
            }

        })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}

        if (intent.getBooleanExtra("SUBMISSION", false))
            edit_post.visibility = View.VISIBLE

        edit_post.setOnClickListener {
            edit_post.visibility = View.GONE
            val goBack = Intent()
            setResult(Activity.RESULT_OK, goBack)
        }

        Glide.with(this).load(intent.getStringExtra("PHOTO")!!).into(profile)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("Comments").child(intent.getStringExtra("LINK")!!)
        layoutManager = LinearLayoutManager(this)
        comment_recycler.suppressLayout(true)
        comment_recycler.layoutManager = layoutManager
        adapter = CommentAdapter(this, commentData, myRef)
        comment_recycler.adapter = adapter
        Thread(thread).start()

        name.text = intent.getStringExtra("NAME")
        date.text = intent.getStringExtra("DATE")
        desc.text = intent.getStringExtra("DESC")
        likes.text = intent.getStringExtra("LIKES")
        views.text = intent.getStringExtra("VIEWS")
        p_title.text = intent.getStringExtra("TITLE")
        comments.text = intent.getStringExtra("COMMENTS")
        category_title.text = intent.getStringExtra("CATEGORY")

        open_profile.setOnClickListener {
            val profile = Intent(this, ProfileActivity::class.java)
            profile.putExtra("UID", intent.getStringExtra("UID"))
            startActivity(profile)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }

        edit_fab.setOnClickListener {
            val editIntent = Intent(this, EditActivity::class.java)
            editIntent.putExtra("UID", intent.getStringExtra("UID"))
            editIntent.putExtra("POS", intent.getStringExtra("POS"))
            editIntent.putExtra("PHOTO", intent.getStringExtra("PHOTO"))
            editIntent.putExtra("NAME", intent.getStringExtra("NAME"))
            editIntent.putExtra("TITLE", intent.getStringExtra("TITLE"))
            editIntent.putExtra("DATE", intent.getStringExtra("DATE"))
            editIntent.putExtra("DESCRIPTION", intent.getStringExtra("DESC"))
            startActivity(editIntent)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }

        checkContent(player)
    }

    private fun checkContent(youTubePlayerView: YouTubePlayerView) {
        if (intent.getStringExtra("CONTENT")=="Youtube") {
            image.visibility = View.GONE
            youTubePlayerView.initialize("YOUR API KEY",
                object : YouTubePlayer.OnInitializedListener {
                    override fun onInitializationSuccess(
                        p0: YouTubePlayer.Provider?,
                        p1: YouTubePlayer?,
                        p2: Boolean) {
                        p1!!.cueVideo(intent.getStringExtra("URL"))
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
            Glide.with(this).load(intent.getStringExtra("URL"))
                .dontAnimate()
                .centerCrop().into(findViewById(R.id.image))
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }
}