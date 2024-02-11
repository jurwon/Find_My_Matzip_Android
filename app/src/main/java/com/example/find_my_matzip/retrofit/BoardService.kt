package com.matzip.find_my_matzip.retrofit

import com.matzip.find_my_matzip.model.BoardDtlDto
import com.matzip.find_my_matzip.model.MainBoardDto
import com.matzip.find_my_matzip.model.NewMainBoardDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface BoardService {
    @GET(" ")
    fun getAllBoards(): Call<List<MainBoardDto>>

    @GET("pagerbleMain")
    fun getAllBoardsPager(@Query("page") page: Int): Call<List<MainBoardDto>>

    @GET("newPagerbleMain")
    fun getNewAllBoardsPager(@Query("page") page: Int): Call<List<NewMainBoardDto>>


    //검색결과 board(New Version)
    @GET("getSearchResultBoard/{text}")
    fun getSearchResultBoardsPager(@Path("text") text : String,
                             @Query("page") page: Int): Call<List<NewMainBoardDto>>


    //검색 결과 게시글 불러오기(Old Version)
    @GET("pagerbleMain/{text}")
    fun getSearchMainBoards(@Path("text") text : String,
                            @Query("page") page: Int): Call<List<MainBoardDto>>

    //식당상세페이지에서 게시글목록 불러오기(Old Version)
    @GET("pagerbleResBoard/{redId}")
    fun getSearchResBoards(@Path("redId") redId : Long,
                            @Query("page") page: Int): Call<List<MainBoardDto>>


    @GET("board/{id}") // 게시글 상세페이지;
    fun getBoardDtl(@Path("id") id : String): Call<BoardDtlDto>

    @GET("users/matjalal")
    fun getMatjalalBoards(@Query("page") page: Int): Call<List<MainBoardDto>>


    @GET("users/newmatjalal")
    fun getNewMatjalalBoards(@Query("page") page: Int): Call<List<NewMainBoardDto>>

    @POST("board/new3/{resId}")// 게시글작성페이지
    fun createBoard3(
        @Path("resId") id: Long, @Body boardDtoMap: MutableMap<String,Any>
    ): Call<Unit>

    @PUT("board/{boardId}/edit")
    fun editBoard(
        @Path("boardId") id: String, @Body boardDtoMap: MutableMap<String, Any>
    ): Call<Unit>

    @DELETE("/board/{boardId}")
    fun deleteBoard(@Path("boardId") boardId: Long): Call<Void>

}