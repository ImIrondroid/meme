package com.kakao.iron.data.remote

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.kakao.iron.ui.search.SearchData
import kotlinx.coroutines.flow.Flow

class SearchRepositoryImpl(
    private val searchApi: SearchApi
) : SearchRepository {

    override suspend fun getSearchListWithPaging(query: String): Flow<PagingData<SearchData>> {
        return Pager(
            config = PagingConfig(
                initialLoadSize = 1,
                pageSize = MAX_PAGE_SIZE,
                enablePlaceholders = false
            ),
            initialKey = 1,
            pagingSourceFactory = { SearchPagingSource(searchApi, query) }
        ).flow
    }

    companion object {
        private const val MAX_PAGE_SIZE = 50
    }
}