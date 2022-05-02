package com.jojo.android.mwodeola.presentation.account.detail

import android.util.Log
import android.view.View
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs
import kotlin.math.max

class ZoomOutPageTransformer(
    private val viewPager2: ViewPager2
) : DefaultItemAnimator(), ViewPager2.PageTransformer {

    companion object {
        private const val TAG = "ZoomOutPageTransformer"
        private const val MIN_SCALE = 0.88f
        private const val MIN_ALPHA = 0.5f
    }

    private val recyclerView: RecyclerView
        get() = viewPager2.getChildAt(0) as RecyclerView

    override fun onAddFinished(item: RecyclerView.ViewHolder?) {
        //Log.w(TAG, "onRemoveFinished(): $item")

        refreshTransformPage()
    }

    override fun onRemoveFinished(item: RecyclerView.ViewHolder?) {
        //Log.w(TAG, "onRemoveFinished(): $item")

        refreshTransformPage()
    }

    override fun transformPage(page: View, position: Float): Unit = when {
        position < -1 -> { // (-Infinity,-1)
            // This page is way off-screen to the left.
            // Log.i(TAG, "transformPage(): $page, position=$position")
            // page.alpha = 0f
            page.scaleX = MIN_SCALE
            page.scaleY = MIN_SCALE
            Unit
        }
        position <= 1 -> { // [-1,1]
            // Modify the default slide transition to shrink the page as well
            // Log.d(TAG, "transformPage(): $page, position=$position")
            val scaleFactor = max(MIN_SCALE, 1 - abs(position))
            val vertMargin = page.height * (1 - scaleFactor) / 2
            val horzMargin = page.width * (1 - scaleFactor) / 2

//            page.translationX =
//                if (position < 0) horzMargin - vertMargin / 2
//                else horzMargin + vertMargin / 2

            // Scale the page down (between MIN_SCALE and 1)
            page.scaleX = scaleFactor
            page.scaleY = scaleFactor

            // Fade the page relative to its size.
            page.alpha = MIN_ALPHA + (scaleFactor - MIN_SCALE) / (1 - MIN_SCALE) * (1 - MIN_ALPHA)
            Unit
        }
        else -> { // (1,+Infinity)
            // This page is way off-screen to the right.
            // Log.e(TAG, "transformPage(): $page, position=$position")
            // page.alpha = 0f
            page.scaleX = MIN_SCALE
            page.scaleY = MIN_SCALE
            Unit
        }
    }

    private fun refreshTransformPage() {
        val currentPosition = viewPager2.currentItem
        refreshTransformPage(currentPosition - 2, -2f)
        refreshTransformPage(currentPosition - 1, -1f)
        refreshTransformPage(currentPosition + 0, 0f)
        refreshTransformPage(currentPosition + 1, 1f)
        refreshTransformPage(currentPosition + 2, 2f)
    }

    private fun refreshTransformPage(adapterPosition: Int, transformPosition: Float) {
        recyclerView.findViewHolderForAdapterPosition(adapterPosition)?.let {
            transformPage(it.itemView, transformPosition)
        }
    }
}