package com.example.find_my_matzip

import android.app.Application
import androidx.multidex.MultiDexApplication
import com.example.find_my_matzip.retrofit.AuthInterceptor
import com.example.find_my_matzip.retrofit.BoardService
import com.example.find_my_matzip.retrofit.CommentService
import com.example.find_my_matzip.retrofit.FeelingService
import com.example.find_my_matzip.retrofit.RestaurantService
import com.example.find_my_matzip.retrofit.UserService
import com.example.find_my_matzip.utiles.SharedPreferencesManager
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : MultiDexApplication() {
    companion object {
        // 이미지 저장소 , 인스턴스 도구
        lateinit var storage: FirebaseStorage
    }

    //통신에 필요한 인스턴스 선언 & 초기화
    var userService: UserService
    val restaurantService: RestaurantService
    val boardService : BoardService
    val feelingService: FeelingService
    var commentService : CommentService

    //인터셉터 생성
    //패킷 보낼때마다 header에 token 붙이는 코드 작성시 중복코드 너무 많이 생김
    //자동으로 sharedPreference의 token정보 꺼내서 header에 붙이는 인터셉터 작성
    val client = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor())
        .build()

    //통신할 서버의 URL주소 등록
    val retrofit = Retrofit.Builder()
        // 학원 ip?
        .baseUrl("http://192.168.0.14:80/")
        //인터셉터 적용
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    init {
        userService = retrofit.create(UserService::class.java)
        restaurantService = retrofit.create(RestaurantService::class.java)
        boardService = retrofit.create(BoardService::class.java)
        feelingService = retrofit.create(FeelingService::class.java)
        commentService = retrofit.create(CommentService::class.java)
    }

    // 생명주기, 최초 1회 동작을 합니다.
    override fun onCreate() {
        super.onCreate()
        // 초기화
        storage = Firebase.storage

        //앱 시작할때 SharedPreferencesManager 싱글톤 객체 생성
        SharedPreferencesManager.init(this)

    }

}