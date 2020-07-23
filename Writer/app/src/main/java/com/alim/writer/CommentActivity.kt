package com.alim.writer

import android.annotation.SuppressLint
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.CommentAdapter
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.Model.CommentModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class CommentActivity : AppCompatActivity() {

    var loop = -1
    var first = true
    lateinit var progress: ProgressBar
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: CommentAdapter
    var commentData : ArrayList<CommentModel> = ArrayList()
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private val thread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(y: Int, data: DataSnapshot) {
                commentData.clear()
                loop = data.child("0").value.toString().toInt()
                for (x in 1 .. loop) {
                    if (data.child("$x").exists()) {
                        val commentModel = CommentModel()
                        commentModel.uid = data.child("$x").child("UID").value.toString()
                        commentModel.name = data.child("$x").child("Name").value.toString()
                        commentModel.date = data.child("$x").child("Date").value.toString()
                        commentModel.photo = data.child("$x").child("Photo").value.toString()
                        commentModel.comment = data.child("$x").child("Comment").value.toString()
                        commentData.add(commentModel)
                    }
                }
                runOnUiThread {
                    database.getReference("User Data").child(intent.getStringExtra("UID")!!)
                        .child("Post").child(intent.getStringExtra("POS")!!)
                        .child("Comments").setValue(data.childrenCount.toInt()-1)
                    progress.visibility = View.GONE
                    adapter.notifyItemInserted(loop)
                    if (first) first = false
                    else recyclerView.smoothScrollToPosition(loop)
                }
                Log.println(Log.ASSERT,"DATA", "$data")
            }
        }).listen(myRef, 0)
    }

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        toolBar.setNavigationOnClickListener { finish() }

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("Comments").child(intent.getStringExtra("LINK")!!)

        progress = findViewById(R.id.loading)
        layoutManager = LinearLayoutManager(this)
        recyclerView = findViewById(R.id.my_recycle)

        recyclerView.layoutManager = layoutManager
        adapter = CommentAdapter(this, commentData)
        recyclerView.adapter = adapter

        findViewById<Button>(R.id.send_comment).setOnClickListener {
            val edit = findViewById<EditText>(R.id.comment_edit)
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
        Thread(thread).start()
    }
}
