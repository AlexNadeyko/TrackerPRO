package com.example.trackerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout


class TrackerFragment : Fragment() {

    private lateinit var viewPager: ViewPager
    private lateinit var tabs: TabLayout

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view: View = inflater.inflate(R.layout.fragment_tracker, container, false)

        viewPager = view.findViewById(R.id.viewpager_tracking)
        tabs = view.findViewById(R.id.tabs_tracking)

        val fragmentAdapter = MyPagerAdapter(childFragmentManager)

        viewPager.adapter = fragmentAdapter
        tabs.setupWithViewPager(viewPager)

        return view
    }

}