package com.matzip.find_my_matzip.model

data class UserListModel (
    val page:String,
    val perPage:String,
    val total:String,
    val totalPages:String,

    val data:List<UsersFormDto>
)