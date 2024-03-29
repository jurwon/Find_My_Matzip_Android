package com.matzip.find_my_matzip.navTab.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.databinding.ItemNewmainboardBinding
import com.matzip.find_my_matzip.model.CommentDto
import com.matzip.find_my_matzip.model.NewMainBoardDto
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator


class NewMainBoardViewHolder(
    private val binding: ItemNewmainboardBinding,
    private val onItemClick: (String) -> Unit,
    private val onUserClick: (String) -> Unit,
    private val onCommentClick: (String, List<CommentDto>) -> Unit
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: NewMainBoardDto) {
        //유저프로필이미지
        Glide.with(binding.root)
            .load(item.user.userImage)
            .override(900, 900)
            .into(binding.userProfileImg)
        //유저이름
        binding.userId.text = item.user.username
        //게시글아이디설정
        binding.boardId.text = item.id.toString()
        //게시글제목
        binding.boardTitle.text = item.boardTitle
        //게시글평점
        binding.boardScore.rating = item.score.toFloat()
        //게시글내용
        binding.boardContent.text = item.content
        //게시글더보기처리
        setViewMore(binding.boardContent, binding.viewMore)

        val viewPager = binding.ImgViewPager
        // 이미지 리사이클러뷰 어댑터 초기화
        val viewPagerAdapter = NewHomeViewPagerAdapter(binding.root.context, item.boardImgDtoList)
        viewPager.adapter = viewPagerAdapter

        // 인디케이터 추가
        val dotsIndicator: DotsIndicator = binding.dotsIndicator // 인디케이터 뷰의 ID를 넣어주세요
        dotsIndicator.setViewPager2(viewPager)

        binding.gotoboardDtl.setOnClickListener {
            onItemClick(binding.boardId.text.toString())
        }

        binding.userLinearLayout.setOnClickListener {
            onUserClick(item.user.userId) // 유저 아이디 클릭 이벤트 핸들링
        }

        binding.AllComment.setOnClickListener { onCommentClick(item.id.toString(), item.comments) }

        binding.AllComment.text = "댓글 (${getTotalCommentCount(item.comments)}개) 모두보기"
    }

    private fun setViewMore(contentTextView: TextView, viewMoreTextView: TextView) {
        contentTextView.post {
            val lineCount = contentTextView.layout.lineCount
            if (lineCount > 0) {
                if (contentTextView.layout.getEllipsisCount(lineCount - 1) > 0) {
                    // 더보기 표시
                    viewMoreTextView.visibility = View.VISIBLE

                    // 더보기 클릭 이벤트
                    viewMoreTextView.setOnClickListener {
                        contentTextView.maxLines = Int.MAX_VALUE
                        viewMoreTextView.visibility = View.GONE
                    }
                }
            }
        }
    }


    private fun getTotalCommentCount(comments: List<CommentDto>?): Int {
        return comments?.let {
            var totalCount = it.size
            for (comment in it) {
                if (comment.children != null) {
                    totalCount += getTotalCommentCount(comment.children)
                }
            }
            totalCount
        } ?: 0
    }


}
class NewHomeRecyclerAdapter(context: Context) : RecyclerView.Adapter<NewMainBoardViewHolder>() {

    private val datas: MutableList<NewMainBoardDto> = mutableListOf()

    fun addData(newBoardList: List<NewMainBoardDto>) {
        datas.addAll(newBoardList)
        notifyDataSetChanged()
    }

    // 클릭 리스너 설정
    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    private var onUserClickListener: ((String?) -> Unit)? = null // 유저 클릭 리스너 선언

    fun setOnUserClickListener(listener: (String?) -> Unit) {
        onUserClickListener = listener
    }

    private var onCommentClickListener: ((String, List<CommentDto>) -> Unit)? = null

    fun setOnCommentClickListener(listener: (String, List<CommentDto>) -> Unit) {
        onCommentClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMainBoardViewHolder {
        val itemBinding =
            ItemNewmainboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NewMainBoardViewHolder(itemBinding,
            { boardId ->
                onItemClickListener?.invoke(boardId)
            },
            { userId ->
                onUserClickListener?.invoke(userId) // User click listener
            },
            { boardId, comments ->
                val currentItem = datas.find { it.id.toString() == boardId }
                val commentList = currentItem?.comments ?: emptyList()
                onCommentClickListener?.invoke(boardId, commentList)
            }
        )
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: NewMainBoardViewHolder, position: Int) {
        val item = datas[position]
        holder.bind(item)
    }


}