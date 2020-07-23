package com.alim.writer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MenuItem
import android.view.Window
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.alim.writer.Adapter.MainPagerAdapter
import com.alim.writer.Cast.PostCast
import com.alim.writer.Cast.TabPosition
import com.alim.writer.Cast.Variable
import com.alim.writer.Class.CircleImageView
import com.alim.writer.Config.ConstantConfig
import com.alim.writer.DataHolder.AdapterData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.DataHolder.PostData
import com.alim.writer.DataHolder.ProfileData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Interfaces.Loaded
import com.bumptech.glide.Glide
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    lateinit var drawer: DrawerLayout
    private lateinit var viewPager: ViewPager
    private lateinit var tabLayout: TabLayout
    lateinit var userData: UserData
    lateinit var applicationData: ApplicationData

    lateinit var myRef: DatabaseReference
    lateinit var lRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    private val thread = Thread {
        DataReader(object : Loaded {
            override fun onLoaded(x: Int, snap: DataSnapshot) {
                AdminData(this@MainActivity).interstitialAdEnabled =
                    snap.child("Advertisement").child("Interstitial")
                        .child("Enabled").value.toString().toBoolean()
                AdminData(this@MainActivity).bannerAdEnabled =
                    snap.child("Advertisement").child("Banner")
                        .child("Enabled").value.toString().toBoolean()
                AdminData(this@MainActivity).nativeAdEnabled =
                    snap.child("Advertisement").child("Native")
                        .child("Enabled").value.toString().toBoolean()

                AdminData(this@MainActivity).bannerAd =
                    snap.child("Advertisement").child("Banner")
                        .child("ID").value.toString()
                AdminData(this@MainActivity).interstitialAd =
                    snap.child("Advertisement").child("Interstitial")
                        .child("ID").value.toString()
                AdminData(this@MainActivity).nativeAd =
                    snap.child("Advertisement").child("Native")
                        .child("ID").value.toString()
                val data = snap.child("Settings")

                AdminData(this@MainActivity).interstitialInterval = try {
                        snap.child("Advertisement").child("Interstitial")
                            .child("Interval").value.toString().toInt()
                } catch (e: Exception) {10}

                if (data.child("Approval Request").exists())
                    AdminData(this@MainActivity).approval = data.child(
                        "Approval Request").value.toString().toBoolean()
                if (data.child("Public Post").exists())
                    AdminData(this@MainActivity).publicPost = data.child(
                        "Public Post").value.toString().toBoolean()
                if (data.child("Comments").exists())
                    AdminData(this@MainActivity).commentsEnabled = data.child(
                        "Comments").value.toString().toBoolean()
                if (data.child("Image Size").exists())
                    AdminData(this@MainActivity).imageSize = data.child(
                        "Image Size").value.toString().toInt()
            }

        }).listen(FirebaseDatabase.getInstance().getReference("Admin"), 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        applicationData = ApplicationData(this)
        if (applicationData.theme)
            setTheme(R.style.AppThemeDark)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this)

        userData = UserData(this)
        database = FirebaseDatabase.getInstance()
        lRef = database.reference
        myRef = database.getReference("User Data").child(FirebaseAuth
            .getInstance().currentUser!!.uid).child("Post")

        tabLayout = findViewById(R.id.main_tab)
        viewPager = findViewById(R.id.view_pager)
        val cat = AdminData(this).getCategory()

        Thread {
            tabLayout.addTab(tabLayout.newTab().setText("Latest"))
            tabLayout.addTab(tabLayout.newTab().setText("Following"))
            tabLayout.addTab(tabLayout.newTab().setText("Videos"))
            for (x in 0 until cat.size) {
                tabLayout.addTab(tabLayout.newTab().setText(cat[x]))
            }
            lRef.child("Index").child("0").addValueEventListener(object
                : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    IndexData.primaryIndex = -1
                }

                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        IndexData.primaryIndex = p0.value.toString().toInt()
                    } catch (e: Exception) {
                        IndexData.primaryIndex = 1
                    }
                }
            })

            database.getReference("User Data").child(
                FirebaseAuth
                    .getInstance().currentUser!!.uid
            ).child("User")
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                    override fun onDataChange(snapshot: DataSnapshot) {
                        val data = snapshot.value.toString()
                        UserData(this@MainActivity).verified = data == "Verified User"
                    }
                })

            lRef.child("Index").child("00").addValueEventListener(object
                : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    IndexData.secondaryIndex = -1
                }

                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        IndexData.secondaryIndex = p0.value.toString().toInt()
                        Log.println(Log.ASSERT, "Register", "$p0")
                        PostCast().knock()
                    } catch (e: Exception) {
                        IndexData.secondaryIndex = 0
                    }
                }
            })

            myRef.child("0").addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    IndexData.secondaryIndex = -1
                }

                override fun onDataChange(p0: DataSnapshot) {
                    try {
                        IndexData.loop = p0.value.toString().toInt() + 1
                    } catch (e: Exception) {
                        IndexData.loop = 1
                    }
                }
            })
        }.start()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        drawer = findViewById(R.id.drawer)
        val navView = findViewById<NavigationView>(R.id.nav_view)
        val headerView = navView.getHeaderView(0)
        val ver =  headerView.findViewById<TextView>(R.id.version)
        val email =  headerView.findViewById<TextView>(R.id.extractor)
        val userName =  headerView.findViewById<TextView>(R.id.name)
        val contact =  navView.findViewById<TextView>(R.id.contact)
        val themeSwitch = headerView.findViewById<SwitchCompat>(R.id.change_theme)
        val profileImage = headerView.findViewById<CircleImageView>(R.id.profile_image)

        userName.text = userData.name
        email.text = userData.email

        contact.setOnClickListener {
            val intent = Intent(
                Intent.ACTION_SENDTO, Uri.fromParts(
                    "mailto",
                    ConstantConfig().contact, null
                )
            )
            startActivity(Intent.createChooser(intent, "Send email..."))
        }

        profileImage.setOnClickListener {
            val pIntent = Intent(this, ProfileActivity::class.java)
            pIntent.putExtra("UID", userData.uid)
            pIntent.putExtra("NAME", userData.name)
            startActivity(pIntent)
        }

        Glide.with(this).load(userData.image).into(profileImage)

        ver.text = BuildConfig.VERSION_NAME
        themeSwitch.isChecked = applicationData.theme

        if (intent.getBooleanExtra("SESSION", false))
            drawer.openDrawer(GravityCompat.START)

        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            applicationData.theme = isChecked
            reCreate()
        }

        val toggle = ActionBarDrawerToggle(this, drawer, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        navView.setNavigationItemSelectedListener(this)

        viewPager.adapter = MainPagerAdapter.ViewPagerAdapter(supportFragmentManager, cat.size+3)
        viewPager.offscreenPageLimit = cat.size+3
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL

        viewPager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) { /****/ }

            override fun onTabUnselected(tab: TabLayout.Tab?) { /****/ }

            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager.currentItem = tab.position
                TabPosition().setCat(tab.position, this@MainActivity)
            }
        })
        Thread(thread).start()
    }

    @SuppressLint("SimpleDateFormat")
    private fun post(title: String, post: String, link: String, type: String) {
        if (IndexData.loop > -1 && IndexData.primaryIndex > -1 && IndexData.secondaryIndex > -1) {
            val key = myRef.push().key!!
            val id = FirebaseAuth.getInstance().currentUser!!.uid
            val lf = myRef.child(IndexData.loop.toString())
            lf.child("Content").setValue(type)
            lf.child("Date").setValue(SimpleDateFormat(
                "dd MMM yyyy").format(Calendar.getInstance().time).toString())
            lf.child("Desc").setValue(post)
            lf.child("Title").setValue(title)
            lf.child("Url").setValue(link)
            lf.child("Name").setValue(userData.name)
            lf.child("Photo").setValue(userData.image)
            lf.child("Comment").setValue(key)
            lf.child("Comments").setValue(0)
            lRef.child("Comments").child(key).child("0").setValue(0)

            val primary = if (IndexData.secondaryIndex+1 > 10) IndexData.primaryIndex+1 else IndexData.primaryIndex
            val secondary = if (IndexData.secondaryIndex+1 > 10) 1 else IndexData.secondaryIndex+1

            lRef.child("Index").child("0").setValue(primary)
            lRef.child("Index").child("00").setValue(secondary)

            lRef.child("Index").child("$primary").child("$secondary").child("UID").setValue(id)
            lRef.child("Index").child("$primary").child("$secondary").child("POS").setValue(IndexData.loop)

            myRef.child("0").setValue(IndexData.loop)
        } else Toast.makeText(this,"Something Gone Wrong", Toast.LENGTH_SHORT).show()
    }

    private fun reCreate() {
        Handler().postDelayed({
            val rec = Intent(this, MainActivity::class.java)
            rec.putExtra("SESSION", true)
            Variable.recreate = true
            startActivity(rec)
            Objects.requireNonNull(overridePendingTransition(
                R.anim.fade_in,
                R.anim.fade_out
            ))
            finish()
        },200)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.nav_post -> postActivity()
            R.id.nav_share -> share()
            R.id.nav_logout -> logout()
            R.id.nav_settings -> launchSettings()
            R.id.nav_like -> likeMe()
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        return false
    }

    private fun likeMe() {
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

    private fun share() {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Writer")
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage = ("${shareMessage}https://play.google.com/store/apps/details?id="
                    +BuildConfig.APPLICATION_ID).trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: java.lang.Exception) {
            //e.toString();
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
            if (data!!.getStringExtra("THEME") == "CHANGE")
                reCreate()
            else if (data.getStringExtra("LAYOUT") == "CHANGE") {
                val rec = Intent(this, MainActivity::class.java)
                Variable.recreate = true
                intent.putExtra("SESSION", true)
                val loop = intent.getStringExtra("0")!!.toInt()
                rec.putExtra("0", loop.toString())
                for (x in 1..loop) rec.putExtra("$x", intent.getStringExtra("$x"))
                startActivity(rec)
                finish()
            }
        }
    }

    private fun launchSettings() {
        val intent = Intent(this, ThemeActivity::class.java)
        startActivityForResult(intent, 1)
    }

    private fun logout() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure want to Logout ?")
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .setPositiveButton("Yes") { _, _ ->
                FirebaseAuth.getInstance().signOut()
                ProfileData().clear()
                IndexData.index.clear()
                AdapterData.first = true
                AdapterData.pos = IndexData.primaryIndex - 1
                startActivity(Intent(this, SplashActivity::class.java))
                finish()
            }
            .show()
    }

    private fun postActivity() {
        if (AdminData(this).publicPost)
            startActivity(Intent(this, PostActivity::class.java))
        else Snackbar.make(findViewById<NavigationView>(R.id.nav_view)
            , "Public posts are disabled", Snackbar.LENGTH_SHORT).show()
    }

    private fun post() {
        drawer.closeDrawer(GravityCompat.START)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.post_layout)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val image = dialog.findViewById<RadioButton>(R.id.image)
        val youtube = dialog.findViewById<RadioButton>(R.id.youtube)
        val link = dialog.findViewById<TextInputEditText>(R.id.link)
        val title = dialog.findViewById<TextInputEditText>(R.id.title)
        val postT = dialog.findViewById<TextInputEditText>(R.id.post_text)

        dialog.findViewById<ImageView>(R.id.dismiss).setOnClickListener { dialog.dismiss() }
        dialog.findViewById<Button>(R.id.post).setOnClickListener {
            val t = title.text.toString()
            val p = postT.text.toString()
            val l = link.text.toString()

            val linkType = when { image.isChecked -> "Image"
                youtube.isChecked -> "Youtube"
                else -> "None" }

            if (t.isEmpty()) title.error = "Write a title"
            else if (p.isEmpty()) postT.error = "Write a Post"
            else { if (linkType != "None" && l == "") link.error = "Type a link"
                else if (linkType == "None" && l != "") link.error = "Select link type"
                else { post(t, p, l, linkType)
                    dialog.dismiss() } }
        }
        Handler().postDelayed({
            dialog.show()
        },250)
    }

    override fun onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START)
        else {
            AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure want to Exit ?")
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .setPositiveButton("Yes") { _, _ ->
                    PostData.pData.clear()
                    IndexData.index.clear()
                    AdapterData.first = true
                    AdapterData.pos = IndexData.primaryIndex - 1
                    super.onBackPressed()
                    Handler().postDelayed({
                        exitProcess(0)
                    }, 500)
                }
                .show()
        }
    }

    override fun onDestroy() {
        if (!userData.session)
            FirebaseAuth.getInstance().signOut()
        super.onDestroy()
    }
}