package com.alim.writeradmin

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.alim.writeradmin.Database.Settings
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.activity_settings.size
import kotlinx.android.synthetic.main.ad_input_layout.*
import java.util.*

class SettingsActivity : AppCompatActivity() {

    val PUBLIC_POST = "Public Post"
    var interstitialInterval = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        if (Settings(this).theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        back.setOnClickListener { finish()
            overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)}

        theme_switch.isChecked = Settings(this).theme
        dashboard_switch.isChecked = Settings(this).colorful

        FirebaseDatabase.getInstance().getReference("Admin")
            .addValueEventListener(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    interstitialInterval = try {
                        val inter = snapshot.child("Advertisement")
                            .child("Interstitial").child("Interval").value.toString().toInt()
                        interstitial_text.text = "    Interval ~ $inter"
                        inter
                    } catch (e: Exception) {3}
                    interstitial_switch.isChecked = (snapshot.child("Advertisement")
                        .child("Interstitial").child("Enabled").value.toString().toBoolean())
                    interstitial_id.text = (snapshot.child("Advertisement")
                        .child("Interstitial").child("ID").value.toString())
                    banner_switch.isChecked = (snapshot.child("Advertisement")
                        .child("Banner").child("Enabled").value.toString().toBoolean())
                    banner_id.text = (snapshot.child("Advertisement")
                        .child("Banner").child("ID").value.toString())
                    native_switch.isChecked = (snapshot.child("Advertisement")
                        .child("Native").child("Enabled").value.toString().toBoolean())
                    native_id.text = (snapshot.child("Advertisement")
                        .child("Native").child("ID").value.toString())
                    public_switch.isChecked = (snapshot.child("Settings")
                        .child(PUBLIC_POST).value.toString().toBoolean())
                    approval_switch.isEnabled = (snapshot.child("Settings")
                        .child(PUBLIC_POST).value.toString().toBoolean())
                    approval_switch.isChecked = (snapshot.child("Settings")
                        .child("Approval Request").value.toString().toBoolean())
                    comment_switch.isChecked = (snapshot.child("Settings")
                        .child("Comments").value.toString().toBoolean())
                    size.text = (snapshot.child("Settings")
                        .child("Image Size").value.toString()).toEditable()
                    Handler().postDelayed({
                        settings_progress.visibility = View.GONE
                    },500)
                }

            })

        interstitial_frame.setOnClickListener {
            saveAdId("Interstitial", interstitial_id.text.toString()) }
        banner_frame.setOnClickListener {
            saveAdId("Banner", banner_id.text.toString()) }
        native_frame.setOnClickListener {
            saveAdId("Native", native_id.text.toString()) }

        interstitial_switch.setOnCheckedChangeListener { _, b ->
            FirebaseDatabase.getInstance().getReference("Admin")
                .child("Advertisement").child("Interstitial")
                .child("Enabled").setValue(b) }

        banner_switch.setOnCheckedChangeListener { _, b ->
            FirebaseDatabase.getInstance().getReference("Admin")
                .child("Advertisement").child("Banner")
                .child("Enabled").setValue(b) }

        native_switch.setOnCheckedChangeListener { _, b ->
            FirebaseDatabase.getInstance().getReference("Admin")
                .child("Advertisement").child("Native")
                .child("Enabled").setValue(b) }

        public_switch.setOnCheckedChangeListener { _, b ->
            settings_progress.visibility = View.VISIBLE
            FirebaseDatabase.getInstance().getReference("Admin").child(
                "Settings").child(PUBLIC_POST).setValue(b)
        }
        approval_switch.setOnCheckedChangeListener { _, b ->
            settings_progress.visibility = View.VISIBLE
            FirebaseDatabase.getInstance().getReference("Admin").child(
                "Settings").child("Approval Request").setValue(b)
        }
        comment_switch.setOnCheckedChangeListener { _, b ->
            settings_progress.visibility = View.VISIBLE
            FirebaseDatabase.getInstance().getReference("Admin").child(
                "Settings").child("Comments").setValue(b)
        }
        save_size.setOnClickListener {
            settings_progress.visibility = View.VISIBLE
            FirebaseDatabase.getInstance().getReference("Admin").child(
                "Settings").child("Image Size").setValue(size.text.toString().toInt())
            size.clearFocus()
            val imm: InputMethodManager = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(size.windowToken, 0)
        }
        theme_switch.setOnCheckedChangeListener { _, b -> Settings(this).theme = b
            activityRecreate() }
        dashboard_switch.setOnCheckedChangeListener { _, b -> Settings(this).colorful = b
            val goBack = Intent()
            goBack.putExtra("THEME","CHANGE")
            setResult(Activity.RESULT_OK, goBack) }
    }

    private fun saveAdId(type: String, id: String) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.ad_input_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.title.text = "$type Ad ID"
        if (type=="Interstitial") {
            (dialog.interval_click.getChildAt(interstitialInterval-1)
                    as Chip).isChecked = true
            dialog.interstitial_interval.visibility = View.VISIBLE
        } else dialog.interstitial_interval.visibility = View.GONE
        dialog.ad_unit.text = id.toEditable()
        dialog.cancel.setOnClickListener {
            dialog.dismiss()
        }
        dialog.save.setOnClickListener {
            if (type == "Interstitial") {
                when {
                    dialog.ad_unit.text.toString().isEmpty() -> Snackbar.make(it,
                        "Please enter a ad id", Snackbar.LENGTH_SHORT).show()
                    dialog.interval_click.checkedChipId < 1 -> Snackbar.make(it,
                        "Please select a interval", Snackbar.LENGTH_SHORT).show()
                    else -> {
                        saveInterstitial(dialog, type)
                    }
                }
            } else {
                if (dialog.ad_unit.text.toString().isEmpty())
                    Snackbar.make(it, "Please enter a ad id", Snackbar.LENGTH_SHORT).show()
                else {
                    FirebaseDatabase.getInstance().getReference("Admin")
                        .child("Advertisement").child(type)
                        .child("ID").setValue(dialog.ad_unit.text.toString())
                    dialog.dismiss()
                }
            }
        }
        dialog.show()
    }

    private fun saveInterstitial(dialog: Dialog, type: String) {
        FirebaseDatabase.getInstance().getReference("Admin")
            .child("Advertisement").child(type)
            .child("ID").setValue(dialog.ad_unit.text.toString())
        FirebaseDatabase.getInstance().getReference("Admin")
            .child("Advertisement").child(type)
            .child("Interval").setValue(
                when (dialog.interval_click.checkedChipId) {
                    R.id.one -> 1
                    R.id.two -> 2
                    R.id.three -> 3
                    R.id.four -> 4
                    R.id.five -> 5
                    R.id.six -> 6
                    R.id.seven -> 7
                    R.id.eight -> 8
                    R.id.nine -> 9
                    R.id.ten -> 10
                    else -> 3
                })
        dialog.dismiss()
    }

    private fun activityRecreate() {
        Handler().postDelayed({
            val rec = Intent(this, SettingsActivity::class.java)
            startActivity(rec)
            Objects.requireNonNull(overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            ))
            finish()
        },200)
        val goBack = Intent()
        goBack.putExtra("THEME","CHANGE")
        setResult(Activity.RESULT_OK, goBack)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        overridePendingTransition(R.anim.slide_in_exit, R.anim.slide_out_exit)
    }

    fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)
}