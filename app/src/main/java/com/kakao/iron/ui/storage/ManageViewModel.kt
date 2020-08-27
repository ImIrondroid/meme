package com.kakao.iron.ui.storage

import androidx.databinding.ObservableField
import androidx.lifecycle.*
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.kakao.iron.data.local.room.SaveRepository
import com.kakao.iron.ui.base.BaseViewModel
import com.kakao.iron.util.coroutine.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageViewModel(
    private val saveRepository: SaveRepository,
    private val dispatchers: Dispatchers
) : BaseViewModel() {

    private var _storageList: MutableLiveData<List<StorageData>> = MutableLiveData(mutableListOf())
    var storageList: LiveData<List<StorageData>> = _storageList
    private var _labelList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    var labelList: LiveData<MutableList<String>> = _labelList
    private var _topList: MutableLiveData<MutableList<String>> = MutableLiveData(mutableListOf())
    var topList: LiveData<MutableList<String>> = _topList

    private val initList: ArrayList<Boolean> = arrayListOf()
    var specialList: LiveData<PagingData<StorageData>> = saveRepository.getSpecialListWithPaging()
        .cachedIn(viewModelScope)
        .asLiveData(coroutineContext)

    private var sortState: StorageState = StorageState.SortDescending
    var currentTab: Tab = Tab.OtherTab
    var currentLabelQuery: String = ""
    val stateWatcher: ObservableField<ActionState> = ObservableField(ActionState.Normal)

    @ExperimentalCoroutinesApi
    val queryChannel: BroadcastChannel<String> = BroadcastChannel(Channel.CONFLATED)
    @FlowPreview
    @ExperimentalCoroutinesApi
    val queryResult = queryChannel
        .asFlow()
        .debounce(500L)
        .asLiveData(coroutineContext)

    init {
        onStart()
    }

    fun onStart() {
        launch {
            _storageList.value = getStorageList(sortState)
            _labelList.value = extract(getLabelList().toString(), hashing = true)
            _topList.value = getTopList(extract(getLabelList().toString(), hashing = false), limitItemCount = 10)

            init()
        }
    }

    fun onSelect(query: String) {
        launch {
            _storageList.value = getSelectedLabelList(query)
        }
    }

    fun onAdd() {
        launch {
            val nowList = _storageList.value ?: listOf()
            nowList
                .filterIndexed { index, item -> item.special != initList[index] }
                .forEach { updateList(it) }

            _storageList.value?.map {
                it.action = ActionState.Normal
            }

            stateWatcher.set(ActionState.Normal)
        }
    }

    fun onDelete(data: StorageData) {
        launch {
            deleteData(data)
            _storageList.value = when(currentTab) {
                Tab.LabelTab -> getSelectedLabelList(currentLabelQuery)
                Tab.OtherTab -> getStorageList(sortState)
            }
        }
    }

    fun onCancel() {
        launch {
            stateWatcher.set(ActionState.Normal)
            _storageList.value = when(currentTab) {
                Tab.LabelTab -> getSelectedLabelList(currentLabelQuery)
                Tab.OtherTab -> getStorageList(sortState)
            }
        }
    }

    fun onOptionChanged(state: ActionState) {
        //아이템이 하나도 없을때는 상태변경하지 않음
        //if(storageList.value.isNullOrEmpty()) return

        launch {
            stateWatcher.set(state)
            _storageList.value = when(currentTab) {
                Tab.LabelTab -> getSelectedLabelList(currentLabelQuery)
                Tab.OtherTab -> getStorageList(sortState)
            }
            init()
        }
    }

    //즐겨찾기상태에서 체크하는 로직
    fun onItemSelected(position: Int) {
        _storageList.value?.let { it ->
            val currentState = it[position].special
            it[position].special=(currentState.not())
        }
    }

    fun onItemSortChanged(state: StorageState) {
        launch {
            _storageList.value = getStorageList(state)
            sortState = state
            init()
        }
    }

    private fun init() {
        if(initList.isNotEmpty())  {
            initList.clear()
        }

        _storageList.value?.forEach {
            initList.add(it.special)
        }
    }

    private suspend fun deleteData(data: StorageData) {
        withContext(dispatchers.single()) {
            saveRepository.deleteStorageData(data)
        }
    }

    private suspend fun updateList(data: StorageData) {
        withContext(dispatchers.single()) {
            saveRepository.updateStorageData(data)
        }
    }

    private suspend fun getLabelList(): List<String> {
        return withContext(dispatchers.single()) {
            saveRepository.getAllLabelList()
        }
    }

    private suspend fun getSelectedLabelList(query: String): List<StorageData> {
        return withContext(dispatchers.single()) {
            saveRepository.getSelectedQueryLabelList(query).apply {
                this.map {
                    it.action = stateWatcher.get() ?: ActionState.Normal
                }
            }
        }
    }

    private suspend fun getStorageList(state: StorageState): List<StorageData> {
        return withContext(dispatchers.single()) {
            when (state) {
                StorageState.SortDescending ->
                    saveRepository.getDescendingStorageList()
                StorageState.SortAscending ->
                    saveRepository.getDescendingStorageList().reversed()
                StorageState.SortQuery ->
                    saveRepository.getQuerySortedStorageList()
            }
        }.apply {
            this.map {
                it.action = stateWatcher.get() ?: ActionState.Normal
            }
        }
    }

    private fun extract(
        str: String,
        hashing: Boolean = true
    ): MutableList<String> {
        return str
            .replace("[^\uAC00-\uD7A3]".toRegex(), " ")
            .split(" ")
            .filter { it.isNotBlank() && it.isNotEmpty() }
            .run {
                when(hashing) {
                    true -> this.toHashSet()
                    false -> this
                }
            }
            .sorted()
            .toMutableList()
    }

    private fun getTopList(
        list: List<String>,
        limitItemCount: Int = 0
    ): MutableList<String> {
        val map = hashMapOf<String, Int>()
        val size = list.size
        list.forEach {
            if(map[it] == null) map[it] = 1
            else map[it] = map[it]?.plus(1) as Int
        }
        val newMap = map
            .toList()
            .sortedWith(compareByDescending {it.second})
            .toMap()
        return when(limitItemCount == 0) {
            true -> newMap.keys
                .toMutableList()
            false -> newMap.keys
                .take(limitItemCount.coerceAtMost(size))
                .toMutableList()
        }
    }

    sealed class Tab {
        object LabelTab: Tab()
        object OtherTab: Tab()
    }
}
