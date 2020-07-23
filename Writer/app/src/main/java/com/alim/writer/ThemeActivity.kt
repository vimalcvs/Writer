package com.alim.writer

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import com.alim.writer.DataHolder.AdapterData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import kotlinx.android.synthetic.main.activity_details.*
import java.io.File
import java.lang.Exception
import java.util.*

class ThemeActivity : AppCompatActivity() {

    private val goBack = Intent()
    private lateinit var applicationData: ApplicationData

    override fun onCreate(savedInstanceState: Bundle?) {
        applicationData = ApplicationData(this)
        if (applicationData.theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        findViewById<Toolbar>(R.id.toolbar).setNavigationOnClickListener { finish() }

        val cacheSize = findViewById<TextView>(R.id.cache_size)
        val cacheSwitch = findViewById<SwitchCompat>(R.id.cache_switch)
        val switchTheme = findViewById<SwitchCompat>(R.id.theme_switch)
        val switchScroll = findViewById<SwitchCompat>(R.id.scroll_switch)

        val chipGroup = findViewById<ChipGroup>(R.id.chip_theme)
        val chipE = findViewById<Chip>(R.id.chip_e)
        val chipF = findViewById<Chip>(R.id.chip_f)

        cacheSwitch.isChecked = applicationData.cache

        cacheSize.text = initializeCache()

        when (applicationData.flat) {
            true -> { chipF.isChecked = true }
            false -> { chipE.isChecked = true }
        }

        chipGroup.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId) {
                R.id.chip_f -> { applicationData.flat = true }
                R.id.chip_e -> { applicationData.flat = false }
            }
            goBack.putExtra("LAYOUT", "CHANGE")
            setResult(Activity.RESULT_OK, goBack)
        }

        switchScroll.isChecked = applicationData.scrollbar
        switchTheme.isChecked = applicationData.theme

        switchScroll.setOnClickListener {
            applicationData.scrollbar = switchScroll.isChecked
            goBack.putExtra("LAYOUT", "CHANGE")
            setResult(Activity.RESULT_OK, goBack)
        }

        switchTheme.setOnClickListener {
            applicationData.theme = switchTheme.isChecked
            reCreate()
        }

        cacheSwitch.setOnClickListener {
            if (!cacheSwitch.isChecked) {
                AlertDialog.Builder(this)
                    .setTitle("Disable Cache ?")
                    .setMessage("Disabling cache will increase data usages and loading time." +
                            " It's highly recommended to keep cache enabled.\n\nNOTE : " +
                            "After disabling cache it will still use a bit cache memory.")
                    .setNegativeButton("Cancel") { dialog, _ ->
                        cacheSwitch.isChecked = true
                        dialog.dismiss()
                    }
                    .setPositiveButton("Yes") { _, _ ->
                        applicationData.cache = cacheSwitch.isChecked
                    }
                    .show()
            } else applicationData.cache = cacheSwitch.isChecked
        }

        findViewById<FrameLayout>(R.id.clear_cache).setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Clear Cache ?")
                .setMessage("By doing this all saved data including images will be " +
                        "deleted & will take significant time to load images for the first time." +
                        "It's recommended but not compulsory to clear cache in every month.")
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("Yes") { _, _ ->
                    cacheDir.deleteRecursively()
                    cacheSize.text = initializeCache()
                }
                .show()
        }

        if (AdminData(this).bannerAdEnabled) {
            val adView = AdView(this)
            adView.adSize = AdSize.BANNER
            adView.adUnitId = AdminData(this).bannerAd
            banner_layout.addView(adView)

            val adRequest: AdRequest = AdRequest.Builder().build()

            adView.loadAd(adRequest)
        }
    }

    private fun initializeCache():String {
        var size = 0
        size += getDirSize(this.cacheDir).toInt()
        size += getDirSize(this.externalCacheDir!!).toInt()
        return "${size/1024/1024} MB"
    }

    private fun getDirSize(dir: File): Long {
        return try {
            var size: Long = 0
            for (file in dir.listFiles()!!) {
                if (file != null && file.isDirectory) {
                    size += getDirSize(file)
                } else if (file != null && file.isFile) {
                    size += file.length()
                }
            }
            return size
        } catch (e: Exception) {
            0
        }
    }

    private fun reCreate() {
        Handler().postDelayed({
            val intent = Intent(this, ThemeActivity::class.java)
            intent.putExtra("SESSION", true)
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