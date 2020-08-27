package com.kakao.iron.data.local.room.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.kakao.iron.data.local.room.entity.MemeEntity

@Dao
interface MemeDao {

    @Query("SELECT * FROM memeEntity ORDER BY `id` DESC")
    fun getDescendingStorageList(): List<MemeEntity>

    @Query("SELECT * FROM memeEntity ORDER BY `query`")
    fun getQuerySortedStorageList(): List<MemeEntity>

    @Query("SELECT * FROM memeEntity WHERE isSpecial == 1 ORDER BY `id` DESC")
    fun getSpecialListWithPaging(): PagingSource<Int, MemeEntity>

    @Query("SELECT id FROM memeEntity WHERE `query` =:query AND thumbnailUrl =:thumbnailUrl AND imageUrl =:imageUrl")
    fun getSelectedImage(query: String, thumbnailUrl: String, imageUrl: String): Long?

    @Query("SELECT id FROM memeEntity WHERE text =:text AND label =:label")
    fun getSelectedImage(text: String, label: String): Long?

    @Query("SELECT id FROM memeEntity WHERE `filePath` =:filePath")
    fun getSelectedImage(filePath: String): Long

    @Query("SELECT label FROM memeEntity")
    fun getAllLabelList(): List<String>

    @Query("SELECT * FROM memeEntity WHERE label LIKE '%' || :query || '%' ORDER BY `id` DESC") //
    fun getSelectedQueryLabelList(query: String): List<MemeEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertEntity(memeEntity: MemeEntity)

    @Update(onConflict = OnConflictStrategy.ABORT)
    fun updateEntity(memeEntity: MemeEntity)

    @Delete
    fun deleteEntity(memeEntity: MemeEntity)
}