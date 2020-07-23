package com.alim.writer.Account

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.alim.writer.Class.CircleImageView
import com.alim.writer.Config.ConstantConfig
import com.alim.writer.Database.UserData
import com.alim.writer.MainActivity
import com.alim.writer.R
import com.alim.writer.SplashActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class SignupActivity : AppCompatActivity() {

    var upload = false
    lateinit var link: Uri
    private val RC_SIGN_IN = 9001

    lateinit var signUp: Button
    lateinit var name: EditText
    lateinit var email: EditText
    lateinit var userData: UserData
    lateinit var password: EditText
    lateinit var c_password: EditText
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    lateinit var profilePic: CircleImageView

    private val myPermission = 105
    private lateinit var chooser : Intent
    private val actionRequestGallery = 102

    private lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        userData = UserData(this)
        auth = FirebaseAuth.getInstance()
        signUp = findViewById<Button>(R.id.sign_up)
        profilePic = findViewById(R.id.profile_pic)

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("User Data")

        Glide.with(this).load(ContextCompat.getDrawable(
            this, (R.drawable.profile)))
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .centerCrop().into(profilePic)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ConstantConfig().clientID)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        name = findViewById(R.id.name)
        email = findViewById(R.id.email)
        password = findViewById(R.id.password)
        c_password = findViewById(R.id.c_password)

        signUp.setOnClickListener {
            val nam = name.text.toString()
            val mail = email.text.toString()
            val pass = password.text.toString()
            val cPass = c_password.text.toString()
            when {
                nam.isEmpty() -> name.error = "Enter a Name"
                mail.isEmpty() -> email.error = "Enter a Email"
                pass.isEmpty() -> password.error = "Enter a Password"
                cPass.isEmpty() -> c_password.error = "Enter Password again"
                pass != cPass -> c_password.error = "Password miss match"
                else -> signUp(it, nam, mail, pass)
            }
        }

        findViewById<LinearLayout>(R.id.google).setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }

        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        chooser = Intent.createChooser(intent, "Choose a Picture")

        profilePic.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), myPermission)
            else
                startActivityForResult(chooser, actionRequestGallery)
        }
    }

    private fun signUp(view: View, name: String, mail: String, pass: String) {
        signUp.visibility = View.GONE
        auth.createUserWithEmailAndPassword(mail, pass)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    userData.session = findViewById<CheckBox>(R.id.remember).isChecked
                    val user = auth.currentUser
                    database.getReference("User Index").child(user!!.uid).setValue(name)
                    myRef.child(user.uid).child("Followers").setValue(0)
                    myRef.child(user.uid).child("Following").setValue(0)
                    myRef.child(user.uid).child("Email").setValue(mail)
                    myRef.child(user.uid).child("Name").setValue(name)
                    userData.name = name
                    userData.email = mail
                    userData.uid = user.uid
                    userData.image = ConstantConfig().image
                    myRef.child(user.uid).child("Photo").setValue(ConstantConfig().image)
                    startActivity(Intent(this, SplashActivity::class.java))
                    finish()
                } else {
                    try {
                        //Toast.makeText(this@SignupActivity, it.exception!!.message.toString(), Toast.LENGTH_SHORT).show()
                        Snackbar.make(view, it.exception!!.message.toString(), Snackbar.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Log.println(Log.ASSERT,"Ex","$e")
                    }
                    //
                    updateUIF(null)
                }
                signUp.visibility = View.VISIBLE
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
            RC_SIGN_IN -> {
                try {
                    val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                    val account = task.getResult(ApiException::class.java)!!
                    Log.println(Log.ASSERT, "Firebase","firebaseAuthWithGoogle:" + account.idToken.toString())
                    firebaseAuthWithGoogle(account.idToken!!)
                } catch (e: Exception) {
                    Log.println(Log.ASSERT, "Firebase", "Google sign in failed:$e")
                }
            }
            actionRequestGallery -> {
                try {
                    profilePic.setImageURI(data?.data)
                    link = data?.data!!
                    upload = true
                } catch (ex: Exception) {
                    Log.println(Log.ASSERT,"Exception", ex.toString())
                }
            }
        }
    }

    private fun updateUIF(@Nullable account: FirebaseUser?) {
        if (account != null) {
            myRef.child(account.uid).child("Email").setValue(account.email!!)
            myRef.child(account.uid).child("Name").setValue(account.displayName)
            myRef.child(account.uid).child("Photo").setValue(account.photoUrl.toString())
            database.getReference("User Index").child(account.uid).setValue(account.displayName)
            userData.name = account.displayName!!
            userData.email = account.email!!
            userData.image = account.photoUrl.toString()
            startActivity(Intent(this, SplashActivity::class.java))
            finish()
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        signUp.visibility = View.GONE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    userData.session = true
                    Log.println(Log.ASSERT,"Firebase", "Authentication Success.")
                    val user = auth.currentUser
                    updateUIF(user)
                } else {
                    signUp.visibility = View.VISIBLE
                    Log.println(Log.ASSERT,"Firebase", "Authentication Failed.")
                    updateUIF(null)
                }
                Toast.makeText(this, task.result.toString(), Toast.LENGTH_SHORT).show()
            }
    }
}