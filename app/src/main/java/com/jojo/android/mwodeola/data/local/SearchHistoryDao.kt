package com.jojo.android.mwodeola.data.local

import androidx.room.*

@Dao
interface SearchHistoryDao {

    @Query("SELECT * FROM SearchHistory")
    fun getAll(): List<SearchHistory>

    @Query("SELECT * FROM SearchHistory WHERE type=:type")
    fun getAllBy(type: Int): List<SearchHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(data: SearchHistory): Long

    @Delete
    fun delete(data: SearchHistory)

    @Query("DELETE FROM SearchHistory WHERE type=:type")
    fun deleteAll(type: Int)

    @Query("DELETE FROM SearchHistory")
    fun deleteAll()
}