package com.matzip.find_my_matzip.navTab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.matzip.find_my_matzip.navTab.navTabFragment.HomeFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.MapFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.RankingFragment
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ActivityTabBinding
import com.matzip.find_my_matzip.navTab.navTabFragment.RestaurantFragment

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
                R.id.tab2 -> {replaceFragment(RestaurantFragment())}
                R.id.tab3 -> {replaceFragment(MapFragment())}
                R.id.tab4 -> {replaceFragment(RankingFragment())}
                R.id.tab5 -> {replaceFragment(MyPageFragment())}
                else -> false
            }
            return@setOnNavigationItemSelectedListener true
        }
        replaceFragment(HomeFragment())

    }//onCreate 끝

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 프래그먼트가 이미 백 스택에 있는지 확인
        val fragmentTag = fragment.javaClass.simpleName
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

        if (currentFragment != null && currentFragment.javaClass.simpleName == fragmentTag) {
            // 프래그먼트가 이미 백 스택에 있다면 아무것도 하지 않음
            return
        }

        // 백 스택에서 프래그먼트를 제거
        val fragmentInBackStack = fragmentManager.findFragmentByTag(fragmentTag)
        if (fragmentInBackStack != null) {
            fragmentTransaction.remove(fragmentInBackStack)
        }

        // 현재 프래그먼트를 새로운 것으로 교체
        fragmentTransaction.replace(R.id.fragmentContainer, fragment, fragmentTag)
    //    fragmentTransaction.addToBackStack(null) // 백 스택에 추가

        // 트랜잭션을 적용
        fragmentTransaction.commit()
    } // replaceFragment 끝
}