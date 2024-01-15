package com.matzip.find_my_matzip.navTab.navTabFragment.homeFragChildFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.matzip.find_my_matzip.databinding.FragmentChildHome1Binding


class ChildHomeFragment1 : Fragment() {
    lateinit var binding : FragmentChildHome1Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChildHome1Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChildHome1Binding.inflate(layoutInflater,container,false)
        return binding.root
    }


}