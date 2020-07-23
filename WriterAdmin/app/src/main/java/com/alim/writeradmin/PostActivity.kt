package com.alim.writeradmin

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Adapter.PostAdapter
import com.alim.writeradmin.Adapter.UserAdapter
import com.alim.writeradmin.Database.Settings
import com.alim.writeradmin.Model.IndexModel
import com.alim.writeradmin.Model.UserModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user.*

class PostActivity : AppCompatActivity() {

    private lateinit var adapter: PostAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}

        layoutManager = LinearLayoutManager(this)

        recycler.layoutManager = layoutManager
        adapter = PostAdapter(this, intent.getParcelableArrayListExtra<IndexModel>("INDEX")!!,
            FirebaseDatabase.getInstance().getReference("User Data"))
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }
}