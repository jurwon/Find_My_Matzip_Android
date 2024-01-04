package com.example.find_my_matzip.model

data class BoardDtlDto(
    val feelingBoardDtlDto : FeelingBoardDtlDto,
    val restaurant : RestaurantDto,
    val users : UsersFormDto,
    val board : BoardDto,
    val commentsPage: CommentsPageDto,
)