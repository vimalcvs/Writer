package com.alim.writer

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.alim.writer.Account.LoginActivity
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.FollowingData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class SplashActivity : AppCompatActivity() {

    private var loaded = false
    private lateinit var main: Intent
    private lateinit var chooser : Intent
    private val actionRequestGallery = 102
    private lateinit var followData: FollowingData
    private lateinit var myRef: DatabaseReference

    private val followThread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                for (childDataSnapshot : DataSnapshot in data.children) {
                    try { followData.setFollowing(childDataSnapshot.key!!, true) }
                    catch (e: Exception) { Log.println(Log.ASSERT,"Exception Follow","$e") }
                    Log.println(Log.ASSERT,"FOLLOW", followData.getFollowing(childDataSnapshot.key!!).toString())
                }
                followData.loaded = true
            }
        }).listenOnce(FirebaseDatabase.getInstance().getReference("User Data")
            .child(UserData(this@SplashActivity).uid).child("Following"))
    }

    private val thread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, snap: DataSnapshot) {
                AdminData(this@SplashActivity).interstitialAdEnabled =
                    snap.child("Advertisement").child("Interstitial")
                    .child("Enabled").value.toString().toBoolean()
                AdminData(this@SplashActivity).bannerAdEnabled =
                    snap.child("Advertisement").child("Banner")
                        .child("Enabled").value.toString().toBoolean()
                AdminData(this@SplashActivity).nativeAdEnabled =
                    snap.child("Advertisement").child("Native")
                        .child("Enabled").value.toString().toBoolean()

                AdminData(this@SplashActivity).bannerAd =
                    snap.child("Advertisement").child("Banner")
                        .child("ID").value.toString()
                AdminData(this@SplashActivity).interstitialAd =
                    snap.child("Advertisement").child("Interstitial")
                        .child("ID").value.toString()
                AdminData(this@SplashActivity).nativeAd =
                    snap.child("Advertisement").child("Native")
                        .child("ID").value.toString()
                val data = snap.child("Settings")
                main.putExtra("0", "3")
                main.putExtra("1", "Latest")
                main.putExtra("2", "Following")
                main.putExtra("3", "Videos")
                if (data.child("Approval Request").exists())
                    AdminData(this@SplashActivity).approval = data.child(
                        "Approval Request").value.toString().toBoolean()
                if (data.child("Public Post").exists())
                    AdminData(this@SplashActivity).publicPost = data.child(
                        "Public Post").value.toString().toBoolean()
                if (data.child("Comments").exists())
                    AdminData(this@SplashActivity).commentsEnabled = data.child(
                        "Comments").value.toString().toBoolean()
                if (data.child("Image Size").exists())
                    AdminData(this@SplashActivity).imageSize = data.child(
                        "Image Size").value.toString().toInt()
                try {
                    val loop = data.child("Tab").child("0").value.toString().toInt()
                    main.putExtra("0", "${loop+3}")
                    val arr : ArrayList<String> = ArrayList()
                    for (l in 1..loop) {
                        arr.add(data.child("Tab").child("$l").value.toString())
                        main.putExtra("${l+3}", data.child("Tab").child("$l").value.toString())
                    }
                    AdminData(this@SplashActivity).setCategory(arr)
                    if (loaded) {
                        startActivity(main)
                        finish()
                    } else loaded = true
                } catch (e: Exception) {
                    Log.println(Log.ASSERT,"Exception Splash Tab","$e")
                    if (loaded) {
                        startActivity(main)
                        finish()
                    } else loaded = true
                }
            }

        }).listenOnce(FirebaseDatabase.getInstance().getReference("Admin"))
        DataReader(object: Loaded {
            override fun onLoaded(x: Int, data: DataSnapshot) {
                try {
                    IndexData.primaryIndex = data.child("0").value.toString().toInt()
                    IndexData.secondaryIndex = data.child("00").value.toString().toInt()
                    if (loaded) {
                        startActivity(main)
                        finish()
                    } else loaded = true
                }
                catch (e: Exception) {
                    Log.println(Log.ASSERT, "Exception", "$e")
                    if (loaded) {
                        startActivity(main)
                        finish()
                    } else loaded = true
                }
            }
        }).listenOnce(myRef)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        main = Intent(this@SplashActivity, MainActivity::class.java)
        myRef = FirebaseDatabase.getInstance().getReference("Index")

        followData = FollowingData(this)
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            UserData(this).uid = user.uid
            Thread(thread).start()
        }
        else {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        if (!followData.loaded)
            Thread(followThread).start()
    }

    fun test() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")
        startActivityForResult(chooser, actionRequestGallery)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            actionRequestGallery -> {
                try {
                    var storage = Firebase.storage
                    var storageRef = storage.reference
                    val profilePic = findViewById<ImageView>(R.id.splash)
                    profilePic.setImageURI(data?.data)
                    val bitmap = (profilePic.drawable as BitmapDrawable).bitmap
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                    val data = baos.toByteArray()
                    val riversRef: StorageReference = storageRef.child("Profile/myid2.jpg")
                    riversRef.putBytes(data)
                        .addOnSuccessListener { taskSnapshot -> // Get a URL to the uploaded content
                            Log.println(Log.ASSERT,"Success","TRUE")
                        }
                        .addOnFailureListener {
                            Log.println(Log.ASSERT,"Failed","TRUE")
                        }
                    Log.println(Log.ASSERT,"DATA","$data")
                } catch (ex: Exception) {
                    Log.println(Log.ASSERT,"Exception", ex.toString())
                }
            }
        }
    }
}