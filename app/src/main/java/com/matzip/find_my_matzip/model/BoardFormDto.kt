package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName

//게시글작성
data class BoardFormDto(
    @SerializedName("user_id")
    val userId:String,
    val boardViewStatus:String,
    @SerializedName("board_title")
    val boardTitle:String,
    val content:String,
    val score:Int,
//    val boardImgDtos: MutableList<String>
    val boardImgDtoList: MutableList<BoardImgDto>,
)