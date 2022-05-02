package com.jojo.android.mwodeola.data.local

import androidx.recyclerview.widget.DiffUtil
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity
data class SearchHistory(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val value: String,
    val type: Int,
    val dateTime: Long,
) : Comparable<SearchHistory> {

    @Ignore
    val dateFormatFull: String = SimpleDateFormat("yyyy년 MM월 dd일 HH:mm:ss", Locale.KOREAN).format(dateTime)
    @Ignore
    val dateFormatLarge: String = SimpleDateFormat("yyyy년 MM월 dd일", Locale.KOREAN).format(dateTime)
    @Ignore
    val dateFormatSmall: String = SimpleDateFormat("M월 d일", Locale.KOREAN).format(dateTime)

    override fun compareTo(other: SearchHistory): Int =
        (other.dateTime - dateTime).toInt()

    fun toStringFull(index: Int): String = "SearchHistory[$index]: $id, $value, $dateFormatFull"
    fun toStringLarge(index: Int): String = "SearchHistory[$index]: $id, $value, $dateFormatLarge"
    fun toStringSmall(index: Int): String = "SearchHistory[$index]: $id, $value, $dateFormatSmall"

    companion object {
        const val NONE = 0
        const val ACCOUNT = 1
        const val CREDIT_CARD = 2

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<SearchHistory>() {
            override fun areItemsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean =
                oldItem.id == newItem.id

            override fun areContentsTheSame(oldItem: SearchHistory, newItem: SearchHistory): Boolean =
                oldItem == newItem
        }

        fun newInstanceForAccount(id: Long = 0, value: String): SearchHistory =
            SearchHistory(id, value, ACCOUNT, System.currentTimeMillis())
    }
}