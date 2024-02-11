package com.matzip.find_my_matzip.model

data class FeelingDto(
    val id:Long,
    val feelingBoard:BoardDto,
    val feelingUsers:UsersFormDto,
    val feelNum:Int,
)
