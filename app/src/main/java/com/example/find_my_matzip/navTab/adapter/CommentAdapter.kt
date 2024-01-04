package com.example.find_my_matzip.navTab.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.MyApplication
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.ItemCommentBinding
import com.example.find_my_matzip.model.CommentDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.navTab.navTabFragment.CommentFragment
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.google.firebase.storage.FirebaseStorage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration

// 뷰와 데이터 연결 한다.
interface CommentAdapterListener {
    fun onReplyClick(comment: CommentDto, boardId: Long)
}

class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root) {
    val recyclerViewChildren: RecyclerView = binding.recyclerViewChildren
    val userImageUrl: ImageView = binding.userImage
}

class CommentAdapter(
    val context: CommentFragment, // Pass CommentFragment instance
    val boardId: Long,
    var datas: List<CommentDto>,
    val listener: CommentAdapterListener? = null
) : RecyclerView.Adapter<CommentViewHolder>() {
    // 대댓글 더 보기 여부를 저장하는 맵
    private val showMoreMap: MutableMap<Long, Boolean> = mutableMapOf()
    var onReplyClick: ((CommentDto, Long) -> Unit)? = null
//    init {
//        // 최초에 디폴트로 보여줄 부모 댓글의 ID를 showMoreMap에 추가
//        datas.firstOrNull()?.let { showMoreMap[it.commentId] = false }
//    }

    init {
        // 최초에 디폴트로 모든 부모 댓글을 보이게 설정
        datas.forEach { showMoreMap[it.commentId] = true }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // XML 레이아웃을 데이터 바인딩을 통해 인플레이트
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return CommentViewHolder(binding)
    }

    // 데이터 세트의 총 아이템 수 반환
    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    // 데이터를 뷰에 바인딩하고 댓글 아이템 레이아웃을 설정
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val binding = holder.binding
        val item = datas?.get(position)
// 대댓글 더 보기 텍스트 처리
        binding.showMoreReplies.setOnClickListener {
            val commentId = item?.commentId ?: -1
            showMoreMap[commentId] = !showMoreMap.getOrDefault(commentId, false)

            // 클릭 시 해당 자식 댓글을 보여주거나 숨기는 로직 추가
            if (showMoreMap[commentId] == true) {
                val innerAdapter =
                    CommentAdapter(context, boardId, item?.children ?: emptyList(), listener)
                binding.recyclerViewChildren.layoutManager =
                    LinearLayoutManager(context?.requireContext()?.applicationContext)
                binding.recyclerViewChildren.adapter = innerAdapter
                binding.recyclerViewChildren.visibility = View.VISIBLE

                binding.showMoreReplies.text = "댓글숨기기"
            } else {
                if (item?.children?.isNotEmpty() == true) {
                    val initialChildren =
                        if (item.children.size > 1) item.children.take(1) else item.children
                    val innerAdapter = CommentAdapter(context, boardId, initialChildren, listener)
                    binding.recyclerViewChildren.layoutManager =
                        LinearLayoutManager(context?.requireContext()?.applicationContext)
                    binding.recyclerViewChildren.adapter = innerAdapter
                    binding.recyclerViewChildren.visibility = View.VISIBLE
                    binding.showMoreReplies.text = "댓글더보기"
                } else {
                    binding.recyclerViewChildren.visibility = View.GONE
                    binding.showMoreReplies.text = "댓글더보기"
                }

                if (item?.children?.size ?: 0 == 0) {
                    binding.showMoreReplies.visibility = View.GONE
                    binding.showMoreReplies.text = ""
                } else {
                    binding.showMoreReplies.visibility = View.VISIBLE
                }
            }

            // 이 부분을 추가해서 갱신된 정보를 반영하도록 합니다.
            notifyItemChanged(position)
        }
        // 초기에 댓글이 숨겨져 있을 때
        if (!showMoreMap.getOrDefault(item?.commentId ?: -1, false)) {
            binding.recyclerViewChildren.visibility = View.GONE
            binding.showMoreReplies.text = "댓글더보기"
            binding.showMoreReplies.visibility = View.VISIBLE
        } else {
            // 최초에는 부모 댓글과 2개의 자식 댓글을 보여줌
            val initialChildren =
                if (item?.children?.isNotEmpty() == true) item?.children?.take(1) else null
            val innerAdapter =
                CommentAdapter(context, boardId, initialChildren ?: emptyList(), listener)
            binding.recyclerViewChildren.layoutManager =
                LinearLayoutManager(context?.requireContext()?.applicationContext)
            binding.recyclerViewChildren.adapter = innerAdapter
            binding.recyclerViewChildren.visibility = View.VISIBLE
            binding.showMoreReplies.text = "댓글숨기기"
            binding.showMoreReplies.visibility = View.VISIBLE
        }

        binding.commentWriter?.text = item?.commentWriter
        binding.commentContents.text = item?.commentContents
        binding.commentCreatedTime.text = item?.commentCreatedTime.toString()

        val commentTimeAgo = getTimeAgoText(item?.commentCreatedTime)
        binding.commentCreatedTime.text = commentTimeAgo

        val isParentComment = item?.depth == 0

        // 화살표 아이콘 표시
        if (!isParentComment) {
            binding.arrowIcon.visibility = View.VISIBLE
        } else {
            binding.arrowIcon.visibility = View.GONE
        }


        // 댓글 깊이에 따라 들여쓰기 설정
        val indentSize = context.resources.getDimensionPixelSize(R.dimen.comment_indent)
        val fixedIndentSize =
            context.resources.getDimensionPixelSize(R.dimen.fixed_comment_indent) // 일정한 들여쓰기 값
        val layoutParams = binding.root.layoutParams as ViewGroup.MarginLayoutParams

        layoutParams.marginStart = if (isParentComment) {
            if (item!!.depth >= 3) {
                fixedIndentSize
            } else {
                indentSize * item.depth
            }
        } else {
            if (item!!.depth >= 3) {
                fixedIndentSize
            } else {
                indentSize * (item.depth + 1)
            }
        }
        binding.root.layoutParams = layoutParams

        // 삭제 버튼 클릭 이벤트 처리
        binding.deleteReply.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val comment = datas[position]
                val commentId = comment.commentId

                // 댓글 삭제 요청
                deleteComment(commentId)
            }
        }

        val userImage = item?.userImage

        // 사용자 이미지 URL이 있다면 Glide로 이미지 로드
        if (!userImage.isNullOrBlank()) {
            Glide.with(context.requireContext().applicationContext)
                .load(userImage)
                .override(900, 900) // 이미지 크기 설정 (원하는 크기로 수정)
                .into(binding.userImage)
        }

        //현재 아이템의 위치를 확인하고,
        //해당 위치의 댓글에 대한 정보를 가져와 onReplyClick 메서드를 호출
        //그리고 showReplyDialog 메서드를 호출하여 답글 작성 다이얼로그를 표시
        binding.saveReply.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val parentComment = datas[position]
                listener?.onReplyClick(parentComment, boardId)
                context.showReplyDialog(parentComment, boardId)
            }
        }
    }

    private fun deleteComment(commentId: Long) {
        val commentService =
            (context.requireContext().applicationContext as MyApplication).commentService

        val deleteCommentCall = commentService.deleteComment(commentId)

        deleteCommentCall.enqueue(object : Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                if (response.isSuccessful) {
                    // 성공적으로 삭제되었을 때의 처리
                    Toast.makeText(
                        context.requireContext().applicationContext,
                        "댓글이 성공적으로 삭제되었습니다.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // 삭제 후 해당 댓글을 리스트에서 제거
                    datas = datas.filterNot { it.commentId == commentId }
                    notifyDataSetChanged()
                } else {
                    val errorBody = response?.errorBody()?.string()
                    // 삭제 실패 시의 처리

                    Toast.makeText(
                        context.requireContext().applicationContext,
                        "댓글 삭제에 실패했습니다 $errorBody.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CommentAdapter", "댓글 작성 실패. 에러 메시지: $errorBody")
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                // 네트워크 오류 또는 예외 발생 시의 처리
                Toast.makeText(
                    context.requireContext().applicationContext,
                    "댓글 삭제 중 오류가 발생했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

}

// 댓글 작성 시간을 기반으로 "얼마 전" 형식의 텍스트 계산
@RequiresApi(Build.VERSION_CODES.O)
private fun getTimeAgoText(commentTime: String?): String {
    val commentDateTime = LocalDateTime.parse(commentTime, DateTimeFormatter.ISO_DATE_TIME)
    val now = LocalDateTime.now()
    val duration = Duration.between(commentDateTime, now)

    return when {
        duration.toMinutes() < 1 -> "Just now"
        duration.toHours() < 1 -> "${duration.toMinutes()} minutes ago"
        duration.toDays() < 1 -> "${duration.toHours()} hours ago"
        else -> "${duration.toDays()} days ago"
    }
}
