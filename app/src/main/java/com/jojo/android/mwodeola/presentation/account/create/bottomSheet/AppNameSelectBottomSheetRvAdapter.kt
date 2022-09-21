package com.jojo.android.mwodeola.presentation.account.create.bottomSheet

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jojo.android.mwodeola.databinding.BottomSheetAppNameSelectRvItemBinding
import com.jojo.android.mwodeola.presentation.account.create.AppInfo
import com.jojo.android.mwodeola.util.alsoForEach
import com.jojo.android.mwodeola.util.indexOfStartEnd

class AppNameSelectBottomSheetRvAdapter(
    private val dialog: AppNameSelectBottomSheet
) : RecyclerView.Adapter<AppNameSelectBottomSheetRvAdapter.ViewHolder>(), Filterable {

    val items = mutableListOf<AppInfo>()
    var itemsForFiltered: List<AppInfo> = emptyList()

    private val appFilter: AppFilter = AppFilter()

    override fun getFilter(): Filter = appFilter

    override fun getItemCount(): Int = itemsForFiltered.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(BottomSheetAppNameSelectRvItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appInfo = itemsForFiltered[position]

        holder.bind(appInfo)
        holder.itemView.setOnClickListener {
            if (appInfo.isExists.not()) {
                dialog.setGroupName(appInfo)
            }
        }
        holder.itemView.setOnTouchListener { v, event ->
            // RecyclerView 영역에서 BottomSheet 의 dismiss by drag 막기
            dialog.binding.root.requestDisallowInterceptTouchEvent(true)
            false
        }
    }

    fun setData(apps: List<AppInfo>) {
        items.clear()
        items.addAll(apps)
    }

    fun filter(text: String) {
        appFilter.filter(text)
    }

    inner class AppFilter : Filter() {

        override fun performFiltering(constraint: CharSequence?): FilterResults =
            FilterResults().also { results ->
                if (constraint.isNullOrBlank()) {
                    results.count = items.size
                    results.values = items

                    items.forEach {
                        it.startIndex = -1
                        it.endIndex = -1
                    }
                } else {
                    items.filter { it.label.contains(constraint, true) }
                        .alsoForEach {
                            it.label.indexOfStartEnd(constraint)?.let { index ->
                                it.startIndex = index[0]
                                it.endIndex = index[1]
                            }
                        }
                        .let {
                            results.count = it.size
                            results.values = it
                        }
                }
            }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            notifyItemRangeRemoved(0, itemsForFiltered.size)

            val filteredList = results?.values
            if (filteredList == null) {
                itemsForFiltered = emptyList()
            } else if (filteredList is List<*>) {
                itemsForFiltered = filteredList as List<AppInfo>
            }

            notifyItemRangeInserted(0, itemsForFiltered.size)
//            notifyDataSetChanged()
        }
    }

    class ViewHolder(binding: BottomSheetAppNameSelectRvItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val enabledColor = Color.rgb(204,204,204)
        val grayScaleColorFilter = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0f)
        })
        val icon: ImageView = binding.icon
        val tvAppName: TextView = binding.tvAppName

        fun bind(appInfo: AppInfo) {
            icon.setImageDrawable(appInfo.icon)

            if (appInfo.startIndex != -1 && appInfo.endIndex != -1) {
                tvAppName.text = SpannableStringBuilder(appInfo.label).apply {
                    setSpan(
                        ForegroundColorSpan(Color.RED),
                        appInfo.startIndex,
                        appInfo.endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            } else {
                tvAppName.text = appInfo.label
            }

            if (appInfo.isExists) {
                icon.colorFilter = grayScaleColorFilter
                icon.alpha = 0.3f
                tvAppName.alpha = 0.3f
            } else {
                icon.clearColorFilter()
                icon.alpha = 1f
                tvAppName.alpha = 1f
            }
        }
    }
}