package com.matzip.find_my_matzip.navTab.adapter

import android.os.Build
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ItemCommentBinding
import com.matzip.find_my_matzip.MyApplication
import com.matzip.find_my_matzip.model.CommentDto
import com.matzip.find_my_matzip.navTab.navTabFragment.CommentFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.Duration
import java.time.format.DateTimeParseException

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
    var datas: MutableList<CommentDto>,
    val listener: CommentAdapterListener? = null,
) : RecyclerView.Adapter<CommentViewHolder>() {
    // 대댓글 더 보기 여부를 저장하는 맵
    private val showMoreMap: MutableMap<Long, Boolean> = mutableMapOf()
    var onReplyClick: ((CommentDto, Long) -> Unit)? = null


//    fun updateData(newCommentList: List<CommentDto>) {
//        commentList = newCommentList
//        notifyDataSetChanged()
//    }

    init {
        // 최초에 디폴트로 모든 댓글을 보이게 설정
        datas.forEach { comment ->
            showMoreMap[comment.commentId] = true
            comment.children?.forEach { childComment ->
                showMoreMap[childComment.commentId] = true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        // XML 레이아웃을 데이터 바인딩을 통해 인플레이트
        val binding = ItemCommentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )

        return CommentViewHolder(binding)
    }

    fun updateData(newCommentList: List<CommentDto>) {
        datas += newCommentList
        notifyDataSetChanged()

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
                val innerAdapter = CommentAdapter(
                    context,
                    boardId,
                    item?.children?.toMutableList() ?: mutableListOf(),
                    listener
                )

                binding.recyclerViewChildren.layoutManager =
                    LinearLayoutManager(context?.requireContext()?.applicationContext)
                binding.recyclerViewChildren.adapter = innerAdapter
                binding.recyclerViewChildren.visibility = View.VISIBLE

                binding.showMoreReplies.text = "댓글숨기기"
            } else {
                if (item?.children?.isNotEmpty() == true) {

                    val initialChildren =
                        if (item?.children?.isNotEmpty() == true) item?.children else null
                    val innerAdapter = CommentAdapter(
                        context,
                        boardId,
                        initialChildren?.toMutableList() ?: mutableListOf(),
                        listener
                    )

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
            notifyItemChanged(position)
        }
        // 초기에 댓글이 숨겨져 있을 때
        if (!showMoreMap.getOrDefault(item?.commentId ?: -1, false)) {
            binding.recyclerViewChildren.visibility = View.GONE
            binding.showMoreReplies.text = "댓글더보기"
            binding.showMoreReplies.visibility = View.VISIBLE
        } else {
            // 최초에는 부모 댓글과 모든 자식 댓글을 보여줌
            val initialChildren = item?.children
            val innerAdapter = CommentAdapter(
                context,
                boardId,
                initialChildren?.toMutableList() ?: mutableListOf(),
                listener
            )
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

        layoutParams.marginStart = if (item!!.depth >= 1) {
            fixedIndentSize
        } else {
            if (isParentComment) {
                indentSize * item.depth
            } else {
                indentSize * (item.depth + 1)
            }
        }
        binding.root.layoutParams = layoutParams


        val loginUserId = SharedPreferencesManager.getString("id", "")
        // 댓글 작성자의 아이디
        val commentWriterId = item?.commentWriter

        if (loginUserId == commentWriterId) {
            // 현재 로그인한 사용자와 댓글 작성자가 동일한 경우 삭제 버튼 표시
            binding.deleteReply.visibility = View.VISIBLE
        } else {
            // 동일하지 않은 경우 삭제 버튼 숨김
            binding.deleteReply.visibility = View.GONE
        }


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

        binding.userImage.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val comment = datas[position]
                val userId = comment.commentWriter // 여기서 사용자 아이디를 가져와야 함

                if (!userId.isNullOrBlank()) {
                    // requireContext()를 통해 현재 컨텍스트에서 FragmentManager를 얻어옵니다.
                    val fragmentManager = context.requireActivity().supportFragmentManager

                    // 이제 fragmentManager를 사용하여 프로필로 이동하는 코드를 작성할 수 있습니다.
                    navigateToUserProfile(userId, fragmentManager)


                }
            }
        }
        binding.commentWriter.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val comment = datas[position]
                val userId = comment.commentWriter // 여기서 사용자 아이디를 가져와야 함

                if (!userId.isNullOrBlank()) {
                    // requireContext()를 통해 현재 컨텍스트에서 FragmentManager를 얻어옵니다.
                    val fragmentManager = context.requireActivity().supportFragmentManager

                    // 이제 fragmentManager를 사용하여 프로필로 이동하는 코드를 작성할 수 있습니다.
                    navigateToUserProfile(userId, fragmentManager)
                }
            }
        }

        //현재 아이템의 위치를 확인하고,
        //해당 위치의 댓글에 대한 정보를 가져와 onReplyClick 메서드를 호출
        //그리고 showReplyDialog 메서드를 호출하여 답글 작성 다이얼로그를 표시
//        binding.saveReply.setOnClickListener {
//            val position = holder.adapterPosition
//            if (position != RecyclerView.NO_POSITION) {
//                val parentComment = datas[position]
//                listener?.onReplyClick(parentComment, boardId)
//                listener?.onReplyClick(parentComment, boardId)
//                context.showReplyDialog(parentComment, boardId)
//
//            }
//        }


        binding.saveReply.setOnClickListener {
            val position = holder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                val parentComment = datas[position]
                listener?.onReplyClick(parentComment, boardId)
                context.onReplyClick(parentComment, boardId)
                Log.d("CommentAdapter", "parentComment${parentComment}")
                Log.d("CommentAdapter", "onReplyClick 호출. position: $position, parentComment: $parentComment, boardId: $boardId")

            }
        }

    }

    //    private fun navigateToUserProfile(userId: String, fragmentManager: FragmentManager) {
//        val fragment = ProfileFragment.newInstance(userId)
//        val transaction = fragmentManager.beginTransaction()
//            .add(R.id.fragmentContainer, fragment)
//            .addToBackStack(null)
//            .commit()
//    }
//private fun navigateToUserProfile(userId: String, fragmentManager: FragmentManager) {
//    val fragment = ProfileFragment.newInstance(userId)
//    val transaction = fragmentManager.beginTransaction()
//        .replace(R.id.fragmentContainer, fragment)
//        .addToBackStack(null)
//        .commit()
//}
//private fun navigateToUserProfile(userId: String, fragmentManager: FragmentManager) {
//    val fragment = ProfileFragment.newInstance(userId)
//    val transaction = fragmentManager.beginTransaction()
//        .replace(R.id.fragmentContainer, fragment) // replace로 수정
//        .addToBackStack(null)
//        .commit()
//}
    private fun navigateToUserProfile(userId: String, fragmentManager: FragmentManager) {
        // 뒷쪽의 BottomSheetDialogFragment를 찾음
        val bottomSheetFragment = fragmentManager.findFragmentByTag(CommentFragment.TAG)
        if (bottomSheetFragment is BottomSheetDialogFragment && bottomSheetFragment.isVisible) {
            // 현재 보여지고 있는 경우에만 dismiss 호출
            bottomSheetFragment.dismiss()
        }

// 현재의 프래그먼트를 제거
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)
        if (currentFragment != null) {
            fragmentManager.beginTransaction().remove(currentFragment).commitAllowingStateLoss()
        }

// 앞쪽에 새로운 프래그먼트 추가
        val fragment = ProfileFragment.newInstance(userId)
        val transaction = fragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commitAllowingStateLoss()}

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
                    datas = datas.filterNot { it.commentId == commentId }.toMutableList()
                    notifyDataSetChanged()
                } else {
                    val errorBody = response?.errorBody()?.string()
                    // 삭제 실패 시의 처리

                    Toast.makeText(
                        context.requireContext().applicationContext,
                        "댓글 삭제에 실패했습니다 $errorBody.",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.e("CommentAdapter", "삭제 실패. 에러 메시지: $errorBody")
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
// 댓글을 평탄화하는 함수
fun flattenComments(comments: List<CommentDto>): List<CommentDto> {
    val result = mutableListOf<CommentDto>()
    for (comment in comments) {
        result.add(comment)
        // 대댓글이 있다면 재귀적으로 추가
        comment.children?.let { result.addAll(flattenComments(it)) }
    }
    return result
}
@RequiresApi(Build.VERSION_CODES.O)
private fun getTimeAgoText(commentTime: String?): String {
    if (commentTime.isNullOrBlank()) {
        Log.e("CommentAdapter", "Comment time is null or blank")
        return "Unknown time"
    }

    try {
        val commentDateTime = LocalDateTime.parse(commentTime, DateTimeFormatter.ISO_DATE_TIME)
        val now = LocalDateTime.now()
        val duration = Duration.between(commentDateTime, now)

        return when {
            duration.toMinutes() < 1 -> "Just now"
            duration.toHours() < 1 -> "${duration.toMinutes()} minutes ago"
            duration.toDays() < 1 -> "${duration.toHours()} hours ago"
            else -> "${duration.toDays()} days ago"
        }
    } catch (e: DateTimeParseException) {
        Log.e("CommentAdapter", "Error parsing comment time: $commentTime", e)
        return "Unknown time"
    }
}



