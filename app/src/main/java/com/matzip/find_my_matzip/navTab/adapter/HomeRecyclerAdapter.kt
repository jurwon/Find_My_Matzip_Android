package com.matzip.find_my_matzip.navTab.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ItemMainboardBinding
import com.matzip.find_my_matzip.model.MainBoardDto
import com.matzip.find_my_matzip.navTab.navTabFragment.HomeFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.boardDtlFragment

class MainBoardViewHolder(private val binding: ItemMainboardBinding, private val onItemClick: (String) -> Unit) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.allItem.setOnClickListener {
            onItemClick(binding.boardId.text.toString())
        }
    }

    fun bind(item: MainBoardDto) {
        binding.boardId.text = item.id
        binding.boardTitle.text = item.boardTitle
        //val formattedScore = String.format("%.1f", item.score ?: 0.0)
        binding.boardScore.rating = item.score.toFloat()

        Glide.with(binding.root)
            .load(item.imgUrl)
            .override(900, 900)
            .into(binding.boardThumbnail)
    }
}

class HomeRecyclerAdapter(context: Context) : RecyclerView.Adapter<MainBoardViewHolder>() {

    private val datas: MutableList<MainBoardDto> = mutableListOf()

    fun addData(newBoardList: List<MainBoardDto>) {
        datas.addAll(newBoardList)
        notifyDataSetChanged()
    }

    // 클릭 리스너 설정
    private var onItemClickListener: ((String) -> Unit)? = null

    fun setOnItemClickListener(listener: (String) -> Unit) {
        onItemClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainBoardViewHolder {
        val itemBinding = ItemMainboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MainBoardViewHolder(itemBinding) { boardId ->
            onItemClickListener?.invoke(boardId)
        }
    }

    override fun getItemCount(): Int {
        return datas.size
    }

    override fun onBindViewHolder(holder: MainBoardViewHolder, position: Int) {
        val item = datas[position]
        holder.bind(item)
    }
}

//class MainBoardViewHolder(private val binding: ItemMainboardBinding) : RecyclerView.ViewHolder(binding.root) {
//    init {
//        binding.allItem.setOnClickListener {
//            // 클릭 시 아이템 상세 정보 화면으로 이동하는 코드
//            val fragment = boardDtlFragment()
//            val fragmentManager = (binding.root.context as FragmentActivity).supportFragmentManager
//            val transaction = fragmentManager.beginTransaction()
//            transaction.replace(R.id.fragmentContainer, fragment)
//            transaction.addToBackStack(null)
//            transaction.commit()
//        }
//    }
//
//    fun bind(item: MainBoardDto) {
//        binding.boardId.text = item.id
//        binding.boardTitle.text = item.boardTitle
//        binding.Score.text = item.score.toString()
//
//        Glide.with(binding.root)
//            .load(item.imgUrl)
//            .override(900, 900)
//            .into(binding.boardThumbnail)
//    }
//}
//
//class HomeRecyclerAdapter(private val context: HomeFragment) : RecyclerView.Adapter<MainBoardViewHolder>() {
//
//    private val datas: MutableList<MainBoardDto> = mutableListOf()
//
//    fun addData(newBoardList: List<MainBoardDto>) {
//        datas.addAll(newBoardList)
//        notifyDataSetChanged()
//    }
//
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MainBoardViewHolder {
//        val itemBinding = ItemMainboardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return MainBoardViewHolder(itemBinding)
//    }
//
//    override fun getItemCount(): Int {
//        return datas.size
//    }
//
//    override fun onBindViewHolder(holder: MainBoardViewHolder, position: Int) {
//        val item = datas[position]
//        holder.bind(item)
//
//
//
//    }
//}