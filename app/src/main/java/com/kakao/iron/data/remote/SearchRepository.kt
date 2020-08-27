package com.kakao.iron.data.remote

import androidx.paging.PagingData
import com.kakao.iron.ui.search.SearchData
import kotlinx.coroutines.flow.Flow

interface SearchRepository {

    suspend fun getSearchListWithPaging(query: String): Flow<PagingData<SearchData>>
}