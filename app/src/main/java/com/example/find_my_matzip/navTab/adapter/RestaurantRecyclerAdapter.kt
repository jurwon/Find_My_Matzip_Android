package com.matzip.find_my_matzip.navTab.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.RestaurantListBinding
import com.matzip.find_my_matzip.model.RestaurantDto
import com.matzip.find_my_matzip.navTab.navTabFragment.RestaurantDtlFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.RestaurantFragment

class RestaurantViewHolder(val binding: RestaurantListBinding) : RecyclerView.ViewHolder(binding.root)

class RestaurantRecyclerAdapter(val context: RestaurantFragment, var datas: List<RestaurantDto>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RestaurantViewHolder(
            RestaurantListBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    fun addData(newData: List<RestaurantDto>) {
        val oldSize = datas?.size ?: 0
        datas = datas.orEmpty() + newData
        notifyItemRangeInserted(oldSize, newData.size)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as RestaurantViewHolder).binding
        val Item = datas?.get(position)

        binding.resId.text = Item?.res_id.toString()
        binding.resName.text = Item?.res_name
        binding.resDistrict.text= Item?.res_district
        binding.resMenu.text = Item?.res_menu
        binding.resIntro.text = Item?.res_intro
//        binding.avgScore.text = Item?.avgScore.toString()

        //소수 둘째자리에서 반올림
        val ratingUpScore = Item?.avgScore?.toFloat()?.let {
            kotlin.math.round(it * 10) / 10.toFloat()}!!
        binding.avgScore.text = ratingUpScore.toString()

        Glide.with(context)
            .load(Item?.res_thumbnail)
            .override(900,900)
            .into(binding.resThumbnail)

        binding.toResDtl.setOnClickListener {
            val resId = Item?.res_id
            // Toast.makeText(binding.root.context, "resId: $resId", Toast.LENGTH_SHORT).show()

            // 데이터를 전달하기 위한 Bundle 생성
            val bundle = Bundle().apply {
                if (resId != null) {
                    putLong("resId", resId)
                }
            }

            // RestaurantDtlFragment의 인스턴스 생성
            val restaurantDtlFragment = RestaurantDtlFragment()
            // 인자로 데이터 전달
            restaurantDtlFragment.arguments = bundle

            // FragmentManager를 통해 Fragment 트랜잭션 시작
            val fragmentManager = (binding.root.context as AppCompatActivity).supportFragmentManager
            val transaction = fragmentManager.beginTransaction()

            // Fragment를 표시하는 코드
            transaction.add(R.id.fragmentContainer, restaurantDtlFragment)
            transaction.addToBackStack(null) // 필요에 따라 back stack에 추가
            // 트랜잭션 완료
            transaction.commit()
        }
    }
}