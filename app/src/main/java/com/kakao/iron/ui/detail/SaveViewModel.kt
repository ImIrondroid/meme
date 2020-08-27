package com.kakao.iron.ui.detail

import com.kakao.iron.data.local.room.SaveRepository
import com.kakao.iron.ui.base.BaseViewModel
import com.kakao.iron.ui.search.SearchData
import com.kakao.iron.ui.storage.StorageData
import com.kakao.iron.util.coroutine.Dispatchers
import kotlinx.coroutines.withContext

class SaveViewModel(
    private val saveRepository: SaveRepository,
    private val dispatchers: Dispatchers
) : BaseViewModel() {

    suspend fun onInsert(
        filePath: String = "default",
        form: Int,
        query: String,
        data: SearchData,
        textList: List<String>,
        labelList: List<String>
    ) {
        withContext(dispatchers.single()) {
            saveRepository.insertStorageData(
                StorageData(
                    id = 0L,
                    form = form,
                    query = query,
                    filePath = filePath,
                    collection = data.collection,
                    thumbnailUrl = data.thumbnailUrl,
                    imageUrl = data.imageUrl,
                    imageWidth = data.imageWidth,
                    imageHeight = data.imageHeight,
                    displaySiteName = data.displaySiteName,
                    documentUrl = data.documentUrl,
                    date = data.date,
                    text = textList,
                    label = labelList
                )
            )
        }
    }

    suspend fun checkForDuplicate(
        text: List<String>,
        label: List<String>
    ): Boolean {
        return withContext(dispatchers.single()) {
            saveRepository.checkForSameStorageData(
                text = text,
                label = label
            )
        }
    }

    suspend fun checkForDuplicate(
        query: String,
        data: SearchData
    ): Boolean {
        return withContext(dispatchers.single()) {
            saveRepository.checkForSameStorageData(
                query = query,
                thumbnailUrl = data.thumbnailUrl,
                imageUrl = data.imageUrl
            )
        }
    }
}