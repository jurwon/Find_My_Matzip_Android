package com.matzip.find_my_matzip.navTab.navTabFragment

import android.app.AlertDialog
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matzip.find_my_matzip.HomeTabActivity
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.FragmentRankingBinding
import com.matzip.find_my_matzip.model.RankingDto
import com.matzip.find_my_matzip.navTab.adapter.RankingRecyclerAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RankingFragment : Fragment() {
    lateinit var binding :FragmentRankingBinding
    lateinit var adapter: RankingRecyclerAdapter
//    lateinit var restaurantService: RestaurantService
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = FragmentRankingBinding.inflate(layoutInflater)
//        restaurantService =  (requireContext().applicationContext as MyApplication).restaurantService
//    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","onCreateView onDestroy")
        binding = FragmentRankingBinding.inflate(layoutInflater, container, false)

//        val restaurantService =
//            (requireContext().applicationContext as MyApplication).restaurantService
        val restaurantService = (context?.applicationContext as MyApplication).restaurantService

        val restaurantList = restaurantService.getTop3RestaurantsByAvgScore()
        Log.d("RankingFragment", "restaurantList.enqueue 호출전 : ")
        restaurantList.enqueue(object : Callback<List<RankingDto>> {
            override fun onResponse(
                call: Call<List<RankingDto>>,
                response: Response<List<RankingDto>>
            ) {
                Log.d("RankingFragment", "도착 확인1: ")
                val restaurantList = response.body()
                Log.d("RankingFragment", "도착 확인2: restaurantList ${restaurantList}")
                if (restaurantList != null && restaurantList.isNotEmpty()) {
                    val firstRestaurant = restaurantList[0]
                    Log.d("RankingFragment", "restaurantList의 값: ${firstRestaurant.resId}")
                    Log.d("RankingFragment", "restaurantList의 값: ${firstRestaurant.avgScore}")
                    Log.d("RankingFragment", "restaurantList의 값: ${firstRestaurant.resName}")
                    Log.d("RankingFragment", "restaurantList의 값: ${firstRestaurant.resThumbnail}")
                    Log.d("RankingFragment", "Full Response: $restaurantList")

//                    val layoutManager = LinearLayoutManager(requireContext())
//                    binding.rankingRecyclerView.layoutManager = layoutManager
//                    adapter = RankingRecyclerAdapter(this@RankingFragment,restaurantList)
//                    binding.rankingRecyclerView.adapter = adapter

                    val spanCount = 2 // 그리드의 열 수를 지정합니다.
                    val spacing = resources.getDimensionPixelSize(R.dimen.grid_spacing) // 간격 크기를 지정합니다.

                    val layoutManager = GridLayoutManager(requireContext(), spanCount)
                    layoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                        override fun getSpanSize(position: Int): Int {
                            return 1 // 각 아이템의 차지하는 span 크기를 지정
                        }
                    }

                    binding.rankingRecyclerView.layoutManager = layoutManager
                    binding.rankingRecyclerView.addItemDecoration(object : RecyclerView.ItemDecoration() {
                        override fun getItemOffsets(
                            outRect: Rect,
                            view: View,
                            parent: RecyclerView,
                            state: RecyclerView.State
                        ) {
                            val position = parent.getChildAdapterPosition(view)
                            if (position % spanCount == 0) {
                                // 첫 번째 열의 아이템인 경우 왼쪽에 간격 추가
                                outRect.left = spacing
                            } else {
                                // 나머지 열의 아이템인 경우 오른쪽에 간격 추가
                                outRect.left = spacing
                                outRect.right = spacing
                            }

                            // 모든 아이템 상단에 간격 추가
                            outRect.top = spacing
                        }
                    })
                    adapter = RankingRecyclerAdapter(this@RankingFragment, restaurantList)
                    binding.rankingRecyclerView.adapter = adapter


                } else {
                    Log.e("RankingFragment", "Response body is null or empty.")

                }
            }

            override fun onFailure(call: Call<List<RankingDto>>, t: Throwable) {
                t.printStackTrace()
                call.cancel()
                Log.e("RankingFragment", " 통신 실패")
            }
        })

        return binding.root
    }
    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","RankingFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","RankingFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","RankingFragment onDestroy")
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