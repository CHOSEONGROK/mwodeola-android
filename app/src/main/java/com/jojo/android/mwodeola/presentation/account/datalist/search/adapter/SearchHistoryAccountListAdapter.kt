package com.jojo.android.mwodeola.presentation.account.datalist.search.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jojo.android.mwodeola.data.local.SearchHistory
import com.jojo.android.mwodeola.databinding.SearchHistoryListItemBinding
import com.jojo.android.mwodeola.presentation.account.datalist.search.SearchAccountContract
import com.jojo.android.mwodeola.util.Log2

@SuppressLint("NotifyDataSetChanged")
class SearchHistoryAccountListAdapter(
    private val view: SearchAccountContract.View,
    private val presenter: SearchAccountContract.Presenter
) : ListAdapter<SearchHistory, SearchHistoryAccountListAdapter.ViewHolder>(SearchHistory.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.newInstance(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val history = currentList[position]

        holder.bind(history)

        holder.setOnItemViewClickListener { _ ->
            view.performClickSearchHistoryItem(history)

            presenter.deleteSearchHistory(history)
            val newHistory = presenter.createNewSearchHistory(history.value)
            removeAndAdd(history, newHistory)
        }

        holder.setOnDeleteButtonClickListener {
            presenter.deleteSearchHistory(history)
            removeHistory(history)
        }
    }

    fun addHistory(history: SearchHistory) {
        val newList = currentList.toMutableList().apply {
            add(history)
            sortByDescending { it.dateTime }
        }
        submitList(newList)
    }

    fun removeHistory(history: SearchHistory) {
        val newList = currentList.toMutableList().apply {
            remove(history)
        }
        submitList(newList)
    }

    fun removeAndAdd(removed: SearchHistory?, added: SearchHistory) {
        val newList = currentList.toMutableList().apply {
            remove(removed)
            add(added)
            sortByDescending { it.dateTime }
        }
        submitList(newList)
    }

    fun removeAllHistories() {
        submitList(null) {
            notifyDataSetChanged()
        }
    }

    class ViewHolder(binding: SearchHistoryListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        private val tvHistory: TextView = binding.tvHistory
        private val tvDateTime: TextView = binding.tvDateTime
        private val btnDelete: ImageView = binding.btnDelete

        fun bind(history: SearchHistory) {
            tvHistory.text = history.value
            tvDateTime.text = history.dateFormatSmall
        }

        fun setOnItemViewClickListener(listener: View.OnClickListener) {
            itemView.setOnClickListener(listener)
        }

        fun setOnDeleteButtonClickListener(listener: View.OnClickListener) {
            btnDelete.setOnClickListener(listener)
        }

        companion object {
            fun newInstance(parent: ViewGroup): ViewHolder = ViewHolder(
                SearchHistoryListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }
}