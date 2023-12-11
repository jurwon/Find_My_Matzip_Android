package com.example.find_my_matzip

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.find_my_matzip.databinding.ActivityMainBinding
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.PermissionManager

class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private var LOCATION_PERMISSION_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (checkLocationPermission()) {
            // 위치 권한이 이미 부여되어 있다면 위치 관련 작업 수행
            // 예: 위치 업데이트 요청, 맵 표시 등
        } else {
            // 위치 권한이 부여되어 있지 않다면 권한 요청
            requestLocationPermission()
        }


        //자동로그인 설정돼있다면
        val autoLogin = SharedPreferencesManager.getBoolean("autoLogin",false)
        if(autoLogin){
            Toast.makeText(this@MainActivity,"자동 로그인", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, HomeTabActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.joinBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, JoinActivity::class.java)
            startActivity(intent)
        }
        binding.loginBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
//        binding.moveHome.setOnClickListener {
//            val intent = Intent(this@MainActivity, TabActivity::class.java)
//            startActivity(intent)
//        }
    } //oncreate

    private fun checkLocationPermission(): Boolean {
        return (ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED)
    }


    private fun requestLocationPermission() {

        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            LOCATION_PERMISSION_CODE
        )
    }
}