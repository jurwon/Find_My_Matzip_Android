package com.example.find_my_matzip.retrofit

import com.example.find_my_matzip.model.LoginDto
import com.example.find_my_matzip.model.ProfileDto
import com.example.find_my_matzip.model.ResultDto
import com.example.find_my_matzip.model.UserListModel
import com.example.find_my_matzip.model.UsersFormDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// 스프링의 Controller 와 같은 구간으로 스프링과 안드로이드 연동을 위한 서비스 구간
interface UserService {

    // 회원가입
    @POST("users/new")
    fun newUsers(@Body user: UsersFormDto): Call<Unit>

    //로그인
    @POST("/login")
    fun login(@Body loginDto: LoginDto): Call<ResultDto>

    //회원 조회
    @GET("users/admin/userList")
    fun getAll(): Call<UserListModel>

    //회원 한명 정보 조회
    @GET("users/aboutUsers/{userid}")
    fun findbyId(@Path("userid") userid: String?): Call<UsersFormDto>


    @GET("users/profile/{pageUserid}")
    fun getProfile(@Path("pageUserid") pageUserid: String?): Call<ProfileDto>

}
