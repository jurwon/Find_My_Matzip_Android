package com.example.find_my_matzip.navTab.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.databinding.ItemBoardsBinding
import com.example.find_my_matzip.databinding.ItemRankingBinding
import com.example.find_my_matzip.model.ContentDto
import com.example.find_my_matzip.model.RankingDto
import com.example.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.example.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.example.find_my_matzip.navTab.navTabFragment.RankingFragment


//item_boards
class BoardsViewHoder2(val binding: ItemBoardsBinding) : RecyclerView.ViewHolder(binding.root)

class BoardRecyclerAdapter2(val context: ProfileFragment, var datas: List<ContentDto>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return BoardsViewHoder2(
            ItemBoardsBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as BoardsViewHoder2).binding
        val Item = datas?.get(position)
        Log.d("BoardRecyclerAdapter", "Item is not null: $Item")
        binding.boardId.text = Item?.id
        binding.boardTitle.text = Item?.board_title
        binding.boardContent.text = Item?.content
        binding.boardScore.text = Item?.score

        Glide.with(context)
            .load(Item?.imgUrl)
            .override(900, 900)
            .into(binding.boardImgUrl)
        Log.e("BoardRecyclerAdapter", "Item is null at position $position")

    }


    }
