package com.kakao.iron.data.local.room.db

import android.content.Context
import androidx.room.*
import com.kakao.iron.data.local.room.entity.MemeEntity
import com.kakao.iron.data.local.room.dao.MemeDao
import com.kakao.iron.data.local.room.dao.RoomConverter

@Database(
    entities = [MemeEntity::class],
    version = 1,
    exportSchema = false
)

@TypeConverters(RoomConverter::class)
abstract class MemeDatabase : RoomDatabase() {

    abstract fun imageDao(): MemeDao

    companion object {
        private var INSTANCE: MemeDatabase? = null
        fun getInstance(context: Context): MemeDatabase? {
            if (INSTANCE == null) {
                synchronized(MemeDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        MemeDatabase::class.java,
                        "memeWorld.db"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                }
            }
            return INSTANCE
        }
    }
}