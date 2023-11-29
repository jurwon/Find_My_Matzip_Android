package com.example.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
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



        binding = FragmentMyPageBinding.inflate(layoutInflater, container, false)
        // 보드 어댑터 초기화
        boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, emptyList())
        binding.boardRecyclerView.adapter = boardAdapter

        binding.updateBtn.setOnClickListener {
//            profileUpdateFragment 회원수정창(타 프레그먼트로) 이동하는 코드
            val profileUpdateFragment = ProfileUpdateFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, profileUpdateFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        binding.writeBoardBtn.setOnClickListener {
//            게시글작성창으로 이동
            val WriteReviewFragment = WriteReviewFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, WriteReviewFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }



        // 로그인한 사용자의 아이디를 가져와서 해당 사용자의 프로필 정보를 서버에서 조회
        val userId = SharedPreferencesManager.getString("id","")
        val userService = (context?.applicationContext as MyApplication).userService
        val profileList = userService.getProfile(userId)

        Log.d("MyPageFragment", "profileList.enqueue 호출전 : ")

        profileList.enqueue(object : Callback<ProfileDto> {
            override fun onResponse(call: Call<ProfileDto>, response: Response<ProfileDto>) {
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
                    // 팔로워 팔로우수
                    binding.countFromUser.text = profileDto.countFromUser.toString()
                    binding.countToUser.text = profileDto.countToUser.toString()
                    binding.countBoard.text = profileDto.countBoard.toString()
//                    binding.followCheck.text=profileDto.followCheck.toString()

                    // 유저정보
                    binding.userId.text = profileDto.pageUserDto.userid
//                    binding.userName.text = profileDto.pageUserDto.username
//                    binding.userAddress.text = profileDto.pageUserDto.user_address
//                    binding.userPhone.text = profileDto.pageUserDto.userphone
//                    binding.userRole.text = profileDto.pageUserDto.user_role
//                    binding.gender.text = profileDto.pageUserDto.gender


                    // 다른 필요한 데이터들도 마찬가지로 설정

                    if(profileDto.pageUserDto.user_image != ""){
                        Glide.with(requireContext())
                            .load(profileDto.pageUserDto.user_image)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)// 디스크 캐시 저장 off
                            .skipMemoryCache(true)// 메모리 캐시 저장 off
                            .override(900, 900)
                            .into(binding.userImage)
                    }

                    Log.d("MyPageFragment", "도착 확인2: profileList ${profileDto?.boards}")

                    boardAdapter = BoardRecyclerAdapter(this@MyPageFragment, profileDto.boards.content)
                    val spanCount = 3 // 원하는 열의 수 ㅋㅋ 생각보다 간단하네..
                    val gridLayoutManager = GridLayoutManager(requireContext(), spanCount)
                    binding.boardRecyclerView.layoutManager = gridLayoutManager
                    binding.boardRecyclerView.adapter = boardAdapter


                    // 팔로잉 목록 클릭 시 다이얼로그 표시
                    binding.following.setOnClickListener {
                        val followingList: List<FollowDto> = profileDto.followingDtoList ?: emptyList()
                        Log.d("MyPageFragment", "도착 확인6: followingDtoList $followingList")
//

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
                t.printStackTrace()
                call.cancel()
                Log.e("MyPageFragment", " 통신 실패")
            }
        })

        //프로필 편집
        binding.updateBtn.setOnClickListener {
            //profileUpdateFragment 회원수정창(타 프레그먼트로) 이동하는 코드
            val profileUpdateFragment = ProfileUpdateFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragmentContainer, profileUpdateFragment)
            transaction.addToBackStack(null) //백스택에 지금 재배치한 fragment추가
            transaction.commit()
        }

        return binding.root
    }

    fun ShowMessage(title: String?, Message: String?) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(true)
        builder.setTitle(title)
        builder.setMessage(Message)
        builder.show()
    }
    private fun navigateToUserProfile(userId: String) {
        // 클릭 시 HomeFollowFragment로 이동하는 코드
        val fragment = ProfileFragment.newInstance(userId)

        // 트랜잭션에 이름 부여
        val transaction = parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()

        // 현재의 HomeFragment를 백 스택에서 제거
        parentFragmentManager.popBackStack("MyPageFragment", FragmentManager.POP_BACK_STACK_INCLUSIVE)

//
//        // 팔로워 해당 유저의 프로필로 이동하는 코드를 추가
//        val profileFragment = ProfileFragment.newInstance(userId)
//        val transaction = requireActivity().supportFragmentManager.beginTransaction()
//        transaction.replace(R.id.fragmentContainer, profileFragment)
//        //    transaction.addToBackStack(null)
//        transaction.commit()
    }
}
