package com.alim.writer.Account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.alim.writer.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class ForgotActivity : AppCompatActivity() {

    lateinit var mail: EditText
    lateinit var forgot: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot)

        auth = FirebaseAuth.getInstance()
        mail = findViewById(R.id.email)
        forgot = findViewById(R.id.forgot)

        forgot.setOnClickListener {
            val email = mail.text.toString()
            if (email.isEmpty())
                mail.error = "Enter your email"
            else recover(it, email)
        }
    }

    private fun recover(view: View, email: String) {
        forgot.visibility = View.GONE
        auth.sendPasswordResetEmail(email).addOnCompleteListener {
            if (it.isSuccessful) {
                Snackbar.make(view, "Check your mail", Snackbar.LENGTH_INDEFINITE).show()
                forgot.isClickable = false
            }
            else Toast.makeText(this, it.exception!!.message, Toast.LENGTH_SHORT).show()
            forgot.visibility = View.VISIBLE
        }
    }
}