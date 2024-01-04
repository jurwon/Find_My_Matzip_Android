package com.example.find_my_matzip.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.databinding.ItemNewmainboardBinding
import com.example.find_my_matzip.model.NewMainBoardDto
import com.example.find_my_matzip.navTab.adapter.NewHomeViewPagerAdapter
import com.example.find_my_matzip.navTab.adapter.NewMainBoardViewHolder
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator


class BoardSearchResultViewHolder(private val binding :ItemNewmainboardBinding,private val onItemClick: (String) -> Unit,private val onUserClick: (String) -> Unit ) : RecyclerView.ViewHolder(binding.root) {
    fun bind(item: NewMainBoardDto) {
        //유저프로필이미지
        Glide.with(binding.root)
            .load(item.user.userImage)
            .override(40, 40)
            .into(binding.userProfileImg)
        //유저이름
        binding.userId.text = item.user.username
        //게시글아이디설정
        binding.boardId.text = item.id.toString()
        //게시글제목
        binding.boardTitle.text = item.boardTitle
        //게시글평점
        binding.boardScore.rating = item.score.toFloat()

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

    }
}
class BoardSearchResultRecyclerAdapter(context : Context) : RecyclerView.Adapter<BoardSearchResultViewHolder>() {

    private val datas : MutableList<NewMainBoardDto> = mutableListOf()

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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BoardSearchResultViewHolder {
        val itemBinding = ItemNewmainboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BoardSearchResultViewHolder(itemBinding,
            { boardId ->
                onItemClickListener?.invoke(boardId)
            },
            { userId ->
                onUserClickListener?.invoke(userId) // 유저 클릭 리스너 호출
            }
        )
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: BoardSearchResultViewHolder, position: Int) {
        val item = datas[position]
        holder.bind(item)
    }



}