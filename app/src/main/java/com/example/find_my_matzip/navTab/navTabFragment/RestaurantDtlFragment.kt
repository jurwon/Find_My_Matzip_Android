package com.matzip.find_my_matzip.navTab.navTabFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.WriteReviewFragment
import com.matzip.find_my_matzip.databinding.FragmentRestaurantDtlBinding
import com.matzip.find_my_matzip.model.RestaurantDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class RestaurantDtlFragment : Fragment() {
    private var resId: Long = 0L
    lateinit var binding : FragmentRestaurantDtlBinding

    //resId 값으로 식당상세페이지 이동
    companion object {
        fun newInstance(resId: Long): RestaurantDtlFragment {
            val fragment = RestaurantDtlFragment()
            val args = Bundle()
            args.putLong("resId", resId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentRestaurantDtlBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        resId = arguments?.getLong("resId") ?: 0L
        binding = FragmentRestaurantDtlBinding.inflate(layoutInflater,container,false)

        val restaurantService = (context?.applicationContext as MyApplication).restaurantService
        val restaurantDtl = arguments?.getLong("resId")?.let { restaurantService.getRestaurantDtl(it) }

        Log.d("RestaurantDtlFragment","restaurantDtl.enqueue 호출 전 : $restaurantDtl")

        restaurantDtl?.enqueue(object : Callback<RestaurantDto>{
            override fun onResponse(call: Call<RestaurantDto>, response: Response<RestaurantDto>) {

                Log.d("RestaurantDtlFragment","레스토랑 도착 확인.")
                val restaurantDto = response.body()
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인1. : restaurantDto $restaurantDto")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인2. : restaurantDto.res_id ${restaurantDto?.res_id}")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인3. : restaurantDto.operate_time ${restaurantDto?.operate_time}")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인4. : restaurantDto.res_address ${restaurantDto?.res_address}")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인5. : restaurantDto.res_district ${restaurantDto?.res_district}")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인6. : restaurantDto.res_intro ${restaurantDto?.res_intro}")
                Log.d("RestaurantDtlFragment","레스토랑 도착 확인7. : restaurantDto.avgScore ${restaurantDto?.avgScore}")

                binding.resName.text = restaurantDto?.res_name.toString()
                binding.resAddress.text = restaurantDto?.res_address.toString()
                binding.operationTime.text = restaurantDto?.operate_time.toString()
                binding.resMenu.text = restaurantDto?.res_menu.toString()
                binding.resPhone.text = restaurantDto?.res_phone.toString()
                binding.resIntro.text = restaurantDto?.res_intro.toString()

                //소수 둘째자리에서 반올림
                val ratingUpScore = restaurantDto?.avgScore?.toFloat()?.let {
                    kotlin.math.round(it * 10) / 10.toFloat()}!!
                binding.resScoreText.text = ratingUpScore.toString()
                binding.resScoreRating.rating = ratingUpScore

                Glide.with(requireContext())
                    .load(restaurantDto?.res_thumbnail)
                    .override(900, 900)
                    .into(binding.resThumbnail)

                Log.d("RestaurantDtlFragment", "도착 확인2: res_thumbnail ${restaurantDto?.res_thumbnail}")

                binding.toWriteReview.setOnClickListener {
                    Log.d("RestaurantDtlFragment", "게시글작성가기 클릭됨")
                    Log.d("RestaurantDtlFragment", "resId: ${restaurantDto?.res_id}")
                    val resId = restaurantDto?.res_id
                    if (resId == 0L) {
                        Log.d("RestaurantDtlFragment", "resId is 비엇다~")
                    }else{
                        val fragment = WriteReviewFragment.newInstance(resId)
                        parentFragmentManager.beginTransaction()
                            .replace(com.matzip.find_my_matzip.R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }

                binding.mapBtn.setOnClickListener {
                   // Toast.makeText(context,"지도뿅",Toast.LENGTH_SHORT).show()

                    val res_lat = restaurantDto?.res_lat.toString()
                    val res_lng = restaurantDto?.res_lng.toString()

                    val mapCardViewFragment = MapCardViewFragment()

                    // 데이터를 전달하기 위한 Bundle 생성
                    val bundle = Bundle().apply {
                        putString("res_lat", res_lat)
                        putString("res_lng", res_lng)
                    }

                    // MapCardViewFragment에 Bundle 설정
                    mapCardViewFragment.arguments = bundle

                    // MapCardViewFragment의 크기를 지정하여 추가
                    val transaction = parentFragmentManager.beginTransaction()
                    transaction.add(
                        com.matzip.find_my_matzip.R.id.fragmentContainer, // 프래그먼트를 표시할 레이아웃 ID
                        mapCardViewFragment,
                        "MapCardViewFragment"
                    )

                    // MapCardViewFragment 크기 지정
                    val cardViewLayoutParams = ViewGroup.LayoutParams(300, 300) // 원하는 크기로 조절
                    mapCardViewFragment.view?.layoutParams = cardViewLayoutParams
                    transaction.addToBackStack(null)
                    transaction.commit()
                }

                //식당 전화걸기
                binding.callResBtn.setOnClickListener {
                    //restaurantDto?.res_phone 의 현재 형태 : 051-711-0415
                    val callNum = restaurantDto?.res_phone
                    val tel = "tel:$callNum"
                    startActivity(Intent("android.intent.action.DIAL", Uri.parse(tel)))
                }
                
                binding.searchBoard.setOnClickListener { 
                    //해당 식당 게시글 목록으로 이동
                    val fragment = HomeFragment.newInstance("", restaurantDto.res_id)
                    parentFragmentManager.beginTransaction()
                        .add(R.id.fragmentContainer, fragment)
                        .addToBackStack("HomeFragment")
                        .commit()
                }

            }

            override fun onFailure(call: Call<RestaurantDto>, t: Throwable) {
                TODO("Not yet implemented")
            }

        })




        return binding.root

    }
}