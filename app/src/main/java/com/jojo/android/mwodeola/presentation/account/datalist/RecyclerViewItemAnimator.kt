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
        if (oldHolder == null || newHolder == null || oldHolder == newHolder)
            return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)

        animateFadeOutAlpha(newHolder)

        return super.animateChange(oldHolder, newHolder, fromX, fromY, toX, toY)
    }

    override fun animateAdd(holder: RecyclerView.ViewHolder?): Boolean {
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
                val view = itemView.findViewWithTag<View>("highlight_view")
                itemView.removeView(view)
            }
    }
}