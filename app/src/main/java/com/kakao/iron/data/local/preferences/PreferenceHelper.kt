package com.kakao.iron.data.local.preferences

import android.content.Context
import android.content.SharedPreferences
import com.kakao.iron.BuildConfig
import com.kakao.iron.ui.search.HistoryData
import java.util.*
import kotlin.collections.ArrayList

class PreferenceHelper(
    context: Context
) {
    private val pref: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val edit: SharedPreferences.Editor = pref.edit()

    fun getAll(): ArrayList<HistoryData> {
        return if (pref.all.isNotEmpty()) {
            val retList = arrayListOf<HistoryData>()
            val savedList = pref.all
            savedList.forEach {
                retList.add(
                    HistoryData(
                        it.key as String,
                        it.value as Long
                    )
                )
            }
            retList.sortWith(
                Comparator { left, right ->
                    left.time.compareTo(right.time) * -1
                }
            )
            retList
        } else arrayListOf()
    }

    fun add(Query: String) {
        val now = System.currentTimeMillis()
        val time = Date(now).time
        edit.putLong(Query, time)
        edit.commit()
    }

    fun remove(Query: String) {
        edit.remove(Query)
        edit.commit()
    }

    fun removeAll() {
        edit.clear()
        edit.commit()
    }

    companion object {
        const val PREF_NAME = BuildConfig.APPLICATION_ID + ".local"
    }
}