package com.alim.writeradmin

import android.os.Bundle
import android.text.Editable
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.alim.writeradmin.Database.Settings
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class EditActivity : AppCompatActivity() {

    lateinit var title: EditText
    lateinit var desc: EditText
    lateinit var add: Button
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("User Data").child(intent.getStringExtra("UID")!!)
            .child("Post").child("${intent.getStringExtra("POS")}")

        title = findViewById<TextInputEditText>(R.id.title)
        desc = findViewById<TextInputEditText>(R.id.desc)
        Glide.with(this).load(intent.getStringExtra("PHOTO")!!)
            .into(findViewById(R.id.profile))
        findViewById<TextView>(R.id.name).text = intent.getStringExtra("NAME")
        findViewById<TextView>(R.id.date).text = intent.getStringExtra("DATE")
        title.text = intent.getStringExtra("TITLE")!!.toEditable()
        desc.text = intent.getStringExtra("DESCRIPTION")!!.toEditable()
        findViewById<Button>(R.id.update).setOnClickListener {
            when {
                title.text.toString()=="" -> Snackbar.make(it,"Title is needed", Snackbar.LENGTH_SHORT).show()
                desc.text.toString()=="" -> Snackbar.make(it,"Description is needed", Snackbar.LENGTH_SHORT).show()
                else -> upDate()
            }
        }


    }
    private fun upDate() {
        myRef.child("Title").setValue(title.text.toString())
        myRef.child("Desc").setValue(desc.text.toString())
        onBackPressed()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }

    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}