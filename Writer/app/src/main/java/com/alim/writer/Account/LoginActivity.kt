package com.alim.writer.Account

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.Nullable
import com.alim.writer.Config.ConstantConfig
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.R
import com.alim.writer.SplashActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import java.lang.Exception

class LoginActivity : AppCompatActivity() {

    private val RC_SIGN_IN = 9001

    private lateinit var email: EditText
    private lateinit var password: EditText

    lateinit var signButton: Button
    lateinit var userData: UserData
    lateinit var myRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    private lateinit var auth: FirebaseAuth
    lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        if (ApplicationData(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        userData = UserData(this)
        auth = FirebaseAuth.getInstance()

        database = FirebaseDatabase.getInstance()
        myRef = database.getReference("User Data")
        signButton = findViewById(R.id.sign_in)

        val gso =
            GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(ConstantConfig().clientID)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        email = findViewById(R.id.email)
        password = findViewById(R.id.password)

        signButton.setOnClickListener {
            val mail = email.text.toString()
            val pass = password.text.toString()
            when {
                mail.isEmpty() -> email.error = "Email Required"
                pass.isEmpty() -> password.error = "Password Required"
                else -> firebaseLogin(mail, pass)
            }
        }

        findViewById<Button>(R.id.forgot).setOnClickListener {
            startActivity(Intent(this, ForgotActivity::class.java))
        }

        findViewById<Button>(R.id.create_account).setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java)) }

        findViewById<LinearLayout>(R.id.google).setOnClickListener {
            val signInIntent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)!!
                Log.println(Log.ASSERT, "Firebase","firebaseAuthWithGoogle:" + account.idToken.toString())
                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: Exception) {
                Log.println(Log.ASSERT, "Firebase", "Google sign in failed:$e")
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
            val intent = Intent(this, SplashActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun firebaseLogin(email: String, pass: String) {
        signButton.visibility = View.GONE
        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                userData.session = findViewById<CheckBox>(R.id.remember).isChecked
                Log.println(Log.ASSERT,"Firebase", "Authentication Success.")
                DataReader(object: Loaded {
                    override fun onLoaded(x: Int, data: DataSnapshot) {
                        userData.image = data.child("Photo").value.toString()
                        userData.name = data.child("Name").value.toString()
                        userData.email = data.child("Email").value.toString()
                        val intent = Intent(this@LoginActivity, SplashActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                }).listenOnce(myRef.child(auth.currentUser!!.uid))
            } else {
                signButton.visibility = View.VISIBLE
                Log.println(Log.ASSERT,"Firebase", "Authentication Failed.")
                updateUIF(null)
            }
            try {
                Toast.makeText(this, task.exception!!.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) { Log.println(Log.ASSERT, "Ex Login", "$e") }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        signButton.visibility = View.GONE
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    userData.session = true
                    Log.println(Log.ASSERT,"Firebase", "Authentication Success.")
                    val user = auth.currentUser
                    updateUIF(user)
                } else {
                    signButton.visibility = View.VISIBLE
                    Log.println(Log.ASSERT,"Firebase", task.result.toString())
                    updateUIF(null)
                }
            }
    }
}