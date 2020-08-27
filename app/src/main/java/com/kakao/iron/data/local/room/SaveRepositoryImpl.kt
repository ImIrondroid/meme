package com.kakao.iron.data.local.room

import androidx.paging.*
import com.google.gson.Gson
import com.kakao.iron.data.local.room.db.MemeDatabase
import com.kakao.iron.data.local.room.entity.MemeEntity
import com.kakao.iron.ui.storage.StorageData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SaveRepositoryImpl(
    private val database: MemeDatabase
) : SaveRepository {

    override fun getDescendingStorageList(): List<StorageData> {
        return database
            .imageDao()
            .getDescendingStorageList().map {
                toStorageData(it)
            }
    }

    override fun getQuerySortedStorageList(): List<StorageData> {
        return database
            .imageDao()
            .getQuerySortedStorageList().map {
                toStorageData(it)
            }
    }

    override fun getSpecialListWithPaging(): Flow<PagingData<StorageData>> {
        return Pager(PagingConfig(10)) {
            database.imageDao().getSpecialListWithPaging()
        }.flow.map {
            it.map { entity ->
                toStorageData(entity)
            }
        }
    }

    override fun getAllLabelList(): List<String> {
        return database
            .imageDao()
            .getAllLabelList()
    }

    override fun getSelectedQueryLabelList(query: String): List<StorageData> {
        return database
            .imageDao()
            .getSelectedQueryLabelList(query).map {
                toStorageData(it)
            }
    }

    override fun insertStorageData(storageData: StorageData) {
        database
            .imageDao()
            .insertEntity(toMemeEntity(storageData))
    }

    override fun updateStorageData(storageData: StorageData) {
        database
            .imageDao()
            .updateEntity(toMemeEntity(storageData))
    }

    override fun deleteStorageData(storageData: StorageData) {
        database
            .imageDao()
            .deleteEntity(toMemeEntity(storageData))
    }

    override fun deleteStorageData(filePath: String) {
        val id = database.imageDao()
            .getSelectedImage(filePath)
        database.imageDao().deleteEntity(MemeEntity(id = id))
    }

    override fun checkForSameStorageData(
        query: String,
        thumbnailUrl: String,
        imageUrl: String
    ): Boolean {
        val isEmpty = database
            .imageDao()
            .getSelectedImage(
                query = query,
                thumbnailUrl = thumbnailUrl,
                imageUrl = imageUrl
            )
        return isEmpty == null //이미 존재하면 false 존재하지 않으면 true
    }

    override fun checkForSameStorageData(
        text: List<String>,
        label: List<String>
    ): Boolean {
        val gson = Gson()
        val isEmpty = database
            .imageDao()
            .getSelectedImage(
                text = gson.toJson(text),
                label = gson.toJson(label)
            )
        return isEmpty == null //이미 존재하면 false 존재하지 않으면 true
    }

    private fun toStorageData(memeEntity: MemeEntity): StorageData {
        return StorageData(
            id = memeEntity.id,
            form = memeEntity.form,
            query = memeEntity.query,
            collection = memeEntity.collection,
            filePath = memeEntity.filePath,
            imageUrl = memeEntity.imageUrl,
            thumbnailUrl = memeEntity.thumbnailUrl,
            imageWidth = memeEntity.imageWidth,
            imageHeight = memeEntity.imageHeight,
            text = memeEntity.text,
            label = memeEntity.label,
            _special = memeEntity.isSpecial
        )
    }

    private fun toMemeEntity(storageData: StorageData): MemeEntity {
        return MemeEntity(
            id = storageData.id,
            form = storageData.form,
            query = storageData.query,
            collection = storageData.collection,
            filePath = storageData.filePath,
            imageUrl = storageData.imageUrl,
            thumbnailUrl = storageData.thumbnailUrl,
            imageWidth = storageData.imageWidth,
            imageHeight = storageData.imageHeight,
            text = storageData.text,
            label = storageData.label,
            isSpecial = storageData.special
        )
    }
}