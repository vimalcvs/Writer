package com.alim.writer

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import com.alim.writer.Database.ApplicationData

class AboutActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val applicationData = ApplicationData(this)
        if (applicationData.theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        findViewById<TextView>(R.id.version).text =
            "Version "+BuildConfig.VERSION_NAME

        findViewById<ImageView>(R.id.back).setOnClickListener {
            finish()
        }

        findViewById<ImageView>(R.id.github).setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW, Uri.parse("https://github.com/Alims-Repo"))
            )
        }

        findViewById<ImageView>(R.id.twitter).setOnClickListener {
            try {
                packageManager.getPackageInfo("com.twitter.android", 0)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("twitter://user?screen_name=sourav_alim")
                    )
                )
            } catch (e: Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://twitter.com/sourav_alim"))
                )
            }
        }

        findViewById<ImageView>(R.id.facebook).setOnClickListener {
            try {
                packageManager.getPackageInfo("com.facebook.katana", 0)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("fb://profile/100006302621357")
                    )
                )
            } catch (e: java.lang.Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/alim.sourav"))
                )
            }
        }

        findViewById<ImageView>(R.id.instagram).setOnClickListener {
            try {
                packageManager.getPackageInfo("com.instagram.android", 0)
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("http://instagram.com/_u/alim_sourav")
                    )
                )
            } catch (e: java.lang.Exception) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW, Uri.parse("https://instagram.com/alim_sourav"))
                )
                Log.println(Log.ASSERT,"error" ,e.toString())
            }
        }
    }
}
