package com.jojo.android.mwodeola.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [SearchHistory::class], version = 1, exportSchema = false)
abstract class SearchHistoryDB : RoomDatabase() {
    abstract fun searchHistoryDao(): SearchHistoryDao

    companion object {
        private var INSTANCE: SearchHistoryDB? = null
        private val lock = Any()

        @Synchronized
        fun getInstance(context: Context): SearchHistoryDB =
            INSTANCE ?: synchronized(lock) {
                Room.databaseBuilder(context, SearchHistoryDB::class.java, "SearchHistory")
                    .allowMainThreadQueries()
                    .build()
                    .also { INSTANCE = it }
            }
    }
}