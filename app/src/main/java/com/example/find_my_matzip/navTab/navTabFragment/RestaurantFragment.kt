package com.example.find_my_matzip.navTab.navTabFragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentRestaurantBinding
import com.example.find_my_matzip.model.RestaurantDto
import com.example.find_my_matzip.navTab.adapter.RestaurantRecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RestaurantFragment : Fragment() {
    lateinit var binding: FragmentRestaurantBinding
    lateinit var adapter: RestaurantRecyclerAdapter
    lateinit var restaurantList: Call<List<RestaurantDto>>
    lateinit var avgScoreList: Call<List<RestaurantDto>>
    private var text:String? = null
    var isLoading = false
    var isLastPage = false
    var currentPage = 0

    companion object {
        fun newInstance(text: String): RestaurantFragment {
            val fragment = RestaurantFragment()
            val args = Bundle()
            args.putString("text", text)
            Log.d("RestaurantFragment", "내가 newInstance에 넣은 text : $text")
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","RestaurantFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentRestaurantBinding.inflate(layoutInflater)
    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","RestaurantFragment onCreateView")
        binding = FragmentRestaurantBinding.inflate(layoutInflater, container, false)

        //검색창에서 넘어왔다면 text넣어줌
        val newText = arguments?.getString("text")
        Log.d("RestaurantFragment", "newText : $newText")
        if(newText != null){
            text = newText
        }

        // 스크롤 리스너 설정
        binding.resListRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll(recyclerView)
            }
        })

        // 데이터 로드
        loadNextPageData(currentPage)




        //검색창
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                // 검색 버튼 누를 때 호출
                if (!query.isNullOrBlank()) {
                    navigateSearchResult(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색창에서 글자가 변경이 일어날 때마다 호출
                //Log.d(TAG, "글자 변경 : $newText")
                return true
            }
        })

        return binding.root
    }

    private fun handleScroll(recyclerView: RecyclerView) {
        Log.d("RestaurnatFragment", "스크롤 리스닝 확인 1")
        val layoutManager = LinearLayoutManager::class.java.cast(recyclerView.layoutManager)
        val visibleItemCount = layoutManager?.childCount ?: 0
        val totalItemCount = layoutManager?.itemCount ?: 0
        val firstVisibleItemPosition = layoutManager?.findFirstVisibleItemPosition() ?: 0
        Log.d("MyPageFragment", "스크롤 리스닝 확인 2")
        Log.d("MyPageFragment", "isLoading : $isLoading, isLastPage : $isLastPage ")
        Log.d("MyPageFragment", "visibleItemCount : $visibleItemCount, firstVisibleItemPosition : $firstVisibleItemPosition")
        Log.d("MyPageFragment", "totalItemCount : $totalItemCount ")
        if (!isLoading && !isLastPage) {
            if (visibleItemCount + firstVisibleItemPosition >= totalItemCount
                && firstVisibleItemPosition >= 0
            ) {
                currentPage++
                Log.d("MyPageFragment", "33 currentPage 전 :$currentPage ")
                Log.d("MyPageFragment", "스크롤 리스닝 확인 3")
                loadNextPageData(currentPage)
                Log.d("MyPageFragment", "스크롤 리스닝 확인 4")
                Log.d("MyPageFragment", "44 currentPage 전 :$currentPage ")
            }
        }
    }

    private fun loadNextPageData(page: Int) {
        isLoading = true

        val restaurantService = (context?.applicationContext as MyApplication).restaurantService

        if (text != null) {
            //검색 단어가 있을 때
            Log.d("RestaurantFragment", "검색중 $text")
            restaurantList = restaurantService.getSearchRestaurantsByAvgScore(text!!,page)
            avgScoreList = restaurantService.getSearchRestaurantsByAvgScore(text!!,page)

        } else {
            //전체 조회
            Log.d("RestaurantFragment", "전체 조회")
            restaurantList = restaurantService.getAllPageRestaurantsByAvgScore(page)
            avgScoreList = restaurantService.getAllPageRestaurantsByAvgScore(page)
        }


        restaurantList.enqueue(object : Callback<List<RestaurantDto>> {
            override fun onResponse(
                call: Call<List<RestaurantDto>>,
                response: Response<List<RestaurantDto>>
            ) {
                isLoading = false
                val restaurantList = response.body()
                if (restaurantList != null && restaurantList.isNotEmpty() && currentPage == 0) {
                    val firstRestaurant = restaurantList[0]
                    val layoutManager = LinearLayoutManager(requireContext())

                    adapter = RestaurantRecyclerAdapter(this@RestaurantFragment, restaurantList)
                    currentPage++

                    binding.resListRecyclerView.layoutManager = layoutManager

                    Log.d("sdotest", "${firstRestaurant.res_thumbnail}")
//                    binding.rankingRecyclerView.adapter = RankingRecyclerAdapter(requireContext(), restaurantList)
                    binding.resListRecyclerView.adapter = adapter

                } else if((restaurantList != null && restaurantList.isNotEmpty() && currentPage != 0)) {
                    adapter.addData(restaurantList)
                    currentPage++
                }
            }

            override fun onFailure(call: Call<List<RestaurantDto>>, t: Throwable) {
                isLoading = false
                t.printStackTrace()
                call.cancel()
                Log.e("RestaurantFragment", " 통신 실패")
            }
        })


    }

    //fragment전환
    private fun navigateSearchResult(text:String) {
        val changeFragment = RestaurantFragment.newInstance(text)
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainer, changeFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }


    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","RestaurantFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","RestaurantFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","RestaurantFragment onDestroy")
        super.onDestroy()
    }
    fun showExitDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Exit?")
        builder.setMessage("앱을 종료하시겠습니까?")
        builder.setNegativeButton("아니오") { dialog, which ->
            // 아무 작업도 수행하지 않음
        }
        builder.setPositiveButton("예") { dialog, which ->
            // 프래그먼트가 호스트하는 액티비티의 onBackPressed() 호출
            (requireActivity() as? HomeTabActivity)?.onBackPressed2()
        }
        builder.show()
    }



}