package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Url

data class MainBoardDto(
    val id:String,
    @SerializedName("board_title")
    val boardTitle:String,
    val content : String,
    val imgUrl : String,
    val score : Int

)
