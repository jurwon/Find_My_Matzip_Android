package com.example.find_my_matzip.NavTab.NavTabFragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentMatZalalBinding
import com.example.find_my_matzip.databinding.FragmentSearchReviewBinding


class SearchReviewFragment : Fragment() {
    lateinit var binding: FragmentSearchReviewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentSearchReviewBinding.inflate(layoutInflater)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSearchReviewBinding.inflate(layoutInflater, container, false)
        return binding.root
    }
}