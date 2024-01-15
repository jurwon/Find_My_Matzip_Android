package com.matzip.find_my_matzip.navTab.navTabFragment

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.matzip.find_my_matzip.R
import com.matzip.find_my_matzip.databinding.ActivityResInfoBinding
import kotlin.math.roundToInt


class ResInfoActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_res_info)

        var resInfoId = intent.getStringExtra("resInfoId")
        Log.d("acttest", "${resInfoId}")
        val bundle = Bundle()
        bundle.putString("resId", resInfoId)

        findViewById<LinearLayout>(R.id.toResDtl).setOnClickListener {
            Toast.makeText(this,"resId:${resInfoId}",Toast.LENGTH_SHORT).show()
        }


        val resInfoName = intent.getStringExtra("resInfoName")
        val textViewresInfoName = findViewById<TextView>(R.id.resInfoName)
        textViewresInfoName.text = resInfoName
        Log.d("acttest","${resInfoName}")

        val resInfoAvgScore = intent.getDoubleExtra("resInfoAvgScore", 0.0)
        val textViewresInfoAvgScore = findViewById<TextView>(R.id.resInfoAvgScore)
        textViewresInfoAvgScore.text = resInfoAvgScore.toString()
        Log.d("acttest","${resInfoAvgScore}")


        val resInfoMenu = intent.getStringExtra("resInfoMenu")
        val textViewresInfoMenu = findViewById<TextView>(R.id.resInfoMenu)
        textViewresInfoMenu.text = resInfoMenu

        val resInfoOT = intent.getStringExtra("resInfoOT")
        val textViewresInfoOT = findViewById<TextView>(R.id.resInfoOT)
        textViewresInfoOT.text = resInfoOT

        val resInfoIntro = intent.getStringExtra("resInfoIntro")
        val textViewresInfoIntro = findViewById<TextView>(R.id.resInfoIntro)
        textViewresInfoIntro.text = resInfoIntro

        val imageUrl = intent.getStringExtra("resInfoThumbnail")

        val imageView = findViewById<ImageView>(R.id.resInfoThumbnail)

        Glide.with(this)
            .load(imageUrl)
            .apply(
                RequestOptions()
                    .placeholder(R.drawable.logo_b) // 이미지 로딩 중 표시할 임시 이미지
                    .error(R.drawable.logo_b) // 이미지 로딩 실패 시 표시할 이미지
                    .diskCacheStrategy(DiskCacheStrategy.ALL) // 디스크 캐시 전략 설정
            )
            .into(imageView)


    }


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()
        initLayout()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun initLayout(){
        var width = 0
        var height = 0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) { // API Level 30 버전
            val windowMetrics = windowManager.currentWindowMetrics
            width = windowMetrics.bounds.width()
            height = windowMetrics.bounds.height()
        } else { // API Level 30 이전 버전
            val display = windowManager.defaultDisplay
            val displayMetrics = DisplayMetrics()
            display.getRealMetrics(displayMetrics)
            width = displayMetrics.widthPixels
            height = displayMetrics.heightPixels
        }
        // 바닥에 붙여서 나오게 설정
        window.setGravity(Gravity.BOTTOM)
        window.setLayout((width *0.99).roundToInt(), (height *0.3).roundToInt())
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))    // 배경화면 투명하게 하는 코드


    }


//    override fun onTouchEvent(event: MotionEvent?): Boolean {
//        // 화면 터치 시 취소 키로 간주하고 액티비티 종료
//        if (event?.action == MotionEvent.ACTION_DOWN) {
//            finish()
//            return true
//        }
//        return super.onTouchEvent(event)
//    }

}