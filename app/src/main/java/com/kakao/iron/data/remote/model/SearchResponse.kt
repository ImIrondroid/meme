package com.kakao.iron.data.remote.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.kakao.iron.ui.search.SearchData

data class SearchResponse(
    @SerializedName("total_count")
    @Expose
    val totalCount: Int,
    @SerializedName("pageable_count")
    @Expose
    val pageableCount: Int,
    @SerializedName("is_end")
    @Expose
    val isEnd: Boolean,
    @SerializedName("documents")
    @Expose
    val documents: List<SearchData>
)