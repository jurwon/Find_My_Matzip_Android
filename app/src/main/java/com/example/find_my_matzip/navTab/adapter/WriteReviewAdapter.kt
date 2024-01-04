package com.example.find_my_matzip.navTab.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.R

class WriteReviewAdapter(val context: Context, val items: ArrayList<Uri>) :
    RecyclerView.Adapter<WriteReviewAdapter.ViewHolder>() {
    // onItemClickListener 인터페이스 선언하기
    interface onItemClickListener {
        fun onItemClick(position: Int)
    }
    private lateinit var itemClickListener: onItemClickListener
    fun setItemClickListener(itemClickListener: onItemClickListener) {
        this.itemClickListener = itemClickListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WriteReviewAdapter.ViewHolder {
        val v =
            LayoutInflater.from(parent.context).inflate(R.layout.item_write_recycler, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.count()
    }

    override fun onBindViewHolder(holder: WriteReviewAdapter.ViewHolder, position: Int) {
        holder.bindItems(items[position])
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bindItems(item: Uri) {
            val imageArea = itemView.findViewById<ImageView>(R.id.imageArea)
            val delete = itemView.findViewById<ImageView>(R.id.btnDelete)

            delete.setOnClickListener {
                val position = adapterPosition
                itemClickListener.onItemClick(position)
            }
            Glide.with(context).load(item).into(imageArea)
        }
    }
}