package com.jojo.android.mwodeola.presentation.account.datalist.search

import android.widget.Filter
import com.jojo.android.mwodeola.util.Log2

abstract class Filter2<T> : Filter() {

    protected abstract fun performFiltering2(constraint: CharSequence?): List<T>
    protected abstract fun publishResults2(constraint: CharSequence?, results: List<T>)

    final override fun performFiltering(constraint: CharSequence?): FilterResults =
        FilterResults().apply {
            val newList = performFiltering2(constraint)
            count = newList.size
            values = newList
        }

    final override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        if (results == null) {
            publishResults2(constraint, emptyList())
        } else {
            publishResults2(constraint, results.values as List<T>)
        }
    }
}