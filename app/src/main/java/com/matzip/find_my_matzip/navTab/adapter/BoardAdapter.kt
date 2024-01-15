package com.matzip.find_my_matzip.navTab.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ItemBoardsBinding
import com.matzip.find_my_matzip.databinding.ItemRankingBinding
import com.matzip.find_my_matzip.model.ContentDto
import com.matzip.find_my_matzip.model.RankingDto
import com.matzip.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.RankingFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.boardDtlFragment


//item_boards
class BoardsViewHoder(val binding: ItemBoardsBinding) : RecyclerView.ViewHolder(binding.root)

class BoardRecyclerAdapter(val context: MyPageFragment, var datas: List<ContentDto>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BoardsViewHoder(
            ItemBoardsBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )
    }

    fun addData(newData: List<ContentDto>) {
        val oldSize = datas?.size ?: 0
        datas = datas.orEmpty() + newData
        notifyItemRangeInserted(oldSize, newData.size)
    }


    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as BoardsViewHoder).binding
        val Item = datas?.get(position)
        Log.d("BoardRecyclerAdapter", "Item is not null: $Item")
//        binding.boardId.text = Item?.id
//        binding.boardTitle.text = Item?.board_title
//        binding.boardContent.text = Item?.content
//        binding.boardScore.text = Item?.score

        Glide.with(context)
            .load(Item?.imgUrl)
            .override(900, 900)
            .into(binding.boardImgUrl)
        Log.e("BoardRecyclerAdapter", "Item is null at position $position")

        binding.userContents.setOnClickListener {
            val id = Item?.id
            Log.d("BoardRecyclerAdapter", "보드의 아이디? : ${Item?.id}")


            val profileBoardDtlFragment = id?.let { it1 -> boardDtlFragment.newInstance(it1) }
            val parentFragmentManager = context.requireActivity().supportFragmentManager
            if (profileBoardDtlFragment != null) {
                parentFragmentManager.beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .add(R.id.fragmentContainer, profileBoardDtlFragment)
                    .addToBackStack(null)
                    .show(boardDtlFragment())
                    .commit()

                false
            }
        }


    }}