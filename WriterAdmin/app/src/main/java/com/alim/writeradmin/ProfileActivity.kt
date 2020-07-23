package com.alim.writeradmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Adapter.PostAdapter
import com.alim.writeradmin.Database.Settings
import com.alim.writeradmin.Model.IndexModel
import com.bumptech.glide.Glide
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.back

class ProfileActivity : AppCompatActivity() {

    val VU = "Verified User"
    val USER_DATA = "User Data"
    private lateinit var adapter: PostAdapter
    val profilePost: ArrayList<IndexModel> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}

        FirebaseDatabase.getInstance().getReference(USER_DATA)
            .child(intent.getStringExtra("UID")!!)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        profilePost.clear()
                        setData(snapshot)
                    } catch (e: Exception) { Log.println(Log.ASSERT,"Error", "$e") }
                }
            })

        FirebaseDatabase.getInstance().getReference(USER_DATA)
            .child(intent.getStringExtra("UID")!!).child("User")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        when {
                            snapshot.value.toString() == VU -> {
                                verified_logo.visibility = View.VISIBLE
                                verified_chip.isChecked = true
                            }
                            snapshot.value.toString() == "Blocked User" -> {
                                verified_logo.visibility = View.GONE
                                blocked_chip.isChecked = true
                            }
                            else -> {
                                verified_logo.visibility = View.GONE
                                regular_chip.isChecked = true
                            }
                        }
                    } catch (e: Exception) { Log.println(Log.ASSERT,"Error", "$e") }
                }
            })

        layoutManager = LinearLayoutManager(this)

        my_recycle.layoutManager = layoutManager
        adapter = PostAdapter(this, profilePost,
            FirebaseDatabase.getInstance().getReference(USER_DATA))
        my_recycle.adapter = adapter
    }

    private fun setData(snapshot: DataSnapshot) {
        Glide.with(this@ProfileActivity).load(snapshot.child("Photo")
            .value.toString()).into(profile_image)
        name.text = snapshot.child("Name").value.toString()
        email.text = snapshot.child("Email").value.toString()
        following.text = snapshot.child("Following").childrenCount.toString()
        followers.text = snapshot.child("Followers").childrenCount.toString()
        Thread {
            var like = 0
            for (x in snapshot.child("Post").children) {
                try {
                    if (x.key.toString() != "0") {
                        val model = IndexModel()
                        like += x.child("Likes").childrenCount.toInt()
                        model.uid = intent.getStringExtra("UID")!!
                        model.pos = x.key.toString()
                        model.category = x.child("Category").value.toString()
                        model.content = x.child("Content").value.toString()
                        model.approved = x.child("Verified").value.toString()
                        profilePost.add(model)
                        runOnUiThread {
                            adapter.notifyItemChanged(profilePost.size)
                        }
                    }
                } catch (e: Exception) {
                    Log.println(Log.ASSERT, "Exception", "$e")
                }
                Log.println(Log.ASSERT,"Likes", "$like")
            }
            runOnUiThread {
                likes.text = "$like"
                loading.visibility = View.GONE
                val p = snapshot.child("Post").childrenCount.toInt()
                post_total.text = if (p==0) "0"
                else (p-1).toString()
            }
        }.start()
        user_group.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.regular_chip -> changeUser("Regular User",
                    snapshot.child("Post").child("0").value.toString().toInt())
                R.id.verified_chip -> changeUser(VU,
                    snapshot.child("Post").child("0").value.toString().toInt())
                R.id.blocked_chip -> changeUser("Blocked User",
                    snapshot.child("Post").child("0").value.toString().toInt())
            }
        }
    }

    private fun changeUser(text: String, limit: Int) {
        FirebaseDatabase.getInstance()
            .getReference("User Data")
            .child(intent.getStringExtra("UID")!!)
            .child("User").setValue(text)
        for (x in 1..limit) {
            if (text == VU)
                FirebaseDatabase.getInstance().getReference(USER_DATA)
                    .child(intent.getStringExtra("UID")!!).child("Post")
                    .child("$x").child("Verified")
                    .setValue("True")
            else FirebaseDatabase.getInstance().getReference(USER_DATA)
                .child(intent.getStringExtra("UID")!!).child("Post")
                .child("$x").child("Verified")
                .setValue("False")
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }
}