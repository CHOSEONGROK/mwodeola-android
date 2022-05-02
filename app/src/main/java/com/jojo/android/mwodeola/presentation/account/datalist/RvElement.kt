package com.jojo.android.mwodeola.presentation.account.datalist

import android.graphics.drawable.Drawable
import androidx.recyclerview.widget.DiffUtil
import com.jojo.android.mwodeola.data.account.AccountGroup

data class RvElement constructor(
    val header: Header? = null,
    var item: AccountGroup? = null
) {
    val isHeader: Boolean
        get() = (header != null)
    val isItem: Boolean
        get() = (item != null)

    var icon: Drawable? = null

    data class Header(val type: Int, val title: String)

    companion object {
        const val HEADER_SPECIAL = 0
        const val HEADER_NORMAL = 1

        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<RvElement>() {
            override fun areItemsTheSame(oldItem: RvElement, newItem: RvElement): Boolean =
                oldItem === newItem

            override fun areContentsTheSame(oldItem: RvElement, newItem: RvElement): Boolean =
                oldItem == newItem
        }
    }
}