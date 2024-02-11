package com.matzip.find_my_matzip.model

data class NewImgDto(
    val id : Int,
    val boardId : Int,
    val imgName : String,
    val oriImgName : String,
    val imgUrl : String,
    val repImgYn : String
)