package com.example.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.UpdateReviewFragment
import com.example.find_my_matzip.databinding.FragmentBoardDtlBinding
import com.example.find_my_matzip.model.BoardDtlDto
import com.example.find_my_matzip.model.CommentDto
import com.example.find_my_matzip.navTab.adapter.BoardDtlViewPagerAdapter
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class boardDtlFragment : Fragment() {
    lateinit var binding: FragmentBoardDtlBinding
    private val TAG: String = "boardDtlFragment"
    private var myFeeling = 0
    private lateinit var AllComment: TextView

    // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
    val loginuserId = SharedPreferencesManager.getString("id","")

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
                //좋아요&싫어요
                binding.countLike.text = boardDto?.feelingBoardDtlDto?.likeCount.toString()
                binding.countDislike.text = boardDto?.feelingBoardDtlDto?.dislikeCount.toString()

                if(boardDto?.feelingBoardDtlDto?.myFeeling == null){
                    Log.d("sjw","데이터 도착 확인(feelingBoardDtlDto). : myFeeling $boardDto?.feelingBoardDtlDto?.myFeeling")
                }else{
                    if(boardDto?.feelingBoardDtlDto?.myFeeling?.feelNum!! == 1){
                        binding.likeBtn.setImageResource(R.drawable.baseline_thumb_up_alt_24)//좋아요
                        myFeeling = 1
                    }else if(boardDto?.feelingBoardDtlDto?.myFeeling?.feelNum!! == -1) {
                        binding.dislikeBtn.setImageResource(R.drawable.baseline_thumb_down_alt_24)//싫어요
                        myFeeling = -1
                    }
                }

                if(boardDto?.board?.userId == loginuserId){
                    binding.manageReview.visibility = View.VISIBLE
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
                fun navigateToResDetail(resId: Long) {

                    val fragment = resId?.let { RestaurantDtlFragment.newInstance(it) }
                    if (fragment != null) {
                        parentFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, fragment)
                            .addToBackStack(null)
                            .commit()
                    }
                }

                fun navigateToUpdateBoard(boardId: String?) {
                    if (boardId.isNullOrEmpty()) {
                        Log.d("kkt", "boardId is null or empty")
                        return
                    }

                    val fragment = UpdateReviewFragment.newInstance(boardId)
                    parentFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, fragment)
                        .addToBackStack(null)
                        .commit()
                }

                binding.toResDtl.setOnClickListener {
                    Log.d("kkt", "식당가기 클릭됨")
                    Log.d("kkt", "resId: ${boardDto?.restaurant?.res_id}")
                    val resId = boardDto?.restaurant?.res_id
                    if (resId != null) {
                        navigateToResDetail(resId)
                    }
                }
                // toResDtl 클릭 이벤트 핸들러

                //캐시지우기
                val directory = File(requireContext().cacheDir, "images")

                binding.updateReview.setOnClickListener {
                    if (directory.exists()) {
                        val files = directory.listFiles()
                        if (files != null) {
                            for (file in files) {
                                file.delete()
                            }
                        }
                        directory.delete()
                    }
                    Log.d("kkt", "게시글 수정 클릭됨")
                    Log.d("kkt", "boardId: ${boardDto?.board?.id}")
                    val boardId = boardDto?.board?.id.toString()
                    navigateToUpdateBoard(boardId)
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
                fun getTotalCommentCount(comments: List<CommentDto>): Int {
                    var totalCount = comments.size
                    for (comment in comments) {
                        if (comment.children != null) {
                            totalCount += getTotalCommentCount(comment.children)
                        }
                    }
                    return totalCount
                }
                binding.AllComment.text = "댓글 (${getTotalCommentCount(boardDto?.commentsPage?.content ?: emptyList())}개) 모두보기"


//                CoroutineScope(Dispatchers.Main).launch {
//                    try {
//                        showNearbyRestaurants(restaurantsInsideCircle)
//                    }
                AllComment = binding.AllComment
                binding.AllComment.setOnClickListener {
                    val boardId = boardDto?.board?.id.toString() // 게시판 아이디 가져오기
                    Log.d(TAG, "AllComment 클릭! . boardId: $boardId")

                    // 추가하기 전에 프래그먼트가 이미 추가되어 있는지 확인
                    val existingFragment = parentFragmentManager.findFragmentByTag(CommentFragment::class.java.simpleName)

                    if (existingFragment == null) {
                        if (boardId != null) {
                            // 프래그먼트가 추가되지 않은 경우, 추가합니다.
                            val commentFragment = CommentFragment.newInstance(boardId)

                            // FragmentTransaction을 사용하여 CommentListFragment를 표시하는 코드를 작성
                            val transaction = fragmentManager?.beginTransaction()

                            // 현재 Fragment를 숨기고 CommentListFragment를 추가
                            transaction?.hide(this@boardDtlFragment)
                            transaction?.add(R.id.fragmentContainer, commentFragment)

                            // addToBackStack을 사용하여 뒤로 가기 스택에 추가 (선택 사항)
                            transaction?.addToBackStack(null)

                            // 변경사항을 즉시 적용
                            transaction?.commit()
                        }
                    }
                }

                //유저 정보 클릭
                binding.userLinearLayout.setOnClickListener {
                    Log.d(TAG, "유저프로필 클릭")
                    Log.d(TAG, "userId: ${boardDto?.users?.userid}")
                    val userId = boardDto?.users?.userid
                    navigateToUserProfile(userId)
                }

                // 좋아요 버튼 클릭 이벤트 핸들러
                binding.likeBtn.setOnClickListener {
                    if (myFeeling == 1) {
                        // 이미 눌려있다면 ->좋아요 취소
                        binding.likeBtn.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                        myFeeling = 0
                        binding.countLike.text = (binding.countLike.text.toString().toInt() - 1).toString()
                    } else if(myFeeling == -1) {
                        // 싫어요 눌려있다면 -> 좋아요
                        binding.dislikeBtn.setImageResource(R.drawable.baseline_thumb_down_off_alt_24)
                        binding.likeBtn.setImageResource(R.drawable.baseline_thumb_up_alt_24)
                        myFeeling = 1
                        binding.countLike.text = (binding.countLike.text.toString().toInt() + 1).toString()
                        binding.countDislike.text = (binding.countDislike.text.toString().toInt() - 1).toString()
                    }else{
                        //안눌려있다면
                        binding.likeBtn.setImageResource(R.drawable.baseline_thumb_up_alt_24)
                        myFeeling = 1
                        binding.countLike.text = (binding.countLike.text.toString().toInt() + 1).toString()

                    }

                    setFeeling(boardDto?.board!!.id,1)
                }

                // 싫어요 버튼 클릭 이벤트 핸들러
                binding.dislikeBtn.setOnClickListener {
                    if (myFeeling == -1) {
                        // 이미 눌러져있다면 -> 취소
                        binding.dislikeBtn.setImageResource(R.drawable.baseline_thumb_down_off_alt_24)
                        myFeeling = 0
                        binding.countDislike.text = (binding.countDislike.text.toString().toInt() - 1).toString()
                    } else if(myFeeling == 1) {
                        // 좋아요 눌려있다면 -> 싫어요
                        binding.likeBtn.setImageResource(R.drawable.baseline_thumb_up_off_alt_24)
                        binding.dislikeBtn.setImageResource(R.drawable.baseline_thumb_down_alt_24)
                        myFeeling = -1
                        binding.countDislike.text = (binding.countDislike.text.toString().toInt() + 1).toString()
                        binding.countLike.text = (binding.countLike.text.toString().toInt() - 1).toString()
                    }else{
                        //안눌려있다면
                        binding.dislikeBtn.setImageResource(R.drawable.baseline_thumb_down_alt_24)
                        myFeeling = -1
                        binding.countDislike.text = (binding.countDislike.text.toString().toInt() + 1).toString()
                    }

                    setFeeling(boardDto?.board!!.id,-1)
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

    //좋아요&싫어요
    private fun setFeeling(boardId:Long, newFeel:Int) {
        val feelingService = (context?.applicationContext as MyApplication).feelingService
        val call = feelingService.setFeeling(boardId,newFeel)

        call.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if(response.isSuccessful) {
                    Log.d(TAG, "감정표현 성공")
                }else{
                    Log.d(TAG, "\"감정표현 실패: ${response.code()}")
                }
            }
            override fun onFailure(call: Call<Unit>, t: Throwable) {
                Log.d(TAG, "서버 응답 실패 ${t.message}")
                call.cancel()
            }
        })

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