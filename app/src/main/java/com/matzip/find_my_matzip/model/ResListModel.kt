package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName

data class ResListModel (
    @SerializedName("RESINFO")
    val data:List<RestaurantDto>
)