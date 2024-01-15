package com.matzip.find_my_matzip.navTab.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ItemDialogBinding
import com.matzip.find_my_matzip.model.FollowDto
import com.matzip.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.matzip.find_my_matzip.utiles.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


//
//class FollowerViewHolder(val binding: ItemDialogBinding) : RecyclerView.ViewHolder(binding.root)

// FollowerAdapter 클래스는 RecyclerView의 어댑터로, 데이터를 받아와 화면에 표시하는 역할을 합니다.

private val TAG: String = "FollowerAdapter"

class FollowerAdapter(val context: Context, var datas: List<FollowDto>, private val listener: OnFollowerClickListener) :
    RecyclerView.Adapter<FollowerAdapter.FollowerViewHolder>() {

    // ViewHolder를 생성하는 함수
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowerViewHolder {
        val binding = ItemDialogBinding.inflate(LayoutInflater.from(context), parent, false)
        return FollowerViewHolder(binding)
    }

    override fun getItemCount(): Int {
        // datas가 null이면 0을 반환, 그렇지 않으면 datas의 크기를 반환
        return datas?.size ?: 0
    }


    // ViewHolder에 데이터를 바인딩하는 함수
    override fun onBindViewHolder(holder: FollowerViewHolder, position: Int) {
        // 현재 위치의 데이터를 가져와서 ViewHolder에 바인딩
        val item = datas?.get(position)
        holder.bind(item)
    }

    // 내부 클래스로 정의된 FollowerViewHolder는 각 아이템 뷰의 구성요소를 관리합니다.
    inner class FollowerViewHolder(val binding: ItemDialogBinding) : RecyclerView.ViewHolder(binding.root) {
        // 아이템 뷰에 데이터를 바인딩하는 함수
        fun bind(item: FollowDto?) {
            // 뷰 바인딩 객체를 통해 아이템의 텍스트 설정
            binding.dialogUserid.text = item?.id
            binding.dialogUserName.text = item?.name

            val userImg = item?.profileImage
            if(userImg != ""){
                Glide.with(context)
                    .load(userImg)
//                    .diskCacheStrategy(DiskCacheStrategy.NONE)// 디스크 캐시 저장 off
//                    .skipMemoryCache(true)// 메모리 캐시 저장 off
                    .override(900, 900)
                    .into(binding.dialogUserImg)

            }

            val followcheck= item?.subscribeState

            Log.d(TAG, "subscribeState $item.id , $followcheck")
            if(followcheck == true){
                // 팔로우 중
                binding.followBtn.visibility = View.GONE
                binding.unfollowBtn.visibility = View.VISIBLE
            }else{
                // 팔로우 중이 아닌 경우
                binding.followBtn.visibility = View.VISIBLE
                binding.unfollowBtn.visibility = View.GONE
            }
            val userService = (context?.applicationContext as MyApplication).userService

            // 팔로우 버튼 클릭 리스너
            binding.followBtn.setOnClickListener {
                Log.d(TAG, "팔로우 버튼클릭")

                item?.id?.let { toUserId ->
                    userService.insertFollow(toUserId)
                        .enqueue(object : Callback<Unit> {
                            override fun onResponse(
                                call: Call<Unit>,
                                response: Response<Unit>
                            ) {
                                if (response.isSuccessful) {
                                    // 성공적으로 팔로우한 경우
                                    binding.followBtn.visibility = View.GONE
                                    binding.unfollowBtn.visibility = View.VISIBLE

                                    Toast.makeText(context, "팔로우 성공", Toast.LENGTH_SHORT).show()
                                    Log.e(TAG, "팔로우 성공")

                                } else {
                                    Log.d(TAG, "팔로우 요청 실패 - Code: ${response.code()}, Message: ${response.message()}")
                                }
                            }

                            override fun onFailure(
                                call: Call<Unit>,
                                t: Throwable?
                            ) {
                                Log.e(TAG, "팔로우 onFailure")
                            }
                        })
                }
            }

            // 팔로우 버튼 클릭 리스너
            binding.unfollowBtn.setOnClickListener {
                Log.d(TAG, "팔로우 버튼클릭")

                item?.id?.let { toUserId ->
                    userService.deleteFollow(toUserId)
                        .enqueue(object : Callback<Unit> {
                            override fun onResponse(
                                call: Call<Unit>,
                                response: Response<Unit>
                            ) {
                                if (response.isSuccessful) {
                                    // 성공적으로 언팔로우한 경우
                                    binding.followBtn.visibility = View.VISIBLE
                                    binding.unfollowBtn.visibility = View.GONE

                                    Toast.makeText(context, "언팔로우 성공", Toast.LENGTH_SHORT).show()
                                    Log.e(TAG, "언팔로우 성공")

                                } else {
                                    Log.d(TAG, "언팔로우 요청 실패 - Code: ${response.code()}, Message: ${response.message()}")
                                }
                            }

                            override fun onFailure(
                                call: Call<Unit>,
                                t: Throwable?
                            ) {
                                Log.e(TAG, "팔로우 onFailure")
                            }
                        })
                }
            }

            // 아이템 뷰를 클릭했을 때의 동작 정의
            binding.root.setOnClickListener {
                // 클릭 시 리스너의 onFollowClick 메서드 호출
                listener.onFollowClick(item?.id ?: "")
            }
        }
    }

//    FollowerAdapter의 OnFollowerClickListener에서 팔로워를 클릭했을 때 호출되는 onFollowClick 메서드에서 해당 유저의 프로필로 이동하는 코드를 추가
    interface OnFollowerClickListener {
        fun onFollowClick(item: String)
    }

//    followerId
}
