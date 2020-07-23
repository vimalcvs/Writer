package com.alim.writeradmin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.util.Log
import android.view.View
import android.widget.Toast
import com.alim.writeradmin.Database.Settings
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sign_in.*
import kotlinx.android.synthetic.main.activity_sign_in.name

class SignInActivity : AppCompatActivity() {

    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("User Data")


        if (!intent.getBooleanExtra("SIGN", false)) {
            sign_in.text = "Sign In"
            name_input_layout.visibility = View.GONE
        } else {
            sign_in.text = "Sign Up"
            name_input_layout.visibility = View.VISIBLE
        }

        sign_in.setOnClickListener {
            if (intent.getBooleanExtra("SIGN", false) && name.text.toString().isEmpty())
                name.error = "Enter your name"
            else if (email.text.toString().isEmpty())
                email.error = "Enter an email address"
            else if (password.text.toString().isEmpty())
                password.error = "Enter a password"
            else if (password.text.toString().length < 6)
                password.error = "Password is too short"
            else authenticate(it, intent.getBooleanExtra("SIGN", false))
        }
    }

    private fun authenticate(view: View, create : Boolean) {
        if(create) {
            sign_in.visibility = View.GONE
            auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        val user = auth.currentUser
                        database.getReference("User Index").child(user!!.uid).setValue(name.text.toString())
                        myRef.child(user.uid).child("Followers").setValue(0)
                        myRef.child(user.uid).child("Following").setValue(0)
                        myRef.child(user.uid).child("Email").setValue(email.text.toString())
                        myRef.child(user.uid).child("Name").setValue(name.text.toString())
                        myRef.child(user.uid).child("User").setValue("Verified User")
                        myRef.child(user.uid).child("Admin").setValue(true)
                        myRef.child(user.uid).child("Photo")
                            .setValue("https://github.com/Alims-Repo/Public-Files/raw/master/profile.jpg")

                        database.getReference("Admin").child("Advertisement")
                            .child("Interstitial").child("Enabled").setValue(false)
                        database.getReference("Admin").child("Advertisement")
                            .child("Interstitial").child("ID")
                            .setValue("ca-app-pub-3940256099942544/1033173712")
                        database.getReference("Admin").child("Advertisement")
                            .child("Interstitial").child("Interval").setValue(3)
                        database.getReference("Admin").child("Advertisement")
                            .child("Banner").child("Enabled").setValue(false)
                        database.getReference("Admin").child("Advertisement")
                            .child("Banner").child("ID")
                            .setValue("ca-app-pub-3940256099942544/6300978111")
                        database.getReference("Admin").child("Advertisement")
                            .child("Native").child("Enabled").setValue(false)
                        database.getReference("Admin").child("Advertisement")
                            .child("Native").child("ID")
                            .setValue("ca-app-pub-3940256099942544/2247696110")

                        database.getReference("Admin").child(
                            "Settings").child("Public Post").setValue(false)
                        database.getReference("Admin").child(
                            "Settings").child("Approval Request").setValue(true)
                        database.getReference("Admin").child(
                            "Settings").child("Comments").setValue(false)
                        database.getReference("Admin").child(
                            "Settings").child("Image Size").setValue(1024)

                        Settings(this@SignInActivity).name = name.text.toString()

                        startActivity(Intent(this, SplashActivity::class.java))
                        finish()
                    } else {
                        try {
                            Snackbar.make(view,
                                it.exception!!.message.toString(), Snackbar.LENGTH_SHORT).show()
                        } catch (e: Exception) {
                            Log.println(Log.ASSERT,"Ex","$e")
                        }
                    }
                    sign_in.visibility = View.VISIBLE
                }
        } else {
            logIn()
        }
    }

    private fun logIn() {
        sign_in.visibility = View.GONE
        auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    myRef.child(auth.currentUser!!.uid)
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                Log.println(Log.ASSERT,"DATA", snapshot.toString())
                                if (snapshot.child("Admin").value.toString().toBoolean()) {
                                    Settings(this@SignInActivity).name = snapshot
                                        .child("Name").value.toString()
                                    Settings(this@SignInActivity).image = snapshot
                                        .child("Photo").value.toString()
                                    finish()
                                } else {
                                    FirebaseAuth.getInstance().signOut()
                                    Snackbar.make(view, "This is not a admin Account",
                                        Snackbar.LENGTH_INDEFINITE).show()
                                    sign_in.visibility = View.VISIBLE
                                }
                            }

                        })
                } else {
                    sign_in.visibility = View.VISIBLE
                }
                try {
                    Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
                } catch (e: Exception) { Log.println(Log.ASSERT, "Exception", "$e") }
            }
    }

}