package com.matzip.find_my_matzip.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Window
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.model.FollowDto
import com.matzip.find_my_matzip.navTab.adapter.FollowerAdapter

class CustomDialog(
    context: Context, private val datas: List<FollowDto>, private val dialogType: DialogType
) {
    private val dialog = Dialog(context)
    private lateinit var onClickListener: OnDialogClickListener
    lateinit var adapter: FollowerAdapter

    init {
        // 다이얼로그 초기화 및 설정
        // 기본 다이얼로그
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        // 다이얼로그 투명도임.
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //dialog.window?.setBackgroundDrawable(ColorDrawable(Color.argb(128, 0, 0, 0))) 예시
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        // 커스텀 레이아웃을 설정
        dialog.setContentView(R.layout.dialog_custom)
        val window: Window? = dialog.window
        val layoutParams = window?.attributes
        layoutParams?.dimAmount = 0.8f // 0.0f (투명) ~ 1.0f (완전 불투명) 배경의 투명도
        window?.attributes = layoutParams

    }

    // 다이얼로그 버튼 클릭 리스너 설정
    fun setOnClickListener(listener: OnDialogClickListener) {
        onClickListener = listener
    }

    // 다이얼로그 표시
    fun showDialog() {
        dialog.findViewById<ImageButton>(R.id.dialogButton)?.setOnClickListener {
            dialog.dismiss()
        }

        // 다이얼로그를 표시
        dialog.show()
    }
    enum class DialogType {
        FOLLOWER, FOLLOWING
    }

    // 다이얼로그 내용 설정
    fun setContent() {
        // 리사이클러뷰 초기화
        val recyclerView: RecyclerView = dialog.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(dialog.context)

        // 팔로워 또는 팔로잉에 따라 어댑터 생성
        adapter = when (dialogType) {
            DialogType.FOLLOWER -> FollowerAdapter(
                dialog.context,
                datas,
                object : FollowerAdapter.OnFollowerClickListener {
                    override fun onFollowClick(item: String) {
                        onClickListener.onClicked(item)

                        dialog.dismiss()
                    }
                }
            )

            DialogType.FOLLOWING -> FollowerAdapter(
                dialog.context,
                datas,
                object : FollowerAdapter.OnFollowerClickListener {
                    override fun onFollowClick(item: String) {
                        onClickListener.onClicked(item)
                        dialog.dismiss()
                    }
                }
            )
        }

        recyclerView.adapter = adapter

        // 다이얼로그의 헤더 텍스트 설정
        dialog.findViewById<TextView>(R.id.headText)?.text =
            if (dialogType == DialogType.FOLLOWER) "팔로워 리스트" else "팔로잉 리스트"
    }
//    fun setContent(content: String) {
//        // 수정: 다이얼로그의 레이아웃을 새로 inflate하고 설정
//        val customBinding = DialogCustomBinding.inflate(LayoutInflater.from(dialog.context))
//
//        // 리사이클러뷰 초기화
//        val recyclerView: RecyclerView = customBinding.recyclerView
//        recyclerView.layoutManager = LinearLayoutManager(dialog.context)
//        // 어댑터 설정
//        val followerAdapter = FollowerAdapter(data, object : FollowerAdapter.OnFollowerClickListener {
//            override fun onFollowClick(followerId: String) {
//                onClickListener.onClicked(followerId)
//                dialog.dismiss()
//            }
//        })
//        recyclerView.adapter = followerAdapter
//
//        dialog.setContentView(customBinding.root)
//        customBinding.headText.text = "팔로워리스트"
//        customBinding.dialogUserid.text = content
//        dialog.setContentView(customBinding.root)
//    }

    interface OnDialogClickListener {
        fun onClicked(name: String)
    }

}