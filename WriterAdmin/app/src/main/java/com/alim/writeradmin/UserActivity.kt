package com.alim.writeradmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import androidx.core.os.HandlerCompat.postDelayed
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Adapter.UserAdapter
import com.alim.writeradmin.Database.Settings
import com.alim.writeradmin.Model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_user.*
import java.time.Clock

class UserActivity : AppCompatActivity() {

    lateinit var snapshotName: DataSnapshot
    private lateinit var adapter: UserAdapter
    var userData : ArrayList<UserModel> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}


        val database = FirebaseDatabase.getInstance()
        //progress = findViewById(R.id.loading)
        layoutManager = LinearLayoutManager(this)

        recycler.layoutManager = layoutManager
        adapter = UserAdapter(this, userData, database.getReference("User Data"))
        recycler.adapter = adapter

        database.getReference("User Index")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    snapshotName = snapshot
                    Thread {
                        for (x in snapshot.children) {
                            val userModel = UserModel()
                            userModel.name = x.value.toString()
                            userModel.uid = x.key.toString()
                            Log.println(Log.ASSERT, "UID", userModel.uid)
                            userData.add(userModel)
                            runOnUiThread {
                                adapter.notifyItemInserted(userData.size)
                            }
                        }
                    }.start()
                }
            })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }
}