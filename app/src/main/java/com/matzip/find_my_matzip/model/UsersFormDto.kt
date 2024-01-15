package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName
//가져오는 데이터 타입
//{
//    "userid": "1234",
//    "user_pwd": null,
//    "username": "남성",
//    "user_address": "서울 강남구 선릉로161길 24",
//    "user_role": "ADMIN",
//    "userphone": "01012345678",
//    "user_image": "/images/users/53ae1859-52d9-4358-a0f6-8905b21fb66a.jpg",
//    "gender": "남성"
//},


// Users 객체(로그인, 회원가입)
data class UsersFormDto(
    @SerializedName("userid") val userid: String,
    @SerializedName("user_pwd") val user_pwd: String,
    @SerializedName("username") val username: String,
    @SerializedName("user_address") val user_address: String,
    @SerializedName("user_role") val user_role: String,
    @SerializedName("userphone") val userphone: String,
    @SerializedName("user_image") val user_image: String,
    @SerializedName("gender") val gender: String?
)