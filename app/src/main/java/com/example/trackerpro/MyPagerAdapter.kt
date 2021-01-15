package com.example.trackerpro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MyPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int) : Fragment {

        return when (position){
            0 -> {
                ClassicModeFragment()
            }
            1 -> {
                DistanceModeFragment()
            }
            2 -> {
                TimeModeFragment()
            }
            else -> {
                return CaloriesModeFragment()
            }
        }
    }

    override fun getCount() : Int {
        return 4
    }

    override fun getPageTitle(position: Int) : CharSequence {
        return when (position) {
            0 -> "Classic"
            1 -> "Distance"
            2 -> "Time"
            else ->{
                return "Calories"
            }
        }
    }
}