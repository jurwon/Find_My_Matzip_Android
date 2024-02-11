package com.matzip.find_my_matzip.model

import com.google.gson.annotations.SerializedName

data class BoardDto(
    val id: Long,
    val resId: Long,
    @SerializedName("user_id")
    val userId: String,
//    val boardViewStatus: String?, // 이 부분은 서버 응답에 따라 실제 타입 확인이 필요할 수 있습니다.
    @SerializedName("board_title")
    val boardTitle: String,
    val content: String,
    val score: Int,
    val boardImgDtoList: List<BoardImgDto>, // BoardImgDto 클래스 필요
//    val boardImgIds: List<Long>

)
