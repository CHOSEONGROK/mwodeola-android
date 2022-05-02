package com.jojo.android.mwodeola.presentation

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class ShimmerRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var isShimmerStarted = false
    abstract val shimmerViewCount: Int

    final override fun getItemCount(): Int =
        if (isShimmerStarted) shimmerViewCount
        else getItemCount2()

    final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (isShimmerStarted) onCreateShimmerViewHolder(parent, viewType)
        else onCreateViewHolder2(parent, viewType)

    final override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder2(holder, position)
    }

    abstract fun getItemCount2(): Int
    abstract fun onCreateViewHolder2(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun onCreateShimmerViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
    abstract fun onBindViewHolder2(holder: RecyclerView.ViewHolder, position: Int)

    fun showShimmer() {
        isShimmerStarted = true
        val itemCount = getItemCount2()

        if (itemCount == 0) {
            notifyItemRangeInserted(0, shimmerViewCount)
        } else if (itemCount < shimmerViewCount) {
            notifyItemRangeChanged(0, itemCount)
            notifyItemRangeInserted(itemCount, shimmerViewCount - itemCount)
        } else if (itemCount == shimmerViewCount) {
            notifyItemRangeChanged(0, shimmerViewCount)
        } else if (itemCount > shimmerViewCount) {
            notifyItemRangeChanged(0, shimmerViewCount)
            notifyItemRangeRemoved(shimmerViewCount, itemCount - shimmerViewCount)
        }
    }

    fun hideShimmer() {
        isShimmerStarted = false
    }


    class ShimmerViewHolder(view: View) : RecyclerView.ViewHolder(view)
}