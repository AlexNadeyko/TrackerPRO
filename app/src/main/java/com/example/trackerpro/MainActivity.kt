package com.example.trackerpro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            val fragment = TrackerFragment()
            supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                .commit()
        }

//        loadFragment(TrackerFragment())

        val navigationMenuView: BottomNavigationView = findViewById(R.id.navigationView)

        val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_tracker -> {
                    val fragment = TrackerFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_statisctics -> {
                    val fragment = StatisticsFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_ranking -> {
                    val fragment = RankingFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }

                R.id.navigation_profile -> {
                    val fragment = ProfileFragment()
                    supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }


        navigationMenuView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener )



//        navigationMenuView.setOnNavigationItemSelectedListener { item ->
//            when (item.itemId) {
//                R.id.navigation_tracker -> {
//                    loadFragment(TrackerFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//
//                R.id.navigation_statisctics -> {
//
////                    loadFragment(com.example.trackerpro.StatisticsFragment())
//                    loadFragment(TrackerFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//
//                R.id.navigation_ranking -> {
//
////                    loadFragment(RankingFragment())
//                    loadFragment(TrackerFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//
//                R.id.navigation_profile -> {
//
////                    loadFragment(ProfileFragment())
//
//                    loadFragment(TrackerFragment())
//                    return@setOnNavigationItemSelectedListener true
//                }
//
//            }
//            false
//
//        }
    }

//    private fun loadFragment(fragment: Fragment) {
//        // load fragment
////        val transaction = supportFragmentManager.beginTransaction()
////        transaction.replace(R.id.container, fragment)
////        transaction.addToBackStack(null)
////        transaction.commit()
//
//        supportFragmentManager.beginTransaction().replace(R.id.container, fragment, fragment.javaClass.getSimpleName())
//            .commit()
//    }

}