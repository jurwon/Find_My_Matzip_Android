package com.matzip.find_my_matzip.model

data class ProfileDto(
    val boards : BoardsDto,
    val pageUserDto : PageUserDto,
    val followerDtoList : List<FollowDto>,
    val followingDtoList: List<FollowDto>,
    val countBoard: Int,
    val loginUserDto: UsersFormDto,
    val countFromUser: Int,
    val countToUser: Int,
    val followcheck: Boolean,
    )
