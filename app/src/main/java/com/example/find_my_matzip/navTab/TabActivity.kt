package com.example.find_my_matzip.navTab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.example.find_my_matzip.navTab.navTabFragment.HomeFragment
import com.example.find_my_matzip.navTab.navTabFragment.MapFragment
import com.example.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.example.find_my_matzip.navTab.navTabFragment.RankingFragment
import com.example.find_my_matzip.navTab.navTabFragment.SearchReviewFragment
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.ActivityTabBinding

class TabActivity : AppCompatActivity() {
    lateinit var binding: ActivityTabBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 탭 레이아웃
        val tabLayout = binding.bottomNavigationView


        //val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        //bottomNavigationView.setOnNavigationItemSelectedListener {

        tabLayout.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tab1 -> {replaceFragment(HomeFragment())}
                R.id.tab2 -> {replaceFragment(MapFragment())}
                R.id.tab3 -> {replaceFragment(SearchReviewFragment())}
                R.id.tab4 -> {replaceFragment(RankingFragment())}
                R.id.tab5 -> {replaceFragment(MyPageFragment())}
                else -> false
            }
            return@setOnNavigationItemSelectedListener true
        }
        replaceFragment(HomeFragment())

    }//onCreate 끝

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}