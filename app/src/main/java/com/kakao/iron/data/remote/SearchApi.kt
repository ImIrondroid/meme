package com.kakao.iron.data.remote

import com.kakao.iron.data.remote.model.SearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchApi {

    @GET("/v2/search/image")
    suspend fun getSearchListWithPaging(
        @Query("query") query: String,
        @Query("page") page: Int
    ): SearchResponse
}