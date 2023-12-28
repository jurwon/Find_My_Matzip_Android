package com.example.find_my_matzip.navTab.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.ItemRankingBinding
import com.example.find_my_matzip.model.RankingDto
import com.example.find_my_matzip.navTab.navTabFragment.RankingFragment
import com.example.find_my_matzip.navTab.navTabFragment.RestaurantDtlFragment


//item_ranking
class RankingViewHoder(val binding: ItemRankingBinding) : RecyclerView.ViewHolder(binding.root)

class RankingRecyclerAdapter(val context: RankingFragment, val datas: List<RankingDto>?) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return RankingViewHoder(
            ItemRankingBinding.inflate(
                LayoutInflater.from(parent.context), parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return datas?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding = (holder as RankingViewHoder).binding
        val Item = datas?.get(position)

        // 번호 설정 (1부터 시작하도록 +1)
        binding.numberTextView.text = (position + 1).toString()

        binding.resId.text = Item?.resId.toString()
        binding.resName.text = Item?.resName
        binding.avgScore.text = Item?.avgScore.toString()

        Glide.with(context)
            .load(Item?.resThumbnail)
            .override(900, 900)
            .into(binding.resThumbnail)

        binding.toResDtl.setOnClickListener {
            val resId = Item?.resId
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
            transaction.replace(R.id.fragmentContainer, restaurantDtlFragment)
            transaction.addToBackStack(null) // 필요에 따라 back stack에 추가

            // 트랜잭션 완료
            transaction.commit()
        }
    }
}