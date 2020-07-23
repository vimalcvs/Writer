package com.alim.writeradmin

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writeradmin.Adapter.PostAdapter
import com.alim.writeradmin.Adapter.SubmissionAdapter
import com.alim.writeradmin.Database.Settings
import com.alim.writeradmin.Interface.Submission
import com.alim.writeradmin.Model.IndexModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_user.*

class SubmissionActivity : AppCompatActivity() {

    var ax = 0
    var oy = 0
    var position = 0
    lateinit var data: ArrayList<IndexModel>
    private lateinit var adapter: SubmissionAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_submission)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}

        data = intent.getParcelableArrayListExtra<IndexModel>("SUBMISSION")!!
        layoutManager = LinearLayoutManager(this)

        recycler.layoutManager = layoutManager
        adapter = SubmissionAdapter(this, data, object: Submission {
            override fun approved(position: Int) {
                data.removeAt(position)
                adapter.notifyItemRemoved(position)
                adapter.notifyItemRangeChanged(position, data.size)
            }

            override fun details(snapshot: DataSnapshot, uid: String, pos: Int, x: Int, y: Int, dpos: String) {
                ax = x
                oy = y
                position = pos
                val details = Intent(this@SubmissionActivity, DetailsActivity::class.java)
                details.putExtra("UID", uid)
                details.putExtra("POS", dpos)
                details.putExtra("SUBMISSION", true)
                details.putExtra("LIKES", snapshot.child("Likes").childrenCount.toInt().toString())
                details.putExtra("PHOTO", snapshot.child("Photo").value.toString())
                details.putExtra("NAME", snapshot.child("Name").value.toString())
                details.putExtra("DATE", snapshot.child("Date").value.toString())
                details.putExtra("DESC", snapshot.child("Desc").value.toString())
                details.putExtra("TITLE", snapshot.child("Title").value.toString())
                details.putExtra("URL", snapshot.child("Url").value.toString())
                details.putExtra("CONTENT", snapshot.child("Content").value.toString())
                details.putExtra("VIEWS", snapshot.child("Views").childrenCount.toInt().toString())
                details.putExtra("COMMENTS", snapshot.child("Comments").value.toString())
                details.putExtra("CATEGORY", snapshot.child("Category").value.toString())
                details.putExtra("LINK", snapshot.child("Comment").value.toString())
                startActivityForResult(details, 3)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }, FirebaseDatabase.getInstance().getReference("User Data"))
        recycler.adapter = adapter
        adapter.notifyDataSetChanged()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                3 -> {
                    Handler().postDelayed({
                        this@SubmissionActivity.data.removeAt(position)
                        adapter.notifyItemRemoved(position)
                        adapter.notifyItemRangeChanged(position, this@SubmissionActivity.data.size)
                        FirebaseDatabase.getInstance().getReference("Index").child(""+ax)
                            .child(""+oy).child("Approved").setValue("True")
                    },500)
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }
}