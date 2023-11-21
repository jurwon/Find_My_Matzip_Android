package com.example.find_my_matzip.retrofit

import com.example.find_my_matzip.model.RankingDto
import retrofit2.Call
import retrofit2.http.GET


interface RestaurantService {
    @GET("/ranking")
//    fun getTop3RestaurantsByAvgScore(): Call<MutableList<RestaurantDto2>>
    fun getTop3RestaurantsByAvgScore(): Call<List<RankingDto>>
}