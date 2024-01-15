package com.matzip.find_my_matzip.navTab.navTabFragment.homeFragChildFrags

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.FragmentChildHome1Binding
import com.matzip.find_my_matzip.databinding.FragmentChildHome2Binding

class ChildHomeFragment2 : Fragment() {
    lateinit var binding : FragmentChildHome2Binding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FragmentChildHome2Binding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentChildHome2Binding.inflate(layoutInflater,container,false)
        return binding.root
    }


}