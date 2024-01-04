package com.example.find_my_matzip.search

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentResSearchBinding
import com.example.find_my_matzip.model.RestaurantDto
import com.example.find_my_matzip.navTab.adapter.RestaurantRecyclerAdapter
import com.example.find_my_matzip.navTab.navTabFragment.RestaurantFragment
import com.example.find_my_matzip.search.adapter.ResSearchResultRecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ResSearchFragment : Fragment() {

    lateinit var binding: FragmentResSearchBinding
    lateinit var adapter: ResSearchResultRecyclerAdapter
    lateinit var restaurantList: Call<List<RestaurantDto>>
    lateinit var avgScoreList: Call<List<RestaurantDto>>

    private var text:String? = null
    var isLoading = false
    var isLastPage = false
    var currentPage = 0

    private val TAG: String = "ResSearchFragment"


    companion object {
        fun newInstance(text: String) =
            ResSearchFragment().apply {
                arguments = Bundle().apply {
                    putString("text", text)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"ResSearchFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentResSearchBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","ResSearchFragment onCreateView")
        binding = FragmentResSearchBinding.inflate(layoutInflater, container, false)

        //전달 받은 검색어
        val newText = arguments?.getString("text")
        Log.d(TAG, "newText : $newText")

        if(newText!!.isNotEmpty()){
            // 스크롤 리스너 설정
            binding.resSearchRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    handleScroll(recyclerView,newText)
                }
            })

            // 데이터 로드
            loadNextPageData(currentPage,newText)
        }

        return binding.root

    }//onCreateView

    private fun handleScroll(recyclerView: RecyclerView,newText:String) {
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
                loadNextPageData(currentPage,newText)
                Log.d("MyPageFragment", "스크롤 리스닝 확인 4")
                Log.d("MyPageFragment", "44 currentPage 전 :$currentPage ")
            }
        }
    }

    private fun loadNextPageData(page: Int,newText:String) {
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

                    adapter = ResSearchResultRecyclerAdapter(this@ResSearchFragment, restaurantList)
                    currentPage++

                    binding.resSearchRecyclerView.layoutManager = layoutManager

                    Log.d("sdotest", "${firstRestaurant.res_thumbnail}")
//                    binding.rankingRecyclerView.adapter = RankingRecyclerAdapter(requireContext(), restaurantList)
                    binding.resSearchRecyclerView.adapter = adapter

                } else if((restaurantList != null && restaurantList.isNotEmpty() && currentPage != 0)) {
                    adapter.addData(restaurantList)
                    currentPage++
                }

                
                //조회 결과가 있는지 여부 표시
                if (restaurantList?.isEmpty()!! && currentPage==0) {
                    //결과값이 비었다면
                    binding.noSearch.visibility = View.VISIBLE
                    binding.noSearch.text = " \" $newText \" 검색 결과 없음"
                }else{
                    //초기화
                    binding.noSearch.visibility = View.GONE
                    binding.noSearch.text = ""
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