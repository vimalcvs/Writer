package com.alim.writer

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.FollowAdapter
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class FollowActivity : AppCompatActivity() {

    lateinit var follow: Button
    lateinit var recyclerView: RecyclerView
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    private lateinit var adapter: FollowAdapter
    var uidList: ArrayList<String> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val thread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                for (childDataSnapshot : DataSnapshot in data.children) {
                    try { uidList.add(childDataSnapshot.key!!) }
                    catch (e: Exception) { Log.println(Log.ASSERT,"Exception","$e") }
                }
                adapter.notifyItemInserted(0)
            }

        }).listenOnce(myRef.child(intent.getStringExtra("UID")!!)
            .child(intent.getStringExtra("TITLE")!!))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = intent.getStringExtra("TITLE")

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("User Data")
        recyclerView = findViewById(R.id.follow_recycler)

        //progress = findViewById(R.id.loading)
        layoutManager = LinearLayoutManager(this)

        recyclerView.layoutManager = layoutManager
        adapter = FollowAdapter(this, uidList)

        recyclerView.adapter = adapter

        Thread(thread).start()
    }
}