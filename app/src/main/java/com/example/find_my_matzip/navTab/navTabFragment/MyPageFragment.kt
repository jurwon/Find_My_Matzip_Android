package com.example.find_my_matzip.navTab.navTabFragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.find_my_matzip.HomeTabActivity
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.WriteReviewFragment
import com.example.find_my_matzip.databinding.FragmentMyPageBinding
import com.example.find_my_matzip.model.FollowDto
import com.example.find_my_matzip.model.FollowingDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.adapter.BoardRecyclerAdapter
import com.example.find_my_matzip.navTab.adapter.ProfileAdapter
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.CustomDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MyPageFragment : Fragment() {
    lateinit var binding: FragmentMyPageBinding
    lateinit var adapter: ProfileAdapter
    lateinit var boardAdapter: BoardRecyclerAdapter
    var isLoading = false
    var isLastPage = false
    var currentPage = 0


    companion object {
        // MyPageFragment 인스턴스 생성
        fun newInstance(userId: String): MyPageFragment {
            val fragment = MyPageFragment()
            val args = Bundle()
            args.putString("userId", userId)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","MyPageFragment onCreateView")

        binding = FragmentMyPageBinding.inflate(layoutInflater, container, false)
        // 보드 어댑터 초기화
        boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, emptyList())
        binding.boardRecyclerView.adapter = boardAdapter

        binding.updateBtn.setOnClickListener {
//            profileUpdateFragment 회원 수정 창(다른 프래그먼트)으로 이동하는 코드
            val profileUpdateFragment = ProfileUpdateFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragmentContainer, profileUpdateFragment)
            transaction.addToBackStack(null)  // 백 스택에 현재 프래그먼트 추가
            transaction.commit()
        }

        binding.writeBoardBtn.setOnClickListener {
//            게시글 작성 창으로 이동
            val writeReviewFragment = WriteReviewFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.add(R.id.fragmentContainer, writeReviewFragment)
            transaction.addToBackStack(null)  // 백 스택에 현재 프래그먼트 추가
            transaction.commit()
        }



        // 스크롤 리스너 설정
        binding.boardRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                handleScroll(recyclerView)
            }
        })

        // 데이터 로드
        loadNextPageData(currentPage)

        return binding.root
    }
    private fun handleScroll(recyclerView: RecyclerView) {
        Log.d("MyPageFragment", "스크롤 리스닝 확인 1")
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
                Log.d("MyPageFragment", "33 currentPage 전 :$currentPage ")
                Log.d("MyPageFragment", "스크롤 리스닝 확인 3")
                loadNextPageData(currentPage)
                Log.d("MyPageFragment", "스크롤 리스닝 확인 4")
                Log.d("MyPageFragment", "44 currentPage 전 :$currentPage ")
            }
        }
    }


    private fun loadNextPageData(page: Int): FrameLayout {
        isLoading = true
        // 로그인한 사용자의 아이디를 가져와서 - 해당 사용자의 프로필 정보를 서버에서 조회
        val userId = SharedPreferencesManager.getString("id","")
        val userService = (context?.applicationContext as MyApplication).userService
        val profileList = userService.getProfile(userId,page)

        Log.d("MyPageFragment", "profileList.enqueue 호출전 : ")

        profileList.enqueue(object : Callback<ProfileDto> {
            @SuppressLint("SuspiciousIndentation")
            override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
                isLoading = false
                Log.d("MyPageFragment", "도착 확인: ")
                val profileDto = response.body()
                Log.d("MyPageFragment", "도착 확인1: profileList $profileDto")
                Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")
                Log.d("MyPageFragment", "도착 확인3: countFromUser ${profileDto?.countFromUser}")
                Log.d("MyPageFragment", "도착 확인4: countToUser ${profileDto?.countToUser}")
                Log.d("MyPageFragment", "도착 확인5: followerDtoList ${profileDto?.followerDtoList}")
                Log.d("MyPageFragment", "도착 확인5: followcheck ${profileDto?.followcheck}")
                if (profileDto != null) {
                    // 팔로워, 팔로잉, 게시물 수 업데이트
                    // 팔로워 팔로우 수
                    binding.countFromUser.text = profileDto.countFromUser.toString()
                    binding.countToUser.text = profileDto.countToUser.toString()
                    binding.countBoard.text = profileDto.countBoard.toString()
//                    binding.followCheck.text=profileDto.followCheck.toString()

                    // 유저 정보
                    binding.userId.text = profileDto.pageUserDto.userid
//                    binding.userName.text = profileDto.pageUserDto.username
//                    binding.userAddress.text = profileDto.pageUserDto.user_address
//                    binding.userPhone.text = profileDto.pageUserDto.userphone
//                    binding.userRole.text = profileDto.pageUserDto.user_role
//                    binding.gender.text = profileDto.pageUserDto.gender


                    // 다른 필요한 데이터들도 똑같이 설정

                    if(profileDto.pageUserDto.user_image != ""){
                        Glide.with(requireContext())
                            .load(profileDto.pageUserDto.user_image)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)// 디스크 캐시 저장 off
                            .skipMemoryCache(true)// 메모리 캐시 저장 off
                            .override(900, 900)
                            .into(binding.userImage)
                    }

                    Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")

                    val newBoardList = profileDto.boards.content
                    if (newBoardList.isNotEmpty() && currentPage == 0) {
                    boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, profileDto.boards.content)
                        currentPage++
                        val spanCount = 3 // 원하는 열의 수 ㅋㅋ 생각보다 간단하네..
                    val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
                    binding.boardRecyclerView.layoutManager = gridLayoutManager
                    binding.boardRecyclerView.adapter = boardAdapter
                    } else if (newBoardList.isNotEmpty() && currentPage !== 0) {
                        boardAdapter.addData(newBoardList)
                        currentPage++
                    }
                    else {
                        isLastPage = true
                    }
                    // 팔로잉 목록 클릭 시 다이얼로그 표시
                    binding.following.setOnClickListener {
                        val followingList: List<FollowDto> = profileDto.followingDtoList ?: emptyList()
                        Log.d("MyPageFragment", "도착 확인6: followingDtoList $followingList")



                        if(followingList!=null){
                            CustomDialog(requireContext(), followingList, CustomDialog.DialogType.FOLLOWING).apply {
                                setOnClickListener(object : CustomDialog.OnDialogClickListener {
                                    override fun onClicked(id: String) {
                                        // 클릭한 팔로워의 프로필로 이동하는 코드 추가
                                        navigateToUserProfile(id)
                                        Log.d("CustomDialog", "팔로잉아이디 클릭! : ID: $id")
                                    }
                                })
                                // 다이얼로그 표시 및 내용 설정
                                showDialog()
                                setContent()
                            }
                        }else{
                            Toast.makeText(requireContext(), "팔로잉 없음", Toast.LENGTH_SHORT).show()
                        }

                    }


                    // 팔로워 목록 클릭 시 다이얼로그 표시
                    binding.follower.setOnClickListener {
//
//                    }
                        // 팔로워 리스트 가져오기
                        val followerList: List<FollowDto> = profileDto.followerDtoList
                        Log.d("MyPageFragment", "도착 확인6: followerDtoList $followerList")

                        if(followerList != null){
                            CustomDialog(requireContext(), followerList, CustomDialog.DialogType.FOLLOWER).apply {
                                setOnClickListener(object : CustomDialog.OnDialogClickListener {
                                    override fun onClicked(name: String) {
                                        // 클릭한 팔로워의 프로필로 이동하는 코드 추가
                                        navigateToUserProfile(name)
                                        Log.d("CustomDialog", "팔로워아이디 클릭! : ID: $name")
                                    }
                                })
                                // 다이얼로그 표시 및 내용 설정
                                showDialog()
                                setContent()
                            }
                        }else{
                            Toast.makeText(requireContext(), "팔로워 없음", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                else {
                    Log.e("MyPageFragment", "Response body is null.")
                }
            }


            // 통신 실패 시 로그 출력
            override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                isLoading = false
                t.printStackTrace()
                call.cancel()
                Log.e("MyPageFragment", " 통신 실패")
            }
        })

        return binding.root
    }
    private fun navigateToUserProfile(userId: String) {
        // 클릭 시 ProfileFragment 이동하는 코드
        val fragment = ProfileFragment.newInstance(userId)

        // 트랜잭션에 이름 부여
        val transaction = parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","MyPageFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","MyPageFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","MyPageFragment onDestroy")
        super.onDestroy()
    }

    fun showExitDialog() {
        val builder = android.app.AlertDialog.Builder(requireContext())
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

//
//        // 팔로워 해당 유저의 프로필로 이동하는 코드를 추가
//        val profileFragment = ProfileFragment.newInstance(userId)
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragmentContainer, profileFragment)
//        //    transaction.addToBackStack(null)
//        transaction.commit()
    }

//    private fun navigateToUserProfile(userId: String) {
//        // 팔로워 해당 유저의 프로필로 이동하는 코드를 추가
//        val profileFragment = ProfileFragment.newInstance(userId)
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragmentContainer, profileFragment)
//        //    transaction.addToBackStack(null)
//        transaction.commit()
//    }
//}
