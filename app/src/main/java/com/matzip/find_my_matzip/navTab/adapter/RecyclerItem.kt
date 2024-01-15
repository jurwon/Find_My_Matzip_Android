package com.matzip.find_my_matzip.navTab.adapter

//리사이클러뷰에 표시될 아이템 클래스 (리사이클러 안에 뷰페이저 넣기)
data class RecyclerItem (
    val name: String,
    val colors: ArrayList<String>
)