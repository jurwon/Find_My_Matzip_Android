package com.example.find_my_matzip.navTab.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.R
import com.example.find_my_matzip.model.NewImgDto

class NewHomeViewPagerAdapter(private val context: Context, private val boardImgDtoList: List<NewImgDto>) :
    RecyclerView.Adapter<NewHomeViewPagerAdapter.ImageViewHolder>() {

    private val imageUrls = boardImgDtoList.filter { it.imgUrl.isNotEmpty() }
    private var currentPosition = 0
    fun setCurrentPosition(position: Int) {
        currentPosition = position
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_home_view_pager, parent, false)

        return ImageViewHolder(view)
    }

    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val imageInfo = boardImgDtoList[position]

        if (imageInfo.imgUrl.isNotEmpty()) {
            if (imageInfo.repImgYn == "Y") {
                holder.loadImage(imageInfo.imgUrl)
            } else {
                val img = boardImgDtoList.firstOrNull { it.imgUrl.isNotEmpty() && it.repImgYn == "N" }
                img?.let { holder.loadImage(it.imgUrl) }
            }
        }
    }

    override fun getItemCount(): Int {
//        return minOf(boardImgDtoList.size, 5)
        //이미지 url이 있을때만 뷰페이저아이템만들기
        return imageUrls.size

    }

    inner class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.viewPagerImageView)

        fun loadImage(imageUrl: String) {
            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.readyimg)
                .error(R.drawable.noimg)
                .into(imageView)
        }
    }
}