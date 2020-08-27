package com.kakao.iron.data.remote

import androidx.paging.PagingSource
import com.kakao.iron.ui.search.SearchData

class SearchPagingSource(
    private val searchApi: SearchApi,
    private val query: String
): PagingSource<Int, SearchData>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, SearchData> {
        return try {
            val nextPageNumber = params.key ?: 1
            val data = searchApi.getSearchListWithPaging(query, nextPageNumber)
                .documents.map { it ->
                    it.apply {
                        val shortDate = it.date.split(".")[0].replace("T", " ")
                        this.date = shortDate
                        val changedFormatUrl = this.thumbnailUrl.replace("130x130_85_c","0x200_85_hr")
                        this.thumbnailUrl = changedFormatUrl
                    }
                }
            LoadResult.Page(
                data = data,
                prevKey = if (nextPageNumber > 1) nextPageNumber - 1 else null,
                nextKey = nextPageNumber + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }
}