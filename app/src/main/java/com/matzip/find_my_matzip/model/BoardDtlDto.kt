package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName
data class BoardDtlDto(
    val restaurant : RestaurantDto,
    val users : UsersFormDto,
    val board : BoardDto,
)