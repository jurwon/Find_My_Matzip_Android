package com.example.find_my_matzip.retrofit

import com.example.find_my_matzip.model.CommentDto
import org.checkerframework.checker.units.qual.C
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


// 스프링의 Controller 와 같은 구간으로 스프링과 안드로이드 연동을 위한 서비스 구간
interface CommentService {

    @POST("comment/save")
    fun save(@Body commentDto: CommentDto?): Call<Unit>

    @POST("comment/saveReply/{parentId}")
    fun saveReply(
        @Body commentDto: CommentDto?,
        @Path("parentId") parentId: Long?
    ): Call<Unit>

    @DELETE("comment/delete/{commentId}")
    fun deleteComment(@Path("commentId") commentId: Long): Call<Unit>
}

//    @PUT("comments/{commentId}")
//    fun updateComment(@Path("commentId") commentId: Long, @Body updatedComment: CommentDto): Call<CommentDto>
