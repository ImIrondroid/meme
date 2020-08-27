package com.kakao.iron.data.local.room

import androidx.paging.PagingData
import com.kakao.iron.ui.storage.StorageData
import kotlinx.coroutines.flow.Flow

interface SaveRepository {

    fun getDescendingStorageList(): List<StorageData>

    fun getQuerySortedStorageList(): List<StorageData>

    fun getSpecialListWithPaging(): Flow<PagingData<StorageData>>

    fun getAllLabelList(): List<String>

    fun getSelectedQueryLabelList(query: String): List<StorageData>

    fun insertStorageData(storageData: StorageData)

    fun updateStorageData(storageData: StorageData)

    fun deleteStorageData(storageData: StorageData)

    fun deleteStorageData(filePath: String)

    fun checkForSameStorageData(query: String, thumbnailUrl: String, imageUrl: String): Boolean

    fun checkForSameStorageData(text: List<String>, label: List<String>): Boolean
}