package com.matzip.find_my_matzip.navTab.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.matzip.find_my_matzip.databinding.NearRestaurantListBinding
import com.matzip.find_my_matzip.model.RestaurantDto
import com.matzip.find_my_matzip.navTab.navTabFragment.NearRestaurantFragment
import com.matzip.find_my_matzip.navTab.navTabFragment.RestaurantDtlFragment


class NearRestaurantViewHolder(val binding: NearRestaurantListBinding) : RecyclerView.ViewHolder(binding.root)

class NearRestaurantRecyclerAdapter(
        val fragment: NearRestaurantFragment,
        private var nearRestaurantList: List<RestaurantDto>
) : RecyclerView.Adapter<NearRestaurantViewHolder>() {
        init {
                // avgScore 내림차순으로 정렬
                nearRestaurantList = nearRestaurantList.sortedByDescending { it.avgScore }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NearRestaurantViewHolder {
                return NearRestaurantViewHolder(
                        NearRestaurantListBinding.inflate(
                                LayoutInflater.from(parent.context),
                                parent,
                                false
                        )
                )
        }

        override fun getItemCount(): Int {
                return nearRestaurantList.size
        }

        override fun onBindViewHolder(holder: NearRestaurantViewHolder, position: Int) {
                val binding = holder.binding
                val item = nearRestaurantList[position]

                binding.resId.text = item.res_id.toString()
                binding.resName.text = item.res_name
                binding.resMenu.text = item.res_menu
                binding.resPhone.text = item.res_phone
                binding.avgScore.text = item.avgScore.toString()

                // Glide에서 null 체크 추가
                if (item.res_thumbnail != null) {
                        Glide.with(binding.root.context)
                                .load(item.res_thumbnail)
                                .override(150, 150)
                                .into(binding.resThumbnail)
                }


                binding.toResDtl.setOnClickListener {
                        val resId = item.res_id
                        // Toast.makeText(binding.root.context, "resId: $resId", Toast.LENGTH_SHORT).show()

                        // 데이터를 전달하기 위한 Bundle 생성
                        val bundle = Bundle().apply {
                                putLong("resId", resId)
                        }
                        // RestaurantDtlFragment의 인스턴스 생성
                        val restaurantDtlFragment = RestaurantDtlFragment()
                        // 인자로 데이터 전달
                        restaurantDtlFragment.arguments = bundle

                        val fragmentManager = fragment.requireActivity().supportFragmentManager

                        // MapFragment 위에 RestaurantDtlFragment를 추가하고 백 스택에 추가하지 않음
                        fragmentManager.beginTransaction()
                                .remove(fragment)
                                .add(com.matzip.find_my_matzip.R.id.fragmentContainer, restaurantDtlFragment)
                                .addToBackStack(null)
                                .commit()

                }



        }


}