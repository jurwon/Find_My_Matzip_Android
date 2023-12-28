package com.example.find_my_matzip

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.find_my_matzip.databinding.ActivityLoginBinding
import com.example.find_my_matzip.model.LoginDto
import com.example.find_my_matzip.model.ResultDto
import com.example.find_my_matzip.retrofit.UserService
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.LoadingDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

// sy FB
private lateinit var auth: FirebaseAuth

class LoginActivity : AppCompatActivity() {
    private val TAG: String = "LoginActivity"
    lateinit var binding: ActivityLoginBinding
    lateinit var userService : UserService

    //editview 밖의 공간 클릭시 키보드 내리기 기능 구현
    //설정 1)
    private lateinit var imm: InputMethodManager

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // sy FB
        auth = Firebase.auth

        //로딩 다이얼로그
        val loadingDialog = LoadingDialog(this)

        //설정 2)
        imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // 툴바 붙이기
        setSupportActionBar(binding.toolbar)

        //시스템에 있는 액션바에 업버튼 붙이기
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        // sy FB
        binding.loginBtn.setOnClickListener{

            val loginId = binding.userId
            val loginPw = binding.userPwd

            if(loginId.text.isEmpty() && loginPw.text.isEmpty()) {
                Toast.makeText(this, "아이디와 비밀번호를 다시 입력해주세요.", Toast.LENGTH_SHORT).show()
                Log.d("Email", "$loginId, $loginPw")
                loginId.setText("")
                loginPw.setText("")
            }
            else{
                signIn(loginId.text.toString(), loginPw.text.toString())
            }
        }



        //로그인 버튼 클릭시
        binding.loginBtn.setOnClickListener {
            //로딩창 띄우기
            loadingDialog.show()

            val loginId = binding.userId.text.toString()
            val loginPw = binding.userPwd.text.toString()

            if (loginId.isEmpty() || loginPw.isEmpty()) {
                showDialog("blank")
                return@setOnClickListener
            }

            //로그인 요청 정보 객체에 담기
            val loginRequest = LoginDto(
                userid = loginId,
                user_pwd = loginPw
            )

            //연결 요청
            userService = (applicationContext as MyApplication).userService
            //로그인 요청
            val call = userService.login(loginRequest)
            Log.d(TAG, "로그인 요청 - ID: ${loginRequest.userid}, PW: ${loginRequest.user_pwd}")

            //응답받은 정보 확인
            call.enqueue(object : Callback<ResultDto> {
                override fun onResponse(call: Call<ResultDto>, response: Response<ResultDto>) {
                    if (response.isSuccessful) {

                        // API 요청 처리 성공
                        val token = response.headers().get("Authorization")
                        val loginState = response.body()

                        if (loginState != null) {
                            if(token != null){
                                Log.d(TAG, "로그인 성공")
                                val autoLogin = binding.autoLoginBtn.isChecked

                                // 로그인된 유저 정보 저장
                                SharedPreferencesManager.setLoginInfo(loginId,loginPw,token,autoLogin);

                                showDialog("success")

                                val header = response.headers()
                                Log.d(TAG, "===========response.body()의 값 : ${response.body()}")
                                Log.d(TAG, "===========response token의 값 : $token")

                                //로딩창 지우기
                                loadingDialog.dismiss()

                            }
                            else{
                                Log.d(TAG, "로그인 실패")
                                //로딩창 지우기
                                loadingDialog.dismiss()
                                showDialog("fail")
                            }
                        }

                    } else {
                        // API 요청 실패 처리
                        Log.d(TAG, "API 요청 실패")
                        //로딩창 지우기
                        loadingDialog.dismiss()
                        showDialog("fail")
                    }
                    Log.d(TAG, "통신 성공 - HTTP 상태 코드: ${response.code()}")
                    Log.d(TAG, "통신 성공 - 응답 메시지: ${response.body()}")
                }

                override fun onFailure(call: Call<ResultDto>, t: Throwable) {
                    //로딩창 지우기
                    loadingDialog.dismiss()
                    // 통신 실패 처리
                    showDialog("fail")
                    Log.e(TAG, "통신 실패: ${t.message}")
                }

            })
        }




        //회원가입창으로 이동
        binding.joinUs.setOnClickListener{
            val intent = Intent(this@LoginActivity, JoinActivity::class.java)
            startActivity(intent)
        }

        //설정 3)
        binding.root.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                // Hide the keyboard when the user clicks outside of the EditText
                val view = this.currentFocus
                if (view != null) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
            false
        }

    }//onCreate

    // sy FB
    private fun signIn(email: String, password: String) {
        val intent = Intent(this, HomeTabActivity::class.java)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("로그인", "성공")
                    val user = auth.currentUser
                    updateUI(user)
                    finish()
                    startActivity(intent)
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(this, "정확한 아이디와 비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    Log.d("로그인", "실패")
                    updateUI(null)
                }
            }
    }
    private fun updateUI(user: FirebaseUser?) {

    }


    // 로그인 성공/실패 시 다이얼로그를 띄워주는 메소드
    private fun showDialog(type: String) {
        val dialogBuilder = AlertDialog.Builder(this)

        if (type == "success") {
            dialogBuilder.setTitle("로그인 성공")
            dialogBuilder.setMessage("로그인 성공!")
        } else if (type == "fail") {
            dialogBuilder.setTitle("로그인 실패")
            dialogBuilder.setMessage("아이디와 비밀번호를 확인해주세요")
        } else if (type == "blank") {
            dialogBuilder.setTitle("입력 필요")
            dialogBuilder.setMessage("아이디와 비밀번호를 입력해주세요")
        }

        val dialogListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    Log.d(TAG, "확인 버튼 클릭")
                    if (type == "success") {
                        // 로그인 성공 시 처리할 작업 수행
                        val intent = Intent(this@LoginActivity, HomeTabActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
            }
        }

        dialogBuilder.setPositiveButton("확인", dialogListener)
        val dialog = dialogBuilder.create() // 다이얼로그 객체 생성
        dialog.show() // 다이얼로그 표시
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    //뒤로가기 비활성화
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        //super.onBackPressed()

        val builder = AlertDialog.Builder(this@LoginActivity)
        builder.setTitle("Exit?")
        builder.setMessage("앱을 종료하시겠습니까?")
        builder.setNegativeButton("아니오") { dialog, which ->
            // 아무 작업도 수행하지 않음
        }
        builder.setPositiveButton("예") { dialog, which ->
            // stack 전부 지우고 앱 종료
            finishAffinity()
        }
        builder.show()
    }

}