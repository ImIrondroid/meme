package com.kakao.iron.data.local.room.dao

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.*

class RoomConverter {

    @TypeConverter
    fun fromString(value: String) : List<String> {
        val type = object : TypeToken<List<String>>(){}.type
        return Gson().fromJson(value, type)
    }

    @TypeConverter
    fun fromList(list: List<String>) : String {
        return Gson().toJson(list)
    }
}