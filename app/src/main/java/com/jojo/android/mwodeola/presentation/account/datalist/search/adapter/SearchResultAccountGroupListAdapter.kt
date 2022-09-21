package com.jojo.android.mwodeola.presentation.account.datalist.search.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.divider.MaterialDivider
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.ActivitySearchResultAccountGroupListItemBinding
import com.jojo.android.mwodeola.presentation.account.datalist.search.Filter2
import com.jojo.android.mwodeola.presentation.account.datalist.search.FuzzyMatcher
import com.jojo.android.mwodeola.presentation.account.datalist.search.SearchAccountContract
import com.jojo.android.mwodeola.presentation.common.IconView
import com.jojo.android.mwodeola.util.Log2

@SuppressLint("NotifyDataSetChanged")
class SearchResultAccountGroupListAdapter(
    private val view: SearchAccountContract.View
) : ListAdapter<AccountGroupWrapper, SearchResultAccountGroupListAdapter.ViewHolder>(AccountGroupWrapper.DIFF_CALLBACK), Filterable {

    private val searchFilter: Filter2<AccountGroupWrapper> = SearchFilter()

    private val originList = mutableListOf<AccountGroupWrapper>()

    override fun getFilter(): Filter = searchFilter

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder.newInstance(parent)

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else if (payloads[0] == AccountGroupWrapper.PAYLOAD_CHANGED) {
            holder.changeSpan(currentList[position])
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = currentList[position]
        holder.bind(item, (position == itemCount - 1))
        holder.itemView.setOnClickListener {
            view.startAccountDetailActivity(item.group.id)
        }
    }

    fun setAccountGroups(context: Context, groups: List<AccountGroup>) {
        originList.clear()
        originList.addAll(
            groups.map {
                AccountGroupWrapper(it).apply { initIcon(context) }
            }
        )
    }

    fun filter(keyword: String) {
        filter.filter(keyword)
    }

    inner class SearchFilter : Filter2<AccountGroupWrapper>() {
        override fun performFiltering2(constraint: CharSequence?): List<AccountGroupWrapper> =
            FuzzyMatcher.matchBy(originList, constraint.toString())
                .map { it.clone() }

        override fun publishResults2(constraint: CharSequence?, results: List<AccountGroupWrapper>) {
            Log2.d("constraint=$constraint, results.size=${results.size}")
            if (results.isEmpty()) {
                submitList(null) {
                    notifyDataSetChanged()
                }
            } else {
                submitList(results)
            }
            view.updateVisibleNoneSearchResultLabel(results.isEmpty())
        }

        private fun postDelayed(runnable: Runnable) {
            Handler(Looper.getMainLooper()).postDelayed(runnable, 10)
        }
    }

    class ViewHolder(binding: ActivitySearchResultAccountGroupListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val icon: IconView = binding.icon
        val snsMark: TextView = binding.snsMark
        val tvAccountName: TextView = binding.tvAccountName
        val bottomDivider: MaterialDivider = binding.divider

        fun bind(item: AccountGroupWrapper, isBottom: Boolean) {
            item.applyIcon(icon)
            snsMark.isVisible = item.group.isSnsGroup

            tvAccountName.text = item.groupNameSpannable

            bottomDivider.isVisible = isBottom.not()
        }

        fun changeSpan(item: AccountGroupWrapper) {
            tvAccountName.text = item.groupNameSpannable
        }

        companion object {
            fun newInstance(parent: ViewGroup) = ViewHolder(
                ActivitySearchResultAccountGroupListItemBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
            )
        }
    }
}