package com.alim.writer.Fragment.Category

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.alim.writer.Adapter.ExtraAdapter
import com.alim.writer.Adapter.YoutubeAdapter
import com.alim.writer.Cast.TabPosition
import com.alim.writer.DataHolder.AdapterData
import com.alim.writer.DataHolder.IndexData
import com.alim.writer.DataHolder.VideoIndexData
import com.alim.writer.Database.AdminData
import com.alim.writer.Database.ApplicationData
import com.alim.writer.Database.UserData
import com.alim.writer.Firebase.Indexer
import com.alim.writer.Interfaces.OnScroll
import com.alim.writer.R
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.InterstitialAd
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class ExtraFragmentEight : Fragment() {

    var pos = 0
    var knock = false
    lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ExtraAdapter
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var layoutManager: RecyclerView.LayoutManager

    lateinit var myId: String
    lateinit var iRef: DatabaseReference
    lateinit var database: FirebaseDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_extra, container, false)
        mInterstitialAd = InterstitialAd(activity!!)
        mInterstitialAd.adUnitId = AdminData(activity!!).interstitialAd
        if (AdminData(activity!!).interstitialAdEnabled)
            mInterstitialAd.loadAd(AdRequest.Builder().build())

        pos = IndexData.primaryIndex
        myId = FirebaseAuth.getInstance().currentUser!!.uid
        database = FirebaseDatabase.getInstance()
        //myRef = database.getReference("User Data")
        iRef = database.getReference("Index")

        layoutManager = LinearLayoutManager(activity!!)
        recyclerView = rootView.findViewById(R.id.main_recycler)
        recyclerView.layoutManager = layoutManager
        adapter = ExtraAdapter(activity!!, AdminData(activity!!).getCategory()[7], mInterstitialAd)
        recyclerView.isVerticalScrollBarEnabled = ApplicationData(activity!!).scrollbar
        recyclerView.adapter = adapter

        mInterstitialAd.adListener = object : AdListener() {
            override fun onAdClosed() {
                UserData(activity!!).interval = 0
                mInterstitialAd.loadAd(AdRequest.Builder().build())
                super.onAdClosed()
            }
        }


        return rootView
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        if (isVisibleToUser) {
            if (IndexData.index.size == 0 && AdapterData.pos > 0) index()
            else if (IndexData.index.size > 0 && !knock) {
                adapter.notifyDataSetChanged()
                knock = true
            }
        }
        super.setUserVisibleHint(isVisibleToUser)
    }

    fun index() {
        Indexer().index(activity!!, AdapterData.pos, iRef, object : OnScroll {
            override fun scrolled() {
                AdapterData.pos--
                adapter.notifyItemInserted(0)
                if (IndexData.index.size == 0 && AdapterData.pos > 0) index()
            }
        })
    }
}