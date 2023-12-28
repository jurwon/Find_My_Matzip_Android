package com.example.find_my_matzip.model

import com.google.gson.annotations.SerializedName
//가져오는 데이터 타입

// res 객체
data class RestaurantDto(
    @SerializedName("resId") val res_id: Long,
    @SerializedName("operate_time") val operate_time: String,
    @SerializedName("res_address") val res_address: String,
    @SerializedName("res_district") val res_district: String,
    //@SerializedName("res_image") val res_image: String,
    @SerializedName("res_intro") val res_intro: String,
    @SerializedName("res_lat") val res_lat: String,
    @SerializedName("res_lng") val res_lng: String,
    @SerializedName("res_menu") val res_menu: String,
    @SerializedName("res_name") val res_name: String,
    @SerializedName("res_phone") val res_phone: String,
    @SerializedName("res_thumbnail") val res_thumbnail: String,
    @SerializedName("avgScore") val avgScore: Double
    // val avgScore:Double,
)
