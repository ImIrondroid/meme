package com.kakao.iron.data.local.room.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Entity(tableName = "memeEntity")
@Parcelize
data class MemeEntity(
    @PrimaryKey(autoGenerate = true)
    val id : Long = 0L,
    val form: Int = 0,
    val isSpecial: Boolean = false,
    val query: String = "",
    val collection : String = "",
    val filePath : String = "",
    val imageUrl: String = "",
    val thumbnailUrl: String = "",
    val imageWidth: Int = 0,
    val imageHeight: Int = 0,
    val text: List<String> = listOf(),
    val label: List<String> = listOf()
) : Parcelable
