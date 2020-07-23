package com.alim.writer

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.FrameLayout
import androidx.appcompat.widget.Toolbar
import com.alim.writer.Database.ApplicationData
import java.util.*

class SettingsActivity : AppCompatActivity() {

    val goBack = Intent()
    lateinit var applicationData: ApplicationData

    override fun onCreate(savedInstanceState: Bundle?) {
        applicationData = ApplicationData(this)
        if (applicationData.theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        findViewById<FrameLayout>(R.id.style).setOnClickListener {
            val intent = Intent(this, ThemeActivity::class.java)
            startActivityForResult(intent, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data!!.getStringExtra("THEME") == "CHANGE") reCreate()
            else if (data.getStringExtra("LAYOUT") == "CHANGE") {
                goBack.putExtra("LAYOUT","CHANGE")
                setResult(Activity.RESULT_OK, goBack)
            }
        }
    }

    private fun reCreate() {
        Handler().postDelayed({
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            Objects.requireNonNull(overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            ))
            goBack.putExtra("THEME","CHANGE")
            setResult(Activity.RESULT_OK, goBack)
            finish()
        },250)
    }
}