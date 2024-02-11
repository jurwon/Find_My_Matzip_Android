package com.matzip.find_my_matzip.navTab.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.matzip.find_my_matzip.databinding.ViewPagerHomeColorBinding

class HomeFollowViewPagerAdapter  : RecyclerView.Adapter<HomeFollowViewPagerAdapter.ViewHolder>() {

    lateinit var items: ArrayList<String>

    fun build(i: ArrayList<String>): HomeFollowViewPagerAdapter {
        items = i
        return this
    }

    class ViewHolder(val binding: ViewPagerHomeColorBinding) : RecyclerView.ViewHolder(binding.root) {
        //        fun bind(item: String) {
        fun bind(item: String) {
            //뷰페이저에 글은 필요없다.
//            binding.viewPagerColor.text = item
            binding.pagerImg.setOnClickListener {
                Toast.makeText(binding.root.context,"뷰페이저 터치됨", Toast.LENGTH_SHORT).show();
                Log.d("kkt","뷰페이저 사진 선택됨")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFollowViewPagerAdapter.ViewHolder =
        ViewHolder(ViewPagerHomeColorBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: HomeFollowViewPagerAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}