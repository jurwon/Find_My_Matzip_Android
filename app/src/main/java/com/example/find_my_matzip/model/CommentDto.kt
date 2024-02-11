package com.matzip.find_my_matzip.model

import java.io.Serializable

// 댓글 리스트를 갖는 데이터 클래스: children 속성은 해당 댓글의 하위 댓글들을 나타냄
data class CommentDto(
    var commentId: Long = 0, // 기본값 0으로 설정
    val commentWriter: String,
    val commentContents: String,
    val boardId: Long,
    val depth: Int,
    val parentId: Long? = null, // 부모 댓글의 ID, 상위 댓글인 경우는 null로 초기화
    val userImage: String?,
    val commentCreatedTime: String,
    var children: MutableList<CommentDto> = mutableListOf()
): Serializable

