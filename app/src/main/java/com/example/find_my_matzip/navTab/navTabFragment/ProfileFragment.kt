package com.example.find_my_matzip.navTab.navTabFragment

import android.annotation.SuppressLint
//import android.os.Build.VERSION_CODES.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentProfileBinding
import com.example.find_my_matzip.model.FollowDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.adapter.BoardRecyclerAdapter2
import com.example.find_my_matzip.navTab.adapter.ProfileAdapter2
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.CustomDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ProfileFragment : Fragment() {

    companion object {
        // ProfileFragment의 인스턴스를 생성하고, 전달할 데이터를 설정하는 메서드?
        fun newInstance(userId: String): ProfileFragment {
            // 1. ProfileFragment의 새로운 인스턴스를 생성합니다.
            val fragment = ProfileFragment()
            // 2. Bundle 객체를 만들어서 그 안에 "userId"라는 키로 전달받은 userId 값을 넣습니다.
            val args = Bundle()
            args.putString("userId", userId)
            Log.d("ProfileFragment", "번들에 담은 아이디: userId ====================$userId")
            // 3. ProfileFragment의 arguments 속성에 해당 Bundle을 설정합니다.
            fragment.arguments = args
            // 4. 설정된 프래그먼트를 반환합니다.
            return fragment
        }
    }

    lateinit var binding: FragmentProfileBinding
    lateinit var adapter: ProfileAdapter2
    lateinit var boardAdapter: BoardRecyclerAdapter2
    var isLoading = false
    var isLastPage = false
    var currentPage = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d("SdoLifeCycle","ProfileFragmnet onCreateView")
        binding = FragmentProfileBinding.inflate(layoutInflater, container, false)
        // 보드 어댑터
        boardAdapter = BoardRecyclerAdapter2(this@ProfileFragment, emptyList())
        binding.boardRecyclerView.adapter = boardAdapter

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

        // 전달된 userId 값 확인 userId : matzuo
        val pageUserId = arguments?.getString("userId")
        if (pageUserId != null) {
            val loginUserId = SharedPreferencesManager.getString("id", "")
            if (loginUserId == pageUserId) {
                Log.d(
                    "ProfileFragment",
                    "현재 사용자 아이디 ID: $loginUserId, 페이지 사용자 ID: $pageUserId"
                )
                Log.d("ProfileFragment", "마이페이지로 이동")
                Log.d("ProfileFragment", "$pageUserId")

                // 마이페이지 프래그먼트로 이동
                navigateToMyPageFragment()
            } else {
                // userId를 사용하여 프로필을 조회 -- 팔로워 팔로우 목록에서 해당아이디의 프로필을 조회 하는것임
                val userService = (context?.applicationContext as MyApplication).userService
//            profileList에 해당 유저의 프로필 정보가 담기는것, matzip
                val profileList = userService.getProfile(pageUserId,page)

                profileList.enqueue(object : Callback<ProfileDto> {
                    @SuppressLint("SuspiciousIndentation")
                    override fun onResponse(
                        call: Call<ProfileDto>,
                        response: Response<ProfileDto>,

                    ) {
                        isLoading = false
                        val profileDto = response.body()
                        if (profileDto != null) {
                            // matzip 의 정보를 갖고 있네. matzip 의 정보네.
                            Log.d("ProfileFragment", "도착 확인5: followcheck ${profileDto?.followcheck}")
                            // 사용자가 이미 팔로우 중인지 확인
                            val followcheck = profileDto.followcheck
                            // 팔로우 버튼 초기 상태 설정
                            val followBtn: Button = binding.followBtn
                            val unfollowBtn: Button = binding.unfollowBtn
                            // 언팔로우 버튼 클릭 리스너

                            // 초기 상태 설정
                            if (followcheck) {
                                // 이미 팔로우 중인 경우
                                followBtn.visibility = View.GONE
                                unfollowBtn.visibility = View.VISIBLE
                            } else {
                                // 팔로우 중이 아닌 경우
                                followBtn.visibility = View.VISIBLE
                                unfollowBtn.visibility = View.GONE
                            }

                            // 팔로우 버튼 클릭 리스너
                            followBtn.setOnClickListener {
                                Log.d("ProfileFragment", "팔로우 버튼클릭")
                                // 현재, 상대방의 페이지, matzip
                                // 하려는게, matzip5 이 matzip 한테, 팔로우, 언팔로우
                                // 스프링에서는 principal 에 이미 로그인한 정보가 있어요. matzip5
                                // 이미 있는 5번 말고, 상대방 아이디를 보내기.
                                pageUserId?.let { toUserId ->
                                    userService.insertFollow(toUserId)
                                        .enqueue(object : Callback<Unit> {
                                            override fun onResponse(
                                                call: Call<Unit>,
                                                response: Response<Unit>
                                            ) {
                                                if (response.isSuccessful) {
                                                    // 성공적으로 팔로우한 경우
                                                    followBtn.visibility = View.GONE
                                                    unfollowBtn.visibility = View.VISIBLE
                                                    // 현재의 프래그먼트를 제거
                                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    transaction.remove(this@ProfileFragment).commit()

                                                    // 새로운 인스턴스를 생성하여 추가
                                                    val newFragment = ProfileFragment.newInstance(pageUserId)
                                                    val newTransaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    newTransaction.add(R.id.fragmentContainer, newFragment)
                                                    newTransaction.addToBackStack(null)
                                                    newTransaction.commit()
                                                } else {
                                                    Log.d("ProfileFragment", "팔로우 요청 실패 - Code: ${response.code()}, Message: ${response.message()}")
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<Unit>,
                                                t: Throwable?
                                            ) {
                                                Log.e("ProfileFragment", "팔로우 onFailure", t)
                                            }
                                        })
                                }
                            }
                            unfollowBtn.setOnClickListener {
                                Log.d("ProfileFragment", "언팔로우 버튼클릭")
                                // loginUserId : matzip5
                                // toUserId : matzip
                                pageUserId?.let { toUserId ->
                                    userService.deleteFollow(toUserId)
                                        .enqueue(object : Callback<Unit> {
                                            override fun onResponse(
                                                call: Call<Unit>,
                                                response: Response<Unit>
                                            ) {
                                                Log.d("ProfileFragment", "팔로우 onResponse: ${response.code()}")
                                                if (response.isSuccessful) {
                                                    // 성공적으로 언팔로우한 경우
                                                    followBtn.visibility = View.VISIBLE
                                                    unfollowBtn.visibility = View.GONE
                                                    // 현재의 프래그먼트를 제거
                                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    transaction.remove(this@ProfileFragment).commit()

                                                    // 새로운 인스턴스를 생성하여 추가
                                                    val newFragment = ProfileFragment.newInstance(pageUserId)
                                                    val newTransaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    newTransaction.add(R.id.fragmentContainer, newFragment)
                                                    newTransaction.addToBackStack(null)
                                                    newTransaction.commit()
                                                } else {
                                                    Log.d("ProfileFragment", "팔로우 요청실패")
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<Unit>,
                                                t: Throwable?
                                            ) {
                                                // 네트워크 오류 등 예외 처리
                                            }
                                        })
                                }
                            }


                            // MessageFragment로 이동
                            val messageBtn: ImageView = binding.messageBtn
                            messageBtn.setOnClickListener {
                                Log.d("MessageFragment", "메시지 버튼 클릭")

                                pageUserId?.let { toUserId ->
                                    userService.deleteFollow(toUserId)
                                        .enqueue(object : Callback<Unit> {
                                            override fun onResponse(
                                                call: Call<Unit>,
                                                response: Response<Unit>
                                            ) {
                                                Log.d("MessageFragment", "메시지 onResponse: ${response.code()}")
                                                if (response.isSuccessful) {
                                                    // 성공한 경우
                                                    followBtn.visibility = View.VISIBLE
                                                    unfollowBtn.visibility = View.GONE
                                                    // 현재의 프래그먼트 제거
                                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    transaction.remove(this@ProfileFragment).commit()

                                                    // 새로운 인스턴스 생성 및 추가
                                                    val newFragment = MessageFragment.newInstance()
                                                    val newTransaction = requireActivity().supportFragmentManager.beginTransaction()
                                                    newTransaction.add(R.id.fragmentContainer, newFragment)
                                                    newTransaction.addToBackStack(null)
                                                    newTransaction.commit()
                                                } else {
                                                    Log.d("MessageFragment", "메시지 요청 실패")
                                                }
                                            }

                                            override fun onFailure(
                                                call: Call<Unit>,
                                                t: Throwable?
                                            ) {
                                                // 네트워크 오류 등 예외 처리
                                            }
                                        })
                                }
                            }





                            // 팔로워 팔로우수
                            binding.countFromUser.text = profileDto.countFromUser.toString()
                            binding.countToUser.text = profileDto.countToUser.toString()
                            binding.countBoard.text = profileDto.countBoard.toString()

//                    binding.followCheck.text=profileDto.followCheck.toString()

                            // 유저정보 matzip
                            binding.pageUserId.text = profileDto.pageUserDto.userid

                            Log.d(
                                "ProfileFragment",
                                "Page User ID: ${profileDto.pageUserDto.userid}"
                            )
//                    binding.userName.text = profileDto.pageUserDto.username
//                    binding.userAddress.text = profileDto.pageUserDto.user_address
//                    binding.userPhone.text = profileDto.pageUserDto.userphone
//                    binding.userRole.text = profileDto.pageUserDto.user_role
//                    binding.gender.text = profileDto.pageUserDto.gender


                            if(profileDto.pageUserDto.user_image != "") {
                                // 다른 필요한 데이터들도 마찬가지로 설정
                                Glide.with(requireContext())
                                    .load(profileDto.pageUserDto.user_image)
                                    .override(900, 900)
                                    .error(R.drawable.profile)
                                    .into(binding.userImage)
                            }


                            Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")
                            // 프로필 어댑터 및 보드 어댑터 업데이트

                            val newBoardList = profileDto.boards.content
                            if (newBoardList.isNotEmpty() && currentPage == 0) {
                            ProfileAdapter2(this@ProfileFragment, listOf(profileDto.pageUserDto))
                            boardAdapter = BoardRecyclerAdapter2(this@ProfileFragment, profileDto.boards.content)
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
                                val followingList: List<FollowDto> =
                                    profileDto?.followingDtoList ?: emptyList()
                                Log.d("MyPageFragment", "도착 확인6: followingDtoList $followingList")


                                CustomDialog(
                                    requireContext(),
                                    followingList,
                                    CustomDialog.DialogType.FOLLOWING
                                ).apply {
                                    setOnClickListener(object : CustomDialog.OnDialogClickListener {
                                        override fun onClicked(name: String) {
                                            // 클릭한 팔로워의 프로필로 이동하는 코드 추가
                                            navigateToUserProfile(name)
                                            Log.d("CustomDialog", "팔로잉아이디 클릭! : ID: $name")
                                        }
                                    })
                                    // 다이얼로그 표시 및 내용 설정
                                    showDialog()
                                    setContent()
                                }


                            }
                            // 팔로워 목록 클릭 시 다이얼로그로 팔로워 리스트 (리사이클러)
                            binding.follower.setOnClickListener {
                                val followerList: List<FollowDto> =
                                    profileDto?.followerDtoList ?: emptyList()
                                Log.d("MyPageFragment", "도착 확인6: followerDtoList $followerList")

                                // 다이얼로그 생성
                                val dialog = CustomDialog(
                                    requireContext(),
                                    followerList,
                                    CustomDialog.DialogType.FOLLOWER
                                )


                                // 다이얼로그 내용 설정
                                dialog.setOnClickListener(object :
                                    CustomDialog.OnDialogClickListener {
                                    override fun onClicked(name: String) {
                                        // 클릭한 팔로워의 프로필로 이동하는 코드 추가
                                        navigateToUserProfile(name)
                                        Log.d("CustomDialog", "팔로워아이디 클릭! : ID: $name")
                                    }
                                })

                                // 다이얼로그 표시
                                dialog.showDialog()
                                // 다이얼로그 내용 설정
                                dialog.setContent()


                            }
                        }

                    }

                    override fun onFailure(call: Call<ProfileDto>, t: Throwable) {
                        isLoading = false
                        t.printStackTrace()
                        call.cancel()
                        Log.e("MyPageFragment", " 통신 실패")
                    }
                })
            }
        }
        return binding.root
    } // onCreateView 메서드의 마지막 부분

    // MyPageFragment로 이동하는 메서드
    private fun navigateToMyPageFragment() {

        // MyPageFragment로 이동하는 코드를 추가
        val myPageFragment = MyPageFragment()
        val transaction = requireActivity().supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragmentContainer, myPageFragment)
        transaction.commit()
        requireActivity().supportFragmentManager.popBackStack("MyPageTransaction", FragmentManager.POP_BACK_STACK_INCLUSIVE)
//        if (currentUserId == pageUserId) {
//            // MyPageFragment로 이동하는 코드를 추가
//            val myPageFragment = MyPageFragment()
//            val transaction = requireActivity().supportFragmentManager.beginTransaction()
//            transaction.replace(R.id.fragmentContainer, myPageFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        } else {
//            // 현재 사용자가 페이지 사용자와 같지 않은 경우의 처리
//            // 필요에 따라 메시지를 표시하거나 다른 작업을 수행할 수 있습니다.
//            Log.d("ProfileFragment", "현재 사용자와 페이지 사용자가 일치하지 않습니다. 다른 작업 수행.")
//        }
    }


    // 팔로워의 프로필로 이동하는 메서드
    // 팔로워의 프로필로 이동하는 메서드
    private fun navigateToUserProfile(userId: String) {
        // 클릭 시 HomeFollowFragment로 이동하는 코드
        val fragment = ProfileFragment.newInstance(userId)
        // 트랜잭션에 이름 부여
        val transaction = parentFragmentManager.beginTransaction()
            .add(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        // 현재의 HomeFragment를 백 스택에서 제거
        // parentFragmentManager.popBackStack("Profile", FragmentManager.POP_BACK_STACK_INCLUSIVE)
    }

    @Override
    override fun onResume() {
        Log.d("SdoLifeCycle","ProfileFragment onResume")
        super.onResume()
    }
    @Override
    override fun onPause() {
        Log.d("SdoLifeCycle","ProfileFragment onPause")
        super.onPause()
    }
    @Override
    override fun onDestroy() {
        Log.d("SdoLifeCycle","ProfileFragment onDestroy")
        super.onDestroy()
    }

//    fun showExitDialog() {
//        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle("Exit?")
//        builder.setMessage("앱을 종료하시겠습니까?")
//        builder.setNegativeButton("아니오") { dialog, which ->
//            // 아무 작업도 수행하지 않음
//        }
//        builder.setPositiveButton("예") { dialog, which ->
//            // 프래그먼트가 호스트하는 액티비티의 onBackPressed() 호출
//            (requireActivity() as? HomeTabActivity)?.onBackPressed()
//        }
//        builder.show()
//    }

//        // 팔로워 해당 유저의 프로필로 이동하는 코드를 추가
//        val profileFragment = ProfileFragment.newInstance(userId)
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//
//        // 추가된 부분
//        if (!isStateSaved) {
//            transaction.replace(R.id.fragmentContainer, profileFragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        } else {
//            // 상태가 저장된 경우에는 커밋을 허용하지 않고 로그를 출력
//            Log.w("ProfileFragment", "Transaction not committed: Fragment state already saved")
//        }

}