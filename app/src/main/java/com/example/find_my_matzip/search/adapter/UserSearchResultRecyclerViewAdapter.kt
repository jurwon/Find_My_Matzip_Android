package com.matzip.find_my_matzip.search.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.databinding.ItemSearchUserBinding
import com.matzip.find_my_matzip.model.MainBoardUserDto
import com.matzip.find_my_matzip.model.UsersFormDto

private val TAG: String = "UserSearchResultRecyclerViewAdapter"

class UserSearchItemViewHolder(val binding: ItemSearchUserBinding): RecyclerView.ViewHolder(binding.root)
class UserSearchResultRecyclerViewAdapter(val context: Context)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // 클릭 리스너 선언
    private var onUserClickListener: ((String?) -> Unit)? = null
    private val datas : MutableList<UsersFormDto> = mutableListOf()

    fun setOnUserClickListener(listener: (String?) -> Unit) {
        onUserClickListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return UserSearchItemViewHolder(
            ItemSearchUserBinding.inflate(
                LayoutInflater.from(parent.context),parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as UserSearchItemViewHolder).binding
        val item = datas?.get(position)

        //바인딩
        binding.userid.text = item?.userid
        val userImg = item?.user_image
        if(userImg != ""){
            Glide.with(context)
                .load(userImg)
                .override(900, 900)
                .into(binding.userImg)
        }

        binding.userItemLinearLayout.setOnClickListener {
            onUserClickListener?.invoke(item?.userid)
        }

    }

    fun addData(newUserList: List<UsersFormDto>) {
        datas.addAll(newUserList)
        notifyDataSetChanged()
    }

}