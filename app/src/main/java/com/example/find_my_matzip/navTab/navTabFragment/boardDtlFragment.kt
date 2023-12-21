package com.example.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentBoardDtlBinding
import com.example.find_my_matzip.model.BoardDtlDto
import com.example.find_my_matzip.navTab.adapter.BoardDtlViewPagerAdapter
import com.example.find_my_matzip.navTab.adapter.BoardRecyclerAdapter
import com.example.find_my_matzip.navTab.adapter.NewHomeViewPagerAdapter
import com.example.find_my_matzip.navTab.adapter.ProfileAdapter
import com.example.find_my_matzip.retrofit.BoardService
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
class boardDtlFragment : Fragment() {
    lateinit var binding: FragmentBoardDtlBinding
    private val TAG: String = "boardDtlFragment"

    companion object {
        fun newInstance(boardId: String): boardDtlFragment {
            val fragment = boardDtlFragment()
            val args = Bundle()
            args.putString("boardId", boardId)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d("SdoLifeCycle","boardDtlFragment onCreate")
        super.onCreate(savedInstanceState)
        binding = FragmentBoardDtlBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","boardDtlFragment onCreateView")
        binding = FragmentBoardDtlBinding.inflate(layoutInflater, container, false)

        val boardService = (context?.applicationContext as MyApplication).boardService
        val boardDtl = arguments?.getString("boardId")?.let { boardService.getBoardDtl(it) }
//        val boardId = arguments?.getString("boardId")
        Log.d("boardDtlFragment", "boardDtl.enqueue 호출전 $boardDtl")

//        val boardDetail = boardService.getBoardDtl(boardId)

        boardDtl?.enqueue(object : Callback<BoardDtlDto> {
            override fun onResponse(call: Call<BoardDtlDto>, response: Response<BoardDtlDto>) {
                Log.d("kkt","데이터 도착 확인.")
                val boardDto = response.body()
                Log.d("kkt","데이터 도착 확인1. : BoardDtlDto $boardDto")
                Log.d("kkt","데이터 도착 확인2. : BoardDtlDto.board ${boardDto?.board}")
                Log.d("kkt","데이터 도착 확인3. : BoardDtlDto.restaurant ${boardDto?.restaurant}")
                Log.d("kkt","데이터 도착 확인4. : BoardDtlDto.users ${boardDto?.users}")
//                    Log.d("kkt","데이터 도착 확인5. : BoardDtlDto ${boardDto?.score}")
//                    Log.d("kkt","데이터 도착 확인6. : BoardDtlDto ${boardDto?.resId}")

                binding.boardDtlTitle.text = boardDto?.board?.boardTitle.toString()
                binding.boardDtlContent.text = boardDto?.board?.content.toString()
                binding.boardScore.rating = boardDto?.board?.score?.toFloat() ?: 0.0f
                binding.userId.text = boardDto?.users?.username.toString()
                val userImg = boardDto?.users?.user_image
                if(userImg != null){
                    Glide.with(requireContext())
                        .load(userImg)
                        .override(900, 900)
                        .into(binding.userProfileImg)
                }

                binding.resName.text = boardDto?.restaurant?.res_name.toString()
                binding.resAddress.text = boardDto?.restaurant?.res_address.toString()

//                binding.resScore.text = boardDto?.restaurant?.avgScore.toString()
                val formattedScore = String.format("%.1f", boardDto?.restaurant?.avgScore ?: 0.0)
                binding.resScore.text = formattedScore

                Log.d("kkt","바인딩완료")

                // 기존 게시물이미지 그려주던 글라이드
//                Glide.with(requireContext())
//                    .load(boardDto?.board?.boardImgDtoList?.get(0)?.imgUrl)
//                    .override(900, 900)
//                    .into(binding.boardThumbnail)
                val viewPager = binding.DtlViewPager
                // 이미지 리사이클러뷰 어댑터 초기화
                val viewPagerAdapter =
                    boardDto?.board?.let { BoardDtlViewPagerAdapter(binding.root.context, it.boardImgDtoList) }
                viewPager.adapter = viewPagerAdapter

                // 인디케이터 추가
                val dotsIndicator: DotsIndicator = binding.dotsIndicator // 인디케이터 뷰의 ID를 넣어주세요
                dotsIndicator.setViewPager2(viewPager)



                Log.d("MyPageFragment", "도착 확인2: res_thumbnail ${boardDto?.board?.boardImgDtoList?.get(0)?.imgUrl}")



                // toResDtl 클릭 이벤트 핸들러
                fun navigateToResDetail(resId: String?) {
                    if (resId.isNullOrEmpty()) {
                        Log.d("kkt", "resId is null or empty")
                        return
                    }

                    val fragment = RestaurantDtlFragment.newInstance(resId)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                binding.toResDtl.setOnClickListener {
                    Log.d("kkt", "식당가기 클릭됨")
                    Log.d("kkt", "resId: ${boardDto?.restaurant?.res_id}")
                    val resId = boardDto?.restaurant?.res_id
                    navigateToResDetail(resId)
                }
                // toResDtl 클릭 이벤트 핸들러


                // 유저 프로필로 이동 로직
                fun navigateToUserProfile(userId: String?) {
                    //만약 유저정보 없다면
                    if (userId.isNullOrEmpty()) {
                        Log.d(TAG, "userId is null or empty")
                        return
                    }

                    val userFrag: Fragment

                    if(SharedPreferencesManager.getString("id","") == userId){
                        //내 프로필
                        userFrag = MyPageFragment.newInstance(userId)

                    }else{
                        //다른사람 프로필
                        userFrag = ProfileFragment.newInstance(userId)
                    }
                    val transaction =  parentFragmentManager.beginTransaction()

                    transaction.replace(R.id.fragmentContainer, userFrag)
                    transaction.addToBackStack(null) //백스택에 지금 재배치한 fragment추가
                    transaction.commit()

                }

                //유저 정보 클릭
                binding.userLinearLayout.setOnClickListener {
                    Log.d(TAG, "유저프로필 클릭")
                    Log.d(TAG, "userId: ${boardDto?.users?.userid}")
                    val userId = boardDto?.users?.userid
                    navigateToUserProfile(userId)
                }


            }

            override fun onFailure(call: Call<BoardDtlDto>, t: Throwable) {
                // Handle failure
            }
        })

        return binding.root
    }

    private fun displayBoardDetails(boardDto: BoardDtlDto) {
    }
    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","boardDtlFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","boardDtlFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","boardDtlFragment onDestroy")
        super.onDestroy()
    }
}