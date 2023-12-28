package com.example.find_my_matzip.navTab.navTabFragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.example.find_my_matzip.R
import com.example.find_my_matzip.databinding.FragmentResInfoBinding
import kotlin.math.roundToInt

class ResInfoFragment : Fragment() {



    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreate(savedInstanceState)
        val rootView = inflater.inflate(R.layout.fragment_res_info, container, false)

        val resInfoId = arguments?.getLong("resInfoId")
//        rootView.findViewById<LinearLayout>(R.id.toResDtl).setOnClickListener {
//            val bundle = Bundle()
//            bundle.putString("resId", resInfoId)
//
//        }

        val resInfoName = arguments?.getString("resInfoName")
        val textViewResInfoName = rootView.findViewById<TextView>(R.id.resInfoName)
        textViewResInfoName.text = resInfoName

        val resInfoAvgScore = arguments?.getDouble("resInfoAvgScore", 0.0)
        val textViewResInfoAvgScore = rootView.findViewById<TextView>(R.id.resInfoAvgScore)
        textViewResInfoAvgScore.text = resInfoAvgScore.toString()

        val resInfoMenu = arguments?.getString("resInfoMenu")
        val textViewResInfoMenu = rootView.findViewById<TextView>(R.id.resInfoMenu)
        textViewResInfoMenu.text = resInfoMenu

        val resInfoOT = arguments?.getString("resInfoOT")
        val textViewResInfoOT = rootView.findViewById<TextView>(R.id.resInfoOT)
        textViewResInfoOT.text = resInfoOT

        val resInfoIntro = arguments?.getString("resInfoIntro")
        val textViewResInfoIntro = rootView.findViewById<TextView>(R.id.resInfoIntro)
        textViewResInfoIntro.text = resInfoIntro

        val imageUrl = arguments?.getString("resInfoThumbnail")
        val imageView = rootView.findViewById<ImageView>(R.id.resInfoThumbnail)

        Glide.with(this)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.logo_b)
                    .error(R.drawable.logo_b)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
            )
            .into(imageView)

        // toResDtl 클릭 이벤트 핸들러
        fun navigateToResDetail(resId: Long) {

            //           val fragment = resId?.let { RestaurantDtlFragment.newInstance(it) }
        val fragment = RestaurantDtlFragment.newInstance(resId)
            val transaction = parentFragmentManager.beginTransaction()

            // 기존의 프래그먼트를 숨기고 새로운 프래그먼트를 표시
            for (existingFragment in parentFragmentManager.fragments) {
                transaction.hide(existingFragment)
            }

            if (fragment != null) {
                transaction
                    .add(R.id.fragmentContainer, fragment)  // 추가된 부분
                    .addToBackStack(null)
                    .commit()
            }
        }

        rootView.findViewById<LinearLayout>(R.id.toResDtl).setOnClickListener {
            val resId = resInfoId
            if (resId != null) {
                navigateToResDetail(resId)
            }
        }
        // toResDtl 클릭 이벤트 핸들러


        return rootView

    }




//    @RequiresApi(Build.VERSION_CODES.R)
//    override fun onResume() {
//        super.onResume()
//        initLayout()
//    }
//
//    @RequiresApi(Build.VERSION_CODES.R)
//    private fun initLayout() {
//        var width = 0
//        var height = 0
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
//            val windowMetrics = requireActivity().windowManager.currentWindowMetrics
//            width = windowMetrics.bounds.width()
//            height = windowMetrics.bounds.height()
//        } else {
//            val display = requireActivity().windowManager.defaultDisplay
//            val displayMetrics = DisplayMetrics()
//            display.getRealMetrics(displayMetrics)
//            width = displayMetrics.widthPixels
//            height = displayMetrics.heightPixels
//        }
//
//        requireActivity().window.setGravity(Gravity.BOTTOM)
//        requireActivity().window.setLayout((width * 0.99).roundToInt(), (height * 0.3).roundToInt())
//        requireActivity().window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//    }

}