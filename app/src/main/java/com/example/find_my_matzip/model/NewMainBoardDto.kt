package com.example.find_my_matzip.model

import com.google.gson.annotations.SerializedName

data class NewMainBoardDto (
    val id : Int,
    val resId : String,
    val modifiedBy : String,
    //게시글에 해당하는 유저의 정보 (아이디, 이름, 프로필이미지)
    val user : MainBoardUserDto,
    val boardViewStatus : String,
    @SerializedName("board_title")
    val boardTitle : String,
    val content : String,
    val score : Int,
    val boardImgDtoList : List<NewImgDto>
)