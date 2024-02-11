package com.matzip.find_my_matzip.model;

data class LoginDto (
    // 토큰 처리 부분 때문에
    // server의 users entity 변수명과 맞춰줘야함
    val userid:String,
    val user_pwd:String
)
