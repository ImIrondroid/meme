package com.kakao.iron.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kakao.iron.data.local.preferences.PreferenceHelper
import com.kakao.iron.ui.base.BaseViewModel
import com.kakao.iron.data.remote.SearchRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce

class SearchViewModel(
    private val searchRepository: SearchRepository,
    private val preference: PreferenceHelper
) : BaseViewModel() {

    private var _historyList: MutableLiveData<MutableList<HistoryData>> = MutableLiveData(mutableListOf())
    var historyList: LiveData<MutableList<HistoryData>> = _historyList

    private var searchResult: Flow<PagingData<SearchData>>? = null
    var searchQuery = ""

    @ExperimentalCoroutinesApi
    val queryChannel: BroadcastChannel<String> = BroadcastChannel(Channel.CONFLATED)
    @FlowPreview
    @ExperimentalCoroutinesApi
    val queryResult = queryChannel
        .asFlow()
        .debounce(500L)
        .asLiveData(coroutineContext)

    init {
        _historyList.value = preference.getAll()
    }

    fun remove(query: String) {
        preference.remove(query)
        _historyList.value = preference.getAll()
    }

    fun removeAll() {
        preference.removeAll()
        _historyList.value = preference.getAll()
    }

    suspend fun onSearch(query: String): Flow<PagingData<SearchData>> {
        preference.add(query)
        val lastResult = searchResult
        return if (query == searchQuery && lastResult != null) { lastResult }
        else {
            val newResult = searchRepository.getSearchListWithPaging(query).cachedIn(viewModelScope)
            searchQuery = query
            searchResult = newResult
            newResult
        }
    }
}