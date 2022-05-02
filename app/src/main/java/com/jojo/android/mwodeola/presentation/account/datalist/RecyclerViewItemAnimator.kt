package com.jojo.android.mwodeola.presentation.account.datalist

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.animation.AnticipateInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewItemAnimator() : DefaultItemAnimator() {
    companion object {
        private const val TAG = "RecyclerViewItemAnimator"
    }

    init {
        supportsChangeAnimations = true
    }

    override fun animateChange(
        oldHolder: RecyclerView.ViewHolder?,
        newHolder: RecyclerView.ViewHolder?,
        fromX: Int,
        fromY: Int,
        toX: Int,
        toY: Int
    ): Boolean {
//        Log.d(TAG, "animateChange(): oldHolder == newHolder: ${oldHolder == newHolder}")
//        Log.d(TAG, "animateChange(): adapterPosition=(old=${oldHolder?.adapterPosition}, new=${newHolder?.adapterPosition})")

        if (oldHolder == null || newHolder == null || oldHolder == newHolder)
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)

        Log.d(TAG, "newHolder.itemView=${newHolder.itemView}")

        animateFadeOutAlpha(newHolder)

        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
        // Log.w(TAG, "animateAdd(): adapterPosition=${holder?.adapterPosition}")
        if (holder == null)
            return super.animateAdd(holder)

//        animateFadeOutAlpha(holder)
        return super.animateAdd(holder)
    }

    private fun animateFadeOutAlpha(viewHolder: RecyclerView.ViewHolder) {
        if (viewHolder is AccountGroupListAdapter.HeaderViewHolder)
            return

        val itemView = viewHolder.itemView as? ViewGroup
            ?: return

        val highlightView = View(viewHolder.itemView.context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            setBackgroundColor(Color.BLUE)
            alpha = 0.2f
            tag = "highlight_view"
        }

        itemView.addView(highlightView, 0)

        highlightView.animate().setInterpolator(AnticipateInterpolator())
            .setDuration(1000)
            .alpha(0f)
            .withEndAction {
                Log.e(TAG, "highlightView animation end!!")
                val view = itemView.findViewWithTag<View>("highlight_view")
                itemView.removeView(view)
            }
    }
}