package com.alim.writer

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Util.FileUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.textfield.TextInputEditText
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class PostActivity : AppCompatActivity(), YouTubePlayer.OnInitializedListener {

    var url = ""
    var content = ""
    var external = true
    lateinit var link: Uri
    lateinit var actualImage: File
    lateinit var compressedImage: File

    lateinit var upload: Button
    lateinit var chipGroup: ChipGroup
    lateinit var title: TextView
    lateinit var desc: TextView
    lateinit var myRef: DatabaseReference
    lateinit var lRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    lateinit var userData: UserData
    lateinit var imageView: ImageView
    lateinit var playerFrame: FrameLayout
    lateinit var youTubePlayer: YouTubePlayer
    lateinit var youTubePlayerView: YouTubePlayerFragment

    private val myPermission = 105
    private lateinit var chooser : Intent
    private val actionRequestGallery = 102

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post)
        val toolBar = findViewById<Toolbar>(R.id.toolbar)
        toolBar.setNavigationOnClickListener { finish() }
        database = FirebaseDatabase.getInstance()
        lRef = database.reference
        myRef = database.getReference("User Data").child(FirebaseAuth
            .getInstance().currentUser!!.uid).child("Post")

        userData = UserData(this)
        val cache = when (ApplicationData(this).cache) {
            true -> DiskCacheStrategy.AUTOMATIC
            false -> DiskCacheStrategy.NONE
        }
        val profile = findViewById<ImageView>(R.id.profile)
        val category = findViewById<ChipGroup>(R.id.chip_category)
        youTubePlayerView = fragmentManager.findFragmentById(R.id.player) as YouTubePlayerFragment

        imageView = findViewById(R.id.image)
        title = findViewById(R.id.title)
        desc = findViewById(R.id.desc)
        upload = findViewById(R.id.upload)
        chipGroup = findViewById(R.id.chip_category)
        playerFrame = findViewById(R.id.player_frame)

        findViewById<TextView>(R.id.name).text = userData.name
        findViewById<TextView>(R.id.date).text = SimpleDateFormat(
            "dd MMM yyyy").format(Calendar.getInstance().time).toString()
        Glide.with(this).load(userData.image)
            .dontAnimate()
            .diskCacheStrategy(cache)
            .centerCrop().into(profile)
        youTubePlayerView.initialize("YOUR API KEY",
            object : YouTubePlayer.OnInitializedListener {
                override fun onInitializationSuccess(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubePlayer?,
                    p2: Boolean) {
                    youTubePlayer = p1!!
                }

                override fun onInitializationFailure(
                    p0: YouTubePlayer.Provider?,
                    p1: YouTubeInitializationResult?
                ) {
                    TODO("Not yet implemented")
                }

            })

        for (x in AdminData(this).getCategory()) {
            val chip = Chip(category.context)
            chip.text= x
            chip.isClickable = true
            chip.isCheckable = true
            category.addView(chip)
        }
        findViewById<Button>(R.id.add).setOnClickListener { addLink() }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")

        upload.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), myPermission)
            else
                startActivityForResult(chooser, actionRequestGallery)
        }

        findViewById<Button>(R.id.post).setOnClickListener {
            val cat = try {
                findViewById<Chip>(chipGroup.checkedChipId).text.toString()
            } catch (e: Exception) {""}
            when {
                title.text.isEmpty() -> title.error = "Title cannot be empty"
                desc.text.isEmpty() -> desc.error = "Post cannot be empty"
                url.isEmpty() && external -> Toast.makeText(this,
                    "Upload or add a link", Toast.LENGTH_SHORT).show()
                cat.isEmpty() -> Toast.makeText(this, "Select a category", Toast.LENGTH_SHORT).show()
                else -> {
                    AlertDialog.Builder(this)
                        .setTitle("Confirm post")
                        .setMessage("Can you see your content preview bellow  ?")
                        .setNegativeButton("No") { dialog, _ ->
                            Toast.makeText(this@PostActivity,"Please check your content",Toast.LENGTH_SHORT).show()
                            dialog.dismiss()
                        }
                        .setPositiveButton("Yes") { dialog, _ ->
                            post(cat)
                            dialog.dismiss()
                        }
                        .show()
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            actionRequestGallery -> {
                try {
                    link = data?.data!!
                    imageView.visibility = View.VISIBLE
                    playerFrame.visibility = View.GONE
                    Glide.with(this).load(link)
                        .dontAnimate()
                        .centerCrop().into(imageView)
                    content = "Image"
                    external = false
                    actualImage = FileUtil.from(this, link)
                    Log.println(Log.ASSERT,"FILE", actualImage.toString())
                    compress()
                } catch (ex: Exception) {
                    Log.println(Log.ASSERT,"Exception", ex.toString())
                }
            }
        }
    }

    private fun compress() {
        val dialog: ProgressDialog = ProgressDialog.show(
            this, "",
            "Compressing Image. Please wait...", true, false
        )
        dialog.show()
        actualImage.let { imageFile ->
            lifecycleScope.launch {
                compressedImage = Compressor.compress(this@PostActivity, imageFile){
                    size((AdminData(this@PostActivity).imageSize*1024).toLong())
                }
                Log.println(Log.ASSERT,"NEW SIZE", compressedImage.length().toString())
                dialog.hide()
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun post(cat: String) {
        if (IndexData.loop > -1 && IndexData.primaryIndex > -1 && IndexData.secondaryIndex > -1) {
            val key = myRef.push().key!!
            val id = FirebaseAuth.getInstance().currentUser!!.uid
            val lf = myRef.child(IndexData.loop.toString())
            lf.child("Content").setValue(content)
            lf.child("Date").setValue(SimpleDateFormat(
                "dd MMM yyyy").format(Calendar.getInstance().time).toString())
            lf.child("Desc").setValue(desc.text.toString())
            lf.child("Title").setValue(title.text.toString())
            lf.child("Category").setValue(cat)
            lf.child("Name").setValue(userData.name)
            lf.child("Photo").setValue(userData.image)
            lf.child("Comment").setValue(key)
            lf.child("Comments").setValue(0)
            if (userData.verified) lf.child("Verified").setValue("True")
            lRef.child("Comments").child(key).child("0").setValue(0)

            val primary = if (IndexData.secondaryIndex+1 > 10) IndexData.primaryIndex+1 else IndexData.primaryIndex
            val secondary = if (IndexData.secondaryIndex+1 > 10) 1 else IndexData.secondaryIndex+1

            lRef.child("Index").child("0").setValue(primary)
            lRef.child("Index").child("00").setValue(secondary)
            lRef.child("Index").child("$primary").child("$secondary").child("Category").setValue(cat)
            lRef.child("Index").child("$primary").child("$secondary").child("Content").setValue(content)
            lRef.child("Index").child("$primary").child("$secondary").child("UID").setValue(id)
            lRef.child("Index").child("$primary").child("$secondary").child("POS").setValue(IndexData.loop)
            when {
                userData.verified -> lRef.child("Index").child("$primary").child("$secondary").child("Approved").setValue("True")
                AdminData(this).approval -> lRef.child("Index").child("$primary").child("$secondary").child("Approved").setValue("False")
                else -> lRef.child("Index").child("$primary").child("$secondary").child("Approved").setValue("True")
            }
                //Approved:
            //"True"

            myRef.child("0").setValue(IndexData.loop)
            if (external) {
                lf.child("Url").setValue(url)
                finish()
            } else upload(lf, key)

        } else Toast.makeText(this,"Something Gone Wrong", Toast.LENGTH_SHORT).show()
    }

    private fun upload(ref : DatabaseReference, name: String) {
        val dialog: ProgressDialog = ProgressDialog.show(
            this, "",
            "Uploading Image. Please wait...", true, false
        )
        dialog.show()
        val storageRef = Firebase.storage.reference
        dialog.show()
        val riversRef: StorageReference = storageRef.child("Post/${userData.uid}/$name.jpg")
        riversRef.putStream(FileInputStream(compressedImage))
            .addOnCompleteListener {
                finish()
            }
            .addOnProgressListener {
            }
            .addOnSuccessListener { taskSnapshot -> // Get a URL to the uploaded content
                riversRef.downloadUrl.addOnSuccessListener {
                    dialog.hide()
                    ref.child("Url").setValue(it.toString())
                }
            }
            .addOnFailureListener {
                Log.println(Log.ASSERT,"Failed","TRUE")
            }
    }

    private fun addLink() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.add_link_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val link = dialog.findViewById<TextInputEditText>(R.id.link)

        dialog.findViewById<ImageView>(R.id.dismiss).setOnClickListener { dialog.dismiss() }

        dialog.findViewById<Button>(R.id.post).setOnClickListener {
            val address = link.text.toString()
            if (address.isEmpty()) Toast.makeText(this, "Add link or Upload from device",
                Toast.LENGTH_SHORT).show()
            if (address.contains("://youtu.be/") ||
                address.contains(".youtube.com/watch"))
                youTubePlayer.cueVideo(getYoutube(dialog, address, link))
            else {
                imageView.visibility = View.VISIBLE
                playerFrame.visibility = View.GONE
                Glide.with(this).load(address)
                    .dontAnimate()
                    .centerCrop().into(findViewById(R.id.image))
                content = "Image"
                url = address
                dialog.dismiss()
            }
            external = true
        }
        dialog.show()
    }

    private fun getYoutube(dialog: Dialog, link: String, text: TextInputEditText): String {
        val pattern = "(?<=youtu.be/|watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*"
        val compiledPattern: Pattern = Pattern.compile(pattern)
        val matcher: Matcher = compiledPattern.matcher(link)
        return if (matcher.find()) {
            imageView.visibility = View.GONE
            playerFrame.visibility = View.VISIBLE
            dialog.dismiss()
            content = "Youtube"
            url = matcher.group()
            matcher.group()
        } else {
            text.error = "Wrong youtube link"
            "ERROR"
        }
    }

    override fun onInitializationSuccess(
        p0: YouTubePlayer.Provider?,
        p1: YouTubePlayer?,
        p2: Boolean) {
        youTubePlayer = p1!!
    }

    override fun onInitializationFailure(
        p0: YouTubePlayer.Provider?,
        p1: YouTubeInitializationResult?) {
        TODO("Not yet implemented")
    }
}