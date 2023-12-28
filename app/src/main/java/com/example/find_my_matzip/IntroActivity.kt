package com.example.find_my_matzip

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.find_my_matzip.databinding.ActivityIntroBinding
import com.example.find_my_matzip.utils.ForcedTerminationService

class IntroActivity : AppCompatActivity() {
    lateinit var binding:ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityIntroBinding.inflate(layoutInflater)

        //백그라운드 서비스 start
        //이 서비스가 앱 강제종료여부 확인하고 같이 종료될것임.
        startService(Intent(this, ForcedTerminationService::class.java))

        val handler = Handler()
        handler.postDelayed(Runnable {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 3000) // 3초 있다 메인액티비티로


        setContentView(binding.root)

        val imageView = findViewById<ImageView>(R.id.imageView)
        Glide.with(this).load(R.raw.first).into(imageView)
    }
}