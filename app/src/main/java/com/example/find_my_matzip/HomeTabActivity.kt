package com.example.find_my_matzip

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.find_my_matzip.databinding.ActivityHomeTabBinding
import com.example.find_my_matzip.navTab.navTabFragment.HomeFollowFragment
import com.example.find_my_matzip.navTab.navTabFragment.HomeFragment
import com.example.find_my_matzip.navTab.navTabFragment.MapFragment
import com.example.find_my_matzip.navTab.navTabFragment.MyPageFragment
import com.example.find_my_matzip.navTab.navTabFragment.NewHomeFragment
import com.example.find_my_matzip.navTab.navTabFragment.ProfileFragment
import com.example.find_my_matzip.navTab.navTabFragment.RankingFragment
import com.example.find_my_matzip.navTab.navTabFragment.RestaurantFragment
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.example.find_my_matzip.utils.LoadingDialog
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeTabActivity : AppCompatActivity() {
    lateinit var binding: ActivityHomeTabBinding
    lateinit var toggle: ActionBarDrawerToggle

    private val TAG: String = "HomeTabActivity"
    private var loginUserId:String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeTabBinding.inflate(layoutInflater)
        setContentView(binding.root)




        // 툴바 , 업버튼
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toggle = ActionBarDrawerToggle(
            this@HomeTabActivity, binding.drawer, R.string.open, R.string.close
        )

        // 드로워 열어주기
        toggle.syncState()

        // 탭 레이아웃
        val tabLayout = binding.bottomNavigationView

        tabLayout.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.tab1 -> {
//                    replaceFragment(HomeFragment())
                    replaceFragment(NewHomeFragment())
                }

                R.id.tab2 -> {
                    replaceFragment(RestaurantFragment())
                }

                R.id.tab3 -> {
                    replaceFragment(MapFragment())
                }

                R.id.tab4 -> {
                    replaceFragment(RankingFragment())
                }

                R.id.tab5 -> {
                    replaceFragment(MyPageFragment())
                }

                else -> false
            }
            return@setOnNavigationItemSelectedListener true
        }
        replaceFragment(NewHomeFragment())

        //Drawer 네비게이션
        binding.mainDrawerView.setNavigationItemSelectedListener {
            if (it.title == "앱 종료") {

                // 팝업 띄우기 (stack 전부 지우고 앱 종료)
                showExitBuilder()

            } else if (it.title == "로그아웃") {
                Toast.makeText(this@HomeTabActivity, "로그아웃 화면 이동", Toast.LENGTH_SHORT).show()

                // sharedPreference, BackStack의 fragment전부 삭제
                logOut()

                //로그인 화면으로 이동
                val intent = Intent(this@HomeTabActivity, LoginActivity::class.java)
                startActivity(intent)

            } else if (it.title == "회원 탈퇴") {

                val builder = AlertDialog.Builder(this@HomeTabActivity)
                builder.setTitle("회원 탈퇴")
                builder.setMessage("정말 탈퇴하시겠습니까???????")
                builder.setNegativeButton("아니오") { dialog, which ->
                    // 아무 작업도 수행하지 않음
                }
                builder.setPositiveButton("예") { dialog, which ->

                    // 1. 비밀번호 확인 창 띄우기
                    // 2. 확인 후 DB삭제, firestore삭제
                    showVertifyPw()
                }
                builder.show()
            }

            true
        }


        //Drawer에 사용자 ID 넣기
        loginUserId = SharedPreferencesManager.getString("id","")

        val header: View? = binding.mainDrawerView.getHeaderView(0)
        val text:TextView = header?.findViewById(R.id.userIdTextView) as TextView
        text.text = "$loginUserId 님"
    }




    //비밀번호 확인
    private fun isCorrectPassword(enteredPassword: String): Boolean {
        val correctPw = SharedPreferencesManager.getString("pw", "")
        return correctPw == enteredPassword
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()

        // 프래그먼트가 이미 백 스택에 있는지 확인
        val fragmentTag = fragment.javaClass.simpleName
        val currentFragment = fragmentManager.findFragmentById(R.id.fragmentContainer)

        if (currentFragment != null && currentFragment.javaClass.simpleName == fragmentTag) {
            // 프래그먼트가 이미 백 스택에 있다면 아무것도 하지 않음
            return
        }

        // 백 스택에서 프래그먼트를 제거
        val fragmentInBackStack = fragmentManager.findFragmentByTag(fragmentTag)
        if (fragmentInBackStack != null) {
            fragmentTransaction.remove(fragmentInBackStack)
        }

        // 현재 프래그먼트를 새로운 것으로 교체
        fragmentTransaction.replace(R.id.fragmentContainer, fragment, fragmentTag)
        //    fragmentTransaction.addToBackStack(null) // 백 스택에 추가

        // 트랜잭션을 적용
        fragmentTransaction.commit()

        //        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragmentContainer, fragment)
//            .commit()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    //backstack의 fragment전부 삭제
    private fun clearBackStack() {
        val fragmentManager = supportFragmentManager
        val count = fragmentManager.backStackEntryCount
        for (i in 0 until count) {
            fragmentManager.popBackStack()
        }
    }

    private fun logOut(){
        //로그인 정보 지우기
        SharedPreferencesManager.clearPreferences()

        // BackStack의 fragment전부 삭제
        clearBackStack()
    }

    private fun showExitBuilder(){
        val builder = AlertDialog.Builder(this@HomeTabActivity)
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

    //비밀번호 확인 창
    private fun showVertifyPw(){

        //로딩 다이얼로그
        val loadingDialog = LoadingDialog(this)

        val et = EditText(this)
        et.gravity = Gravity.CENTER

        // 회원 탈퇴 전 본인 확인
        val passwordBuilder = AlertDialog.Builder(this@HomeTabActivity)
            .setTitle("본인 확인")
            .setMessage("비밀번호를 입력해주세요")
            .setView(et)
            .setPositiveButton("확인") { dialog, which ->

                val enteredPassword = et.text.toString()

                Log.d(TAG, "enteredPassword : $enteredPassword")

                //로딩창 띄우기
                loadingDialog.show()

                //비밀번호 확인
                if (isCorrectPassword(enteredPassword)) {

                    //회원정보 삭제 로직 추가
                    //1.DB에서 DATA 삭제
                    val userService = (applicationContext as MyApplication).userService
                    val call = userService.deleteById(loginUserId)

                    call.enqueue(object: Callback<Unit> {
                        override fun onResponse(call: Call<Unit>, response: Response<Unit>) {

                            Log.d(TAG, "Request URL: ${call.request().url()}")
                            Log.d(TAG, "Request Body: ${call.request().body()}")
                            Log.d(TAG, "Response Code: ${response.code()}")
                            if(response.isSuccessful) {
                                Log.d(TAG, "삭제 성공")

                                //2.firebase에서 이미지삭제
                                // 스토리지 접근 도구 ,인스턴스
                                val storage = MyApplication.storage
                                // 스토리지에 저장할 인스턴스
                                val storageRef = storage.reference

                                // 이미지 저장될 위치 및 파일명(파이어베이스)
                                val imgRef = storageRef.child("users_img/${loginUserId}.jpg")


                                imgRef.delete().addOnCompleteListener {
                                    // 파일 삭제 성공
                                    Log.d(TAG, " firestore 파일 삭제 성공")

                                    logOut()

                                    //로딩창 지우기
                                    loadingDialog.dismiss()

                                    val intent = Intent(this@HomeTabActivity, LoginActivity::class.java)
                                    startActivity(intent)

                                }.addOnFailureListener {
                                    //로딩창 지우기
                                    loadingDialog.dismiss()

                                    // 파일 삭제 실패
                                    Log.d(TAG, "firestore 파일 삭제 실패")
                                }


                            }else {
                                Log.d(TAG, "서버 응답 실패: ${response.code()}")

                                //로딩창 지우기
                                loadingDialog.dismiss()
                            }
                        }

                        override fun onFailure(call: Call<Unit>, t: Throwable) {
                            Log.d(TAG, "실패 ${t.message}")

                            //로딩창 지우기
                            loadingDialog.dismiss()
                            call.cancel()
                        }


                    })

                    Toast.makeText(
                        this@HomeTabActivity,
                        "비밀번호 확인 성공",
                        Toast.LENGTH_SHORT
                    ).show()



                } else {

                    //로딩창 지우기
                    loadingDialog.dismiss()
                    Toast.makeText(
                        this@HomeTabActivity,
                        "비밀번호가 일치하지 않습니다",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

        passwordBuilder.setNegativeButton("취소") { dialog, which ->
            // Cancelled password input
            Toast.makeText(this@HomeTabActivity, "비밀번호 입력이 취소되었습니다", Toast.LENGTH_SHORT)
                .show()
        }

        passwordBuilder.show()
    }


    override fun onBackPressed() {
        // 현재 화면에 표시된 프래그먼트의 종료 다이얼로그를 호출
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)

        when (currentFragment) {
            is NewHomeFragment -> currentFragment.showExitDialog()
            is HomeFollowFragment -> currentFragment.showExitDialog()
            is RestaurantFragment -> currentFragment.showExitDialog()
            is MapFragment -> currentFragment.showExitDialog()
            is RankingFragment -> currentFragment.showExitDialog()
            is MyPageFragment -> currentFragment.showExitDialog()
            //    is ProfileFragment -> currentFragment.showExitDialog()

            else -> super.onBackPressed()
        }
    }
    fun onBackPressed2(){
        super.onBackPressed()
    }

}