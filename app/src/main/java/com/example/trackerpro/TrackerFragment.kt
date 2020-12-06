package com.example.trackerpro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2

//import kotlinx.android.synthetic.main.fragmnet_tracker.*


class TrackerFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_tracker, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        val viewpager_tracking = R.id.viewpager_tracking
//        val fragmentAdapter = MyPagerAdapter()
//        viewpager_tracking.adapter = fragmentAdapter
//
//        tabs_tracking.setupWithViewPager(viewpager_tracking)
    }

}