package com.matzip.find_my_matzip.retrofit

import com.matzip.find_my_matzip.model.LoginDto
import com.matzip.find_my_matzip.model.MainBoardUserDto
import com.matzip.find_my_matzip.model.ProfileDto
import com.matzip.find_my_matzip.model.ResultDto
import com.matzip.find_my_matzip.model.UserListModel
import com.matzip.find_my_matzip.model.UsersFormDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    //회원 삭제
    @DELETE("users/delete/{userid}")
    fun deleteById(@Path("userid") userid: String?): Call<UsersFormDto>


//    @GET("users/profile/{pageUserid}")
//    fun getProfile(@Path("pageUserid"),@Query("page"), pageUserid: String?): Call<ProfileDto>

    @GET("users/profile/{pageUserid}")
    fun getProfile(
        @Path("pageUserid") pageUserid: String?,
        @Query("page") page: Int // 페이지 값 추가
    ): Call<ProfileDto>

    //회원 정보 수정
    @POST("/users/updateUsers")
    fun updateUsers(@Body usersFormDto: UsersFormDto): Call<Unit>

    @DELETE("/users/deleteFollow/{toUserId}")
    fun deleteFollow(@Path("toUserId") toUserId: String): Call<Unit>

    @GET("/users/insertFollow/{toUserId}")
    fun insertFollow(@Path("toUserId") toUserId: String): Call<Unit>

    @GET("/users/getAllUsers/{text}")
    fun getAllUsers(@Path("text") text : String,
                    @Query("page") page: Int): Call<List<UsersFormDto>>


}
