package com.matzip.find_my_matzip.search.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.matzip.find_my_matzip.databinding.ItemSearchHistoryBinding
import com.matzip.find_my_matzip.model.SearchDto
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import java.text.SimpleDateFormat
import java.util.Locale

private val TAG: String = "SearchHistoryRecyclerViewAdapter"

class SearchItemViewHolder(val binding:ItemSearchHistoryBinding):RecyclerView.ViewHolder(binding.root)

class SearchHistoryRecyclerViewAdapter(val context: Context, var datas: List<SearchDto>?)
    : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onSearchItemClickListener: OnSearchItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return SearchItemViewHolder(
            ItemSearchHistoryBinding.inflate(
                LayoutInflater.from(parent.context),parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    fun addData(newData: List<SearchDto>) {
        val oldSize = datas?.size ?: 0
        datas = datas.orEmpty() + newData
        notifyItemRangeInserted(oldSize, newData.size)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as SearchItemViewHolder).binding
        val item = datas?.get(position)

        binding.searchText.text = item?.text
        binding.searchDate.text = SimpleDateFormat("yyyy.MM.dd", Locale.getDefault()).format(item?.date)


        binding.constraintSearchItem.setOnClickListener{
            Log.d(TAG,"constraintSearchItem 클릭")

            val newText = binding.searchText.text.toString()

            //SearchActivity에 item click이벤트 알려줌.
            onSearchItemClickListener?.onSearchItemClick(newText)

        }

        binding.deleteSearchBtn.setOnClickListener {
            Log.d(TAG, "deleteBtn 클릭")

            val copyList = datas?.toMutableList()
            copyList?.remove(item)
            datas = copyList

            SharedPreferencesManager.deleteSearchHistory(item?.text.toString())

            //Adapter에 변경사항 적용(특정한 아이템 1개를 삭제할 때 사용)
            notifyDataSetChanged()
        }

    }

    //최근 검색어 한개 삭제시 목록 리로드
    fun clearData() {
        datas = emptyList()
        notifyDataSetChanged()
    }

    fun setOnSearchItemClickListener(listener: OnSearchItemClickListener) {
        onSearchItemClickListener = listener
    }

    //최근 검색어 클릭시 이벤트
    interface OnSearchItemClickListener {
        fun onSearchItemClick(item: String)
    }

}


