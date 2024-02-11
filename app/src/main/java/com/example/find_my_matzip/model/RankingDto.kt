package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName

data class RankingDto(
    val resId:Long,
    @SerializedName("res_name")
    val resName:String,
    val avgScore:Double,
    // 프로필 이미지가 저장된 위치의 URL주소
    @SerializedName("res_thumbnail")
    val resThumbnail:String,
)
