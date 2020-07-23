package com.alim.writeradmin

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.alim.writeradmin.Adapter.CategoryAdapter
import com.alim.writeradmin.Database.Settings
import com.alim.writeradmin.Model.IndexModel
import com.bumptech.glide.Glide
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.ad_input_layout.cancel
import kotlinx.android.synthetic.main.ad_input_layout.save
import kotlinx.android.synthetic.main.category_layout.*
import kotlinx.android.synthetic.main.set_profile_layout.*
import java.io.ByteArrayOutputStream
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    var id = -1
    val USER_DATA = "User Data"
    val array = ArrayList<Int>()
    private var loaded = false
    private val myPermission = 105
    private lateinit var chooser : Intent
    private val actionRequestGallery = 102

    lateinit var userData: DataSnapshot
    lateinit var database: FirebaseDatabase

    val list : ArrayList<String> = ArrayList()
    var userId: ArrayList<String> = ArrayList()
    var userName: ArrayList<String> = ArrayList()
    var index: ArrayList<IndexModel> = ArrayList()
    var submission: ArrayList<IndexModel> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        loading.visibility = View.VISIBLE

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")

        database = FirebaseDatabase.getInstance()
        change_theme.isChecked = Settings(this).theme

        admin_name.text = Settings(this).name
        try {
            Glide.with(this@MainActivity).load(Settings(this).image).into(admin_photo)
        } catch (e: Exception) { Log.println(Log.ASSERT,"E Main", "$e") }

        if (Settings(this).theme && !Settings(this).colorful) {
            val color = resources.getColor(R.color.cardBack)
            cardView0.setCardBackgroundColor(color)
            cardView1.setCardBackgroundColor(color)
            cardView2.setCardBackgroundColor(color)
            cardView3.setCardBackgroundColor(color)
            cardView4.setCardBackgroundColor(color)
            cardView5.setCardBackgroundColor(color)
        }

        database.getReference("")

        database.getReference("User Index")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.println(Log.ASSERT,"Error", "$error")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    userId.clear()
                    userName.clear()
                    userData = snapshot
                    for (x in snapshot.children) {
                        userId.add(x.value.toString())
                        userName.add(x.key.toString())
                    }
                    total_user_count.text = snapshot.childrenCount.toInt().toString()
                }
            })

        database.getReference("Index")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.println(Log.ASSERT,"error Main", "$error")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (admin_name.text.isEmpty()) {
                        admin_name.text = Settings(this@MainActivity).name
                        try {
                            Glide.with(this@MainActivity).load(Settings(
                                this@MainActivity).image).into(admin_photo)
                        } catch (e: Exception) {
                            Log.println(Log.ASSERT, "E Main", "$e")
                        }
                    }
                    try { microProcess(snapshot) }
                    catch (e: Exception) { Log.println(Log.ASSERT,"E Main", "$e") }
                    Handler().postDelayed({ loading.visibility = View.GONE },1000)
                }
            })

        val categoryAdapter = CategoryAdapter(this, list)
        val layoutManager = LinearLayoutManager(this,
            LinearLayoutManager.HORIZONTAL, false)
        category_recycle.layoutManager = layoutManager
        category_recycle.adapter = categoryAdapter

        database.getReference("Admin")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    Log.println(Log.ASSERT, "Data Error","$error")
                }
                override fun onDataChange(snapshot: DataSnapshot) {
                    try {
                        loaded = true
                        list.clear()
                        for (x in 1..snapshot.child("Settings")
                            .child("Tab").child("0").value.toString().toInt())
                            list.add(snapshot.child("Settings")
                                .child("Tab").child("" + x).value.toString())
                        categoryAdapter.notifyDataSetChanged()
                    } catch (e: Exception) { Log.println(Log.ASSERT,"Ex Main", "$e") }
                }
            })

        initButton()
        contentCategoryEditor()
    }

    private fun initButton() {
        admin_photo.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE), myPermission)
            else
                startActivityForResult(chooser, actionRequestGallery)
        }

        change_theme.setOnCheckedChangeListener { _, b ->
            Settings(this).theme = b
            activityRecreate()
        }

        total_user.setOnClickListener {
            if (total_user_count.text != "0") {
                val intent = Intent(this, UserActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            } else Snackbar.make(it, "No User found", Snackbar.LENGTH_SHORT).show()
        }

        total_post.setOnClickListener {
            if (index.isEmpty())
                Snackbar.make(it, "No Post found", Snackbar.LENGTH_SHORT).show()
            else {
                val postIntent = Intent(this, PostActivity::class.java)
                postIntent.putExtra("INDEX", index)
                startActivity(postIntent)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }

        total_sub.setOnClickListener {
            if (submission.isEmpty())
                Snackbar.make(it, "No Submission request", Snackbar.LENGTH_SHORT).show()
            else {
                val submissionIntent = Intent(this, SubmissionActivity::class.java)
                submissionIntent.putExtra("SUBMISSION", submission)
                startActivity(submissionIntent)
                overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
            }
        }

        settings.setOnClickListener {
            startActivityForResult(Intent(this, SettingsActivity::class.java), 1)
            overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
        }
    }

    private fun microProcess(snapshot: DataSnapshot) {
        index.clear()
        submission.clear()
        val zero = snapshot.child("0").value.toString().toInt()
        val dzero = snapshot.child("00").value.toString().toInt()
        Thread {
            var sub = 0
            for (x in zero downTo 1) {
                for (y in snapshot.child("$x").children) {
                    val model = IndexModel()
                    model.x = x
                    model.y = y.key.toString().toInt()
                    model.uid = y.child("UID").value.toString()
                    model.pos = y.child("POS").value.toString()
                    model.content = y.child("Content").value.toString()
                    model.category = y.child("Category").value.toString()
                    model.approved = y.child("Approved").value.toString()
                    if (model.approved != "True") {
                        sub++
                        submission.add(model)
                    } else index.add(model)
                }
            }
            runOnUiThread {
                total_sub_count.text = "$sub"
                total_post_count.text = (((zero-1)*10)+dzero-sub).toString()
            }
        }.start()
        val id = snapshot.child("$zero")
            .child("$dzero").child("UID").value.toString()
        val pos = snapshot.child("$zero")
            .child("$dzero").child("POS").value.toString()
        database.getReference(USER_DATA).child(id).child("Post")
            .child(pos)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    name.text = userData.child(id).value.toString()
                    date.text = snapshot.child("Date").value.toString()
                    try {
                        Glide.with(this@MainActivity)
                            .load(snapshot.child("Photo").value.toString())
                            .into(last_image)
                    } catch (e: Exception) { Log.println(Log.ASSERT,"Ex Main", "$e") }
                }
            })
        lastPost(id, pos)
    }

    private fun activityRecreate() {
        Handler().postDelayed({
            val rec = Intent(this, MainActivity::class.java)
            startActivity(rec)
            Objects.requireNonNull(overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            ))
            finish()
        },200)
    }

    private fun uploadImage(uid: String, image: Bitmap) {
        val ref = FirebaseStorage.getInstance().reference
        val riversRef: StorageReference = ref.child("Profile/$uid.jpg")

        val stream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.PNG, 90, stream)
        riversRef.putBytes(stream.toByteArray())
            .addOnSuccessListener {
                riversRef.downloadUrl.addOnSuccessListener {
                    Log.println(Log.ASSERT,"DONE", "DONE")
                    database.getReference(USER_DATA).child(uid).child("Photo")
                        .setValue(it.toString())
                    Settings(this@MainActivity).image = it.toString()
                }
            }

    }

    private fun setImage(uri: Uri) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.set_profile_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.dialog_profile_picture.setImageURI(uri)
        dialog.cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.save.setOnClickListener {
            admin_photo.setImageURI(uri)
            uploadImage(FirebaseAuth.getInstance().currentUser!!.uid,
                dialog.dialog_profile_picture.drawToBitmap())
                dialog.dismiss()
            }
        dialog.show()
    }

    private fun lastPost(uid: String, pos: String) {
        database.getReference(USER_DATA).child(uid).child("Post").child(pos)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    val details = Intent(this@MainActivity, DetailsActivity::class.java)
                    details.putExtra("UID", uid)
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

                    name.setOnClickListener {
                        val profile = Intent(this@MainActivity, ProfileActivity::class.java)
                        profile.putExtra("UID", uid)
                        startActivity(profile)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }
                    last_image.setOnClickListener {
                        val profile = Intent(this@MainActivity, ProfileActivity::class.java)
                        profile.putExtra("UID", uid)
                        startActivity(profile)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }
                    last_frame.setOnClickListener {
                        startActivity(details)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }
                    cardView1.setOnClickListener {
                        startActivity(details)
                        overridePendingTransition(R.anim.slide_in, R.anim.slide_out)
                    }
                }
            })
    }

    private fun contentCategoryEditor() {
        edit_cat_button.setOnClickListener {
            if (loaded) {
                array.clear()
                val dialog = Dialog(this@MainActivity)
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.setCancelable(false)
                dialog.setContentView(R.layout.category_layout)
                id = -1
                for (a in list) {
                    id++
                    val chipP = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.chip_layout, null) as Chip
                    chipP.id = id
                    chipP.text = a
                    dialog.user_group.addView(chipP)
                    array.add(id)
                }
                id++
                array.add(id)
                val chip = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.chip_layout, null) as Chip
                chip.id = id
                chip.isCheckedIconEnabled = false
                chip.chipIcon = resources.getDrawable(R.drawable.ic_baseline_add_24)
                chip.text = "Add More"
                dialog.user_group.addView(chip)

                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.user_group.setOnCheckedChangeListener { group, checkedId ->
                    when {
                        checkedId < 0 -> {
                            dialog.save_cat.text = "Done"
                            dialog.remove_cat.visibility = View.GONE
                            dialog.edit_cat_lay.visibility = View.GONE
                        }
                        checkedId == id -> {
                            dialog.save_cat.text = "Add"
                            dialog.remove_cat.visibility = View.GONE
                            dialog.edit_cat_lay.visibility = View.VISIBLE
                            dialog.edit_cat.text = "".toEditable()
                            dialog.edit_cat.requestFocus()
                            val imm =
                                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.toggleSoftInput(
                                InputMethodManager.SHOW_FORCED,
                                0
                            )
                        }
                        else -> {
                            val chipS = group.getChildAt(array.indexOf(checkedId)) as Chip
                            dialog.save_cat.text = "Save"
                            dialog.remove_cat.visibility = View.VISIBLE
                            dialog.edit_cat_lay.visibility = View.VISIBLE
                            dialog.edit_cat.text = chipS.text.toString().toEditable()
                        }
                    }
                }

                dialog.remove_cat.setOnClickListener {
                    val chipS = dialog.user_group.getChildAt(array.indexOf(dialog.user_group.checkedChipId)) as Chip
                    dialog.user_group.removeView(chipS)
                    array.removeAt(array.indexOf(dialog.user_group.checkedChipId))
                    dialog.save_cat.text = "Done"
                    dialog.edit_cat.clearFocus()
                    dialog.remove_cat.visibility = View.GONE
                    dialog.edit_cat_lay.visibility = View.GONE
                    val imm: InputMethodManager = this.
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(dialog.edit_cat.windowToken, 0)
                }

                dialog.save_cat.setOnClickListener {
                    onSaveClick(dialog, it)
                }
                dialog.cancel_cat.setOnClickListener {
                    val imm: InputMethodManager = this.
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(dialog.edit_cat.windowToken, 0)
                    dialog.dismiss()
                }
                dialog.show()
            } else Snackbar.make(this.view, "Wait a moment...", Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun onSaveClick(dialog: Dialog, it: View) {
        when (dialog.save_cat.text) {
            "Add" -> {
                if (array.size < 11) {
                    val chipS = dialog.user_group.getChildAt(array
                        .indexOf(dialog.user_group.checkedChipId)) as Chip
                    chipS.text = dialog.edit_cat.text.toString()
                    chipS.isChipIconEnabled = false
                    chipS.isCheckedIconEnabled = true
                    val chip = LayoutInflater.from(this@MainActivity)
                        .inflate(R.layout.chip_layout, null) as Chip
                    id++
                    array.add(id)
                    chip.id = id
                    chip.isCheckedIconEnabled = false
                    chip.chipIcon = resources.getDrawable(R.drawable.ic_baseline_add_24)
                    chip.text = "Add More"
                    dialog.user_group.addView(chip)
                    dialog.edit_cat.clearFocus()
                    (dialog.user_group.getChildAt(array.indexOf(dialog.user_group
                        .checkedChipId)) as Chip).isChecked = false
                    val imm: InputMethodManager = this.
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(dialog.edit_cat.windowToken, 0)
                } else Snackbar.make(it, "Max Category limit Exited", Snackbar.LENGTH_SHORT).show()
            }

            "Save" -> {
                val chipS = dialog.user_group.getChildAt(
                    array.indexOf(dialog.user_group.checkedChipId)) as Chip
                chipS.text = dialog.edit_cat.text.toString()
                dialog.save_cat.text = "Done"
                dialog.edit_cat.clearFocus()
                dialog.remove_cat.visibility = View.GONE
                dialog.edit_cat_lay.visibility = View.GONE
                (dialog.user_group.getChildAt(array.indexOf(dialog.user_group
                    .checkedChipId)) as Chip).isChecked = false
                val imm: InputMethodManager = this.
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(dialog.edit_cat.windowToken, 0)
            }

            "Done" -> {
                val cat = ArrayList<String>()
                Log.println(Log.ASSERT,"Test", array.toString())
                for (c in 0 until array.size-1) {
                    val chi = dialog.user_group.getChildAt(c) as Chip
                    cat.add(chi.text.toString())
                }
                saveCategory(cat)
                val imm: InputMethodManager = this.
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(dialog.edit_cat.windowToken, 0)
                dialog.dismiss()
            }
        }
    }

    private fun saveCategory(cat: ArrayList<String>) {
        database.getReference("Admin").child("Settings")
            .child("Tab").child("0").setValue(cat.size)
        for (c in 0 until cat.size) {
            database.getReference("Admin").child("Settings")
                .child("Tab").child("${c+1}").setValue(cat[c])
        }
        Log.println(Log.ASSERT,"LIST", cat.toString())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                actionRequestGallery -> {
                    try {
                        setImage(data?.data!!)
                    } catch (ex: Exception) {
                        Log.println(Log.ASSERT,"Exception", ex.toString())
                    }
                }
                1 -> if (data!!.getStringExtra("THEME") == "CHANGE") {
                    activityRecreate()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            myPermission -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED))
                    startActivityForResult(chooser, actionRequestGallery)
                else Toast.makeText(this,"Permission Denied",Toast.LENGTH_LONG).show()
                return
            }
        }
    }
    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}