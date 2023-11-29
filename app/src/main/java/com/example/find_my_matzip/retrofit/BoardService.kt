package com.example.find_my_matzip.retrofit

import com.example.find_my_matzip.model.BoardDtlDto
import com.example.find_my_matzip.model.BoardFormDto
import com.example.find_my_matzip.model.MainBoardDto
import com.example.find_my_matzip.model.ProfileDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardService {
    @GET(" ")
    fun getAllBoards(): Call<List<MainBoardDto>>

    @GET("pagerbleMain")
    fun getAllBoardsPager(@Query("page") page: Int): Call<List<MainBoardDto>>

    //검색 결과 게시글 불러오기
    @GET("searchMainBoard")
    fun getSearchMainBoards(@Query("page") page: Int,
                            @Query("text") text : String): Call<List<MainBoardDto>>

    
    @GET("board/{id}") // 게시글 상세페이지;
    fun getBoardDtl(@Path("id") id : String): Call<BoardDtlDto>

    @GET("users/matjalal")
    fun getMatjalalBoards(@Query("page") page: Int): Call<List<MainBoardDto>>


    // 게시글작성 gpt
    @POST("board/new/{resId}")
    fun createBoard(
        @Path("resId") id: String, @Body boardFormDto: BoardFormDto
    ): Call<Unit>

    @POST("board/new2/{resId}")
    fun createBoard2(
        @Path("resId") id: String, @Body boardFormDto: BoardFormDto
    ): Call<Unit>
//    @GET("board/829") // 게시글 상세페이지;
//    fun getBoardDtl(): Call<BoardDtlDto>



}