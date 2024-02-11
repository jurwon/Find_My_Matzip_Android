package com.matzip.find_my_matzip.search

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.matzip.find_my_matzip.HomeTabActivity
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ActivitySearchBinding
import com.matzip.find_my_matzip.model.SearchDto
import com.matzip.find_my_matzip.search.adapter.SearchHistoryRecyclerViewAdapter
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.google.android.material.tabs.TabLayout
import java.text.SimpleDateFormat
import java.util.Locale

class SearchActivity : AppCompatActivity(),
    SearchHistoryRecyclerViewAdapter.OnSearchItemClickListener{
    lateinit var binding : ActivitySearchBinding
    lateinit var adapter: SearchHistoryRecyclerViewAdapter

    private var searchType = "default"
    private val TAG:String = "SearchActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //검색 기록 저장(초기값은 true로 설정)
        binding.switchBtn.isChecked = true
        if (SharedPreferencesManager.getAutoSearch() == null) {
            SharedPreferencesManager.setAutoSearch(true)
        }

        //home으로 이동
        binding.homeBtn.setOnClickListener(){
            val intent = Intent(this@SearchActivity, HomeTabActivity::class.java)
            startActivity(intent)
        }

        checkSearchType()


        //searchView에서 검색클릭 -> text가지고 searchView exit
        binding.searchView
            .editText
            .setOnEditorActionListener {v,actionId,event ->
                val searchText = binding.searchView.text.toString()

                binding.searchBar.setText(searchText)
                binding.searchView.hide()

                if(binding.searchBar.text.isNullOrEmpty()){
                    searchType = "default"
                }else{
                    searchType = "board"
                }

                if(SharedPreferencesManager.getAutoSearch() == true){
                    //최근 검색어에 저장
                    SharedPreferencesManager.saveSearchHistory(searchText)
                    Log.d(TAG,"searchText 저장완료 : saveSearchHistory($searchText)")
                    //변경된 SharedPreference 로딩
                    showSearchHistory()
                    Log.d(TAG,"autoSearch 들어옴")
                }

                checkSearchType()
                false
            }


        binding.tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                // Tab이 선택되었을 때
                when (tab!!.position) {
                    0 -> {
                        searchType = "board"
                        checkSearchType()
                    }
                    1 -> {
                        searchType = "user"
                        checkSearchType()
                    }
                    2 ->{
                        searchType = "restaurant"
                        checkSearchType()
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                // Tab이 선택되지 않았을 때
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                // Tab이 다시 선택되었을 때
            }
        })

        binding.deleteAllBtn.setOnClickListener{
            //모든 검색 기록 삭제
            deleteAllSearchHistory()
        }

        //searchView의 모든 검색 기록 삭제
        binding.deleteAllBtnSearchView.setOnClickListener{
            //모든 검색 기록 삭제
            deleteAllSearchHistory()
        }

        //검색 기록 자동저장 기능
        binding.switchBtn.setOnCheckedChangeListener { _, isChecked ->
            SharedPreferencesManager.setAutoSearch(isChecked)
        }




    }//onCreateView

    //창 전환(3가지) 메서드
    // searchType -> "board", "user","default"
    private fun checkSearchType(){

        when(searchType){
            "default" ->{
                Log.d(TAG,"default")
                binding.currentView.visibility = View.VISIBLE
                binding.fragChange.visibility = View.GONE

                showSearchHistory()
            }
            "board" ->{
                Log.d(TAG,"board")
                binding.currentView.visibility = View.GONE
                binding.fragChange.visibility = View.VISIBLE
                replaceFragment(BoardSearchFragment.newInstance(binding.searchBar.text.toString()))
                binding.tabs.getTabAt(0)?.select()
            }
            "user" -> {
                Log.d(TAG,"user")
                binding.currentView.visibility = View.GONE
                binding.fragChange.visibility = View.VISIBLE
                replaceFragment(UserSearchFragment.newInstance(binding.searchBar.text.toString()))
                binding.tabs.getTabAt(1)?.select()
            }
            "restaurant" -> {
                Log.d(TAG,"restaurant")
                binding.currentView.visibility = View.GONE
                binding.fragChange.visibility = View.VISIBLE
                replaceFragment(ResSearchFragment.newInstance(binding.searchBar.text.toString()))
                binding.tabs.getTabAt(2)?.select()
            }
            else -> searchType = "default"
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        Log.d(TAG,"replaceFragment 실행")
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    //최근 검색어 보기
    private fun showSearchHistory(){
        Log.d(TAG,"showSearchHistory 실행")

        val existingSet = SharedPreferencesManager.getSearchHistory()

        //형변환(Hash -> ArrayList)
        val searchDtoList = existingSet?.map {
            val (text, dateString) = it.split(",")

            val parseFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US)
            SearchDto(text, parseFormat.parse(dateString))
        }?.sortedByDescending {
            it.date
        }

        //searchActivity
        val layoutManager = LinearLayoutManager(this)

        adapter = SearchHistoryRecyclerViewAdapter(this@SearchActivity, searchDtoList)
        adapter.setOnSearchItemClickListener(this)

        binding.searchHistoryRecyclerView.layoutManager = layoutManager
        binding.searchHistoryRecyclerView.adapter = adapter

        //searchView
        val layoutManagerSearchView = LinearLayoutManager(this)

        //adapter2 = SearchHistoryRecyclerViewAdapter(this@SearchActivity, searchDtoList)
        //adapter2.setOnSearchItemClickListener(this)

        binding.searchHistoryRecyclerSearchView.layoutManager = layoutManagerSearchView
        binding.searchHistoryRecyclerSearchView.adapter = adapter

    }

    //최근 검색어 전체 삭제
    private fun deleteAllSearchHistory(){
        SharedPreferencesManager.clearSearchPreferences()
        adapter.clearData()
        adapter.notifyDataSetChanged()
    }

    //검색어 클릭시 로직
    override fun onSearchItemClick(item: String) {
        binding.currentView.visibility = View.GONE
        binding.fragChange.visibility = View.VISIBLE

        searchType = "board"
        binding.searchBar.setText(item)
        binding.searchView.hide()

        checkSearchType()
    }



}