package com.matzip.find_my_matzip.navTab.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.matzip.find_my_matzip.databinding.HomeFragmentItem2Binding

class HomeFollowRecyclerAdapter : RecyclerView.Adapter<HomeFollowRecyclerAdapter.ViewHolder>() {
    lateinit var items: ArrayList<RecyclerItem2>

    fun build(i: ArrayList<RecyclerItem2>): HomeFollowRecyclerAdapter {
        items = i
        return this
    }

    class ViewHolder(val binding: HomeFragmentItem2Binding, val context: Context) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: RecyclerItem2) {
            with(binding)
            {
                tvPalette.text = item.name
                viewPager.adapter = HomeFollowViewPagerAdapter().build(item.colors)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeFollowRecyclerAdapter.ViewHolder =
        ViewHolder(
            HomeFragmentItem2Binding.inflate(LayoutInflater.from(parent.context), parent, false),
            parent.context
        )

    override fun onBindViewHolder(holder: HomeFollowRecyclerAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}