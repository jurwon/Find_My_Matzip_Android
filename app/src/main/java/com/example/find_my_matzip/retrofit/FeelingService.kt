package com.example.find_my_matzip.retrofit

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface FeelingService {

    // 좋아요 & 싫어요
    //newFeel => -1:싫어요, 0:취소, +1:좋아요
    @GET("setFeeling/{boardId}/{newFeel}")
    fun setFeeling(@Path("boardId") boardId : Long,@Path("newFeel") newFeel : Int): Call<Unit>

}
