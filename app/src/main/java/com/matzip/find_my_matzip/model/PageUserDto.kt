package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName

data class PageUserDto(
    val userid: String,
    val username: String,
    val user_address: String,
    val user_role: String,
    val userphone: String,
    val user_image: String,
    val gender: String
//    val userId : String,
//    val userName : String,
//    @SerializedName("user_address")
//    val userAddress : String,
//    @SerializedName("user_role")
//    val userRole : String,
//    val userPhone : String,
//    @SerializedName("user_image")
//    val userImage : String,
//    val gender : String


)