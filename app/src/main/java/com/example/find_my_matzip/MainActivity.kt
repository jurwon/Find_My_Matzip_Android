package com.matzip.find_my_matzip

import android.R
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.matzip.find_my_matzip.databinding.ActivityMainBinding
import com.matzip.find_my_matzip.utils.SharedPreferencesManager
import com.google.firebase.database.FirebaseDatabase


class MainActivity : AppCompatActivity() {
    lateinit var binding : ActivityMainBinding
    private var LOCATION_PERMISSION_CODE = 1001

    // 파이어베이스 데이터베이스 연동
    private val database = FirebaseDatabase.getInstance()

    /*
    // DatabaseReference: 데이터베이스의 특정 위치로 연결 O - 키값(테이블 또는 속성)의 위치까지는 연결 X
    private val databaseReference = database.referenceㅎ

    var btn: Button? = null
    var edit1: EditText? = null
    var edit2:EditText? = null

     */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        /*
        btn = findViewById<Button>(R.id.btn) //버튼 아이디 연결

        edit1 = findViewById<EditText>(R.id.edit1) //동물 이름 적는 곳

        edit2 = findViewById<EditText>(R.id.edit2) //동물 종류 적는 곳


        // 버튼 누르면 값을 저장
        btn.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View?) {
                //에딧 텍스트 값을 문자열로 바꾸어 함수에 넣어줍니다.
                addanimal(edit1.getText().toString(), edit2.getText().toString())
            }
        })




         */

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