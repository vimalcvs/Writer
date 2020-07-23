package com.alim.writer.Fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.RecyclerAdapter
import com.alim.writer.Cast.PostCast
import com.alim.writer.Cast.Variable
import com.alim.writer.Class.CircleImageView
import com.alim.writer.DataHolder.AdapterData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.DataReader
import com.alim.writer.Firebase.Indexer
import com.alim.writer.Interfaces.Loaded
import com.alim.writer.Interfaces.OnScroll
import com.alim.writer.Model.IndexModel
import com.alim.writer.PostActivity
import com.alim.writer.ProfileActivity
import com.alim.writer.R
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.android.gms.ads.MobileAds
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LatestFragment : Fragment() {

    var pos = 0
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecyclerAdapter
    private lateinit var layoutManager: RecyclerView.LayoutManager

    private lateinit var mInterstitialAd: InterstitialAd
    lateinit var myId: String
    lateinit var iRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_latest, container, false)
        mInterstitialAd = InterstitialAd(activity!!)
        mInterstitialAd.adUnitId = AdminData(activity!!).interstitialAd
        if (AdminData(activity!!).interstitialAdEnabled)
            mInterstitialAd.loadAd(AdRequest.Builder().build())

        pos = IndexData.primaryIndex
        myId = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance()
        //myRef = database.getReference("User Data")
        iRef = database.getReference("Index")

        val userData = UserData(activity!!)
        layoutManager = LinearLayoutManager(activity!!)
        recyclerView = rootView.findViewById(R.id.main_recycler)

        val profile = rootView.findViewById<CircleImageView>(R.id.profile)

        Glide.with(activity!!).load(userData.image)
            .dontAnimate()
            .centerCrop().into(profile)

        profile.setOnClickListener {
            val pIntent = Intent(activity!!, ProfileActivity::class.java)
            pIntent.putExtra("UID", userData.uid)
            pIntent.putExtra("NAME", userData.name)
            startActivity(pIntent)
        }

        rootView.findViewById<View>(R.id.open_post).setOnClickListener {
            if (AdminData(activity!!).publicPost)
                startActivity(Intent(activity!!, PostActivity::class.java))
            else Snackbar.make(it, "Public posts are disabled", Snackbar.LENGTH_SHORT).show()
        }

        recyclerView.layoutManager = layoutManager
        adapter = RecyclerAdapter(activity!!, mInterstitialAd)
        recyclerView.adapter = adapter

        rootView.findViewById<NestedScrollView>(R.id.scroll_s)
            .isVerticalScrollBarEnabled = ApplicationData(activity!!).scrollbar

        if (IndexData.index.size == 0) {
            Indexer().index(activity!!, IndexData.primaryIndex, iRef, object : OnScroll {
                override fun scrolled() {
                    activity!!.runOnUiThread {
                        adapter.notifyItemInserted(0)
                    }
                }
            })
        }

        PostCast().register(object : OnScroll {
            override fun scrolled() {
                DataReader(object: Loaded {
                    override fun onLoaded(x: Int, data: DataSnapshot) {
                        if (data.child("${IndexData.secondaryIndex}").exists()
                            && !AdapterData.first && !Variable.recreate) {
                            val indexModel = IndexModel()
                            indexModel.pos = data.child("${IndexData.secondaryIndex}").child("POS").value.toString()
                            indexModel.uid = data.child("${IndexData.secondaryIndex}").child("UID").value.toString()
                            IndexData.index.add(0, indexModel)
                            if (AdapterData.aPos < 4) {
                                recyclerView.scrollToPosition(0)
                                adapter.notifyItemInserted(0)
                            } else {
                                Snackbar.make(rootView, "New Post added", Snackbar.LENGTH_LONG)
                                    .setAction("See") {
                                        recyclerView.scrollToPosition(0)
                                        adapter.notifyItemInserted(0)
                                    }.show()
                            }
                        }
                        AdapterData.first = false
                    }
                }).listenOnce(iRef.child("${IndexData.primaryIndex}"))
            }
        })

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                UserData(activity!!).interval = 0
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                super.onAdClosed()
            }
        }

        return rootView
    }
}