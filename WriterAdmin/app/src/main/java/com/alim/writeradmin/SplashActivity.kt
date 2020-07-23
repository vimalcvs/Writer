package com.alim.writeradmin

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        FirebaseAuth.getInstance()
            .addAuthStateListener { firebaseAuth: FirebaseAuth ->
                if (firebaseAuth.currentUser == null) {
                    FirebaseDatabase.getInstance().getReference("Admin")
                        .addListenerForSingleValueEvent(object: ValueEventListener {
                            override fun onCancelled(error: DatabaseError) {
                                TODO("Not yet implemented")
                            }

                            override fun onDataChange(snapshot: DataSnapshot) {
                                val sign = Intent(this@SplashActivity, SignInActivity::class.java)
                                sign.putExtra("SIGN" , snapshot.value == null)
                                startActivity(sign)
                                finish()
                            }
                        })
                } else {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
            }
    }
}