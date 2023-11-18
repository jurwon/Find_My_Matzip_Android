package com.example.find_my_matzip.navTab.navTabFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentHomeBinding
import com.example.find_my_matzip.navTab.adapter.HomeRecyclerAdapter
import com.example.find_my_matzip.navTab.adapter.RecyclerItem


class HomeFragment : Fragment() {
    lateinit var binding: FragmentHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentHomeBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)

        binding.homeRecyclerView.apply {
            adapter = HomeRecyclerAdapter().build(items)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        }

        binding.toFollowHome.setOnClickListener {
            // 클릭 시 HomeFollowFragment로 이동하는 코드
            val fragment = HomeFollowFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }

        return binding.root
    }

    val items = arrayListOf<RecyclerItem>(
        RecyclerItem("cozy", arrayListOf("pink", "purple", "sky blue", "white")),
        RecyclerItem(
            "rainbow",
            arrayListOf("red", "orange", "yellow", "green", "blue", "indigo", "purple")
        ),
        RecyclerItem(
            "Healthy Leaves",
            arrayListOf("Olive Green", "Lime Green", "Yellow Green", "Green")
        ),
        RecyclerItem("korea", arrayListOf("white", "black", "blue", "red")),
        RecyclerItem("usa", arrayListOf("blue", "red", "white")),
        RecyclerItem("italy", arrayListOf("green", "white", "red")),
        RecyclerItem("china", arrayListOf("red", "yellow")),
        RecyclerItem("google", arrayListOf("red", "yellow", "green", "blue")),
    )
}