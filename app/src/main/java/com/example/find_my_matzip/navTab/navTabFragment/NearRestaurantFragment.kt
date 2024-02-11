package com.matzip.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.FragmentNearRestaurantBinding
import com.matzip.find_my_matzip.model.RestaurantDto
import com.matzip.find_my_matzip.navTab.adapter.NearRestaurantRecyclerAdapter
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class NearRestaurantFragment : BottomSheetDialogFragment() {

    lateinit var binding: FragmentNearRestaurantBinding
    lateinit var adapter: NearRestaurantRecyclerAdapter
    var restaurantsInsideCircle: List<RestaurantDto> = emptyList() // 이 부분을 추가하고 초기화

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = FragmentNearRestaurantBinding.inflate(layoutInflater)

        // restaurantsInsideCircle를 어댑터에 전달하여 초기화
        Log.d("restaurantsInsideCircle","$restaurantsInsideCircle")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentNearRestaurantBinding.inflate(layoutInflater, container, false)

        val nearRestaurantList = restaurantsInsideCircle

        // TextView에 식당 갯수 설정
        binding.bottomSheetTitleTextView.text = "범위 내 식당 (${nearRestaurantList.size}개)"


        if (nearRestaurantList.isNotEmpty()) {
            val firstRestaurant = nearRestaurantList[0]
            val layoutManager = LinearLayoutManager(requireContext())
            binding.nearResListRecyclerView.layoutManager = layoutManager
            adapter = NearRestaurantRecyclerAdapter(this, nearRestaurantList)
            binding.nearResListRecyclerView.adapter = adapter
        } else {
            // restaurantsInsideCircle가 비어있을 때의 처리
            // 예: 사용자에게 어떤 메시지를 보여주거나 다른 작업 수행
        }

        return binding.root
    }
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        // BottomSheet의 레이아웃을 가져오기
        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)

        // BottomSheetBehavior 가져오기
        val bottomSheetBehavior = bottomSheet?.let { BottomSheetBehavior.from(it) }

        // 최초에 보여지는 높이 설정
        bottomSheetBehavior?.peekHeight = resources.getDimensionPixelSize(R.dimen.peek_height)

        // BottomSheet 상태 변경 리스너 등록
        bottomSheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // 상태 변경에 따른 동작 처리
                when (newState) {
                    BottomSheetBehavior.STATE_COLLAPSED -> {
                        // BottomSheet가 축소된 상태
                    }
                    BottomSheetBehavior.STATE_EXPANDED -> {
                        // BottomSheet가 확장된 상태
                    }
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // 슬라이드 중일 때의 동작 처리
            }
        })
    }
}