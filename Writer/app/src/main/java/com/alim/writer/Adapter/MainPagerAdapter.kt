package com.alim.writer.Adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.alim.writer.Fragment.*
import com.alim.writer.Fragment.Category.*

class MainPagerAdapter {
    class ViewPagerAdapter(fm: FragmentManager?, maximum: Int) :
        FragmentPagerAdapter(fm!!) {
        private val max = maximum
        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> LatestFragment()
                1 -> FollowingFragment()
                2 -> VideosFragment()
                3 -> ExtraFragmentOne()
                4 -> ExtraFragmentTwo()
                5 -> ExtraFragmentThree()
                6 -> ExtraFragmentFour()
                7 -> ExtraFragmentFive()
                8 -> ExtraFragmentSix()
                9 -> ExtraFragmentSeven()
                10 -> ExtraFragmentEight()
                11 -> ExtraFragmentNine()
                12 -> ExtraFragmentTen()
                else -> LatestFragment()
            }
        }
        override fun getCount(): Int {
            return max
        }
    }
}