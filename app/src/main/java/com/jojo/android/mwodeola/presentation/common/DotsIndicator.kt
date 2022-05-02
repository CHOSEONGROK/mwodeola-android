package com.jojo.android.mwodeola.presentation.common

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.setMargins
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.R

class DotsIndicator @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayoutCompat(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "DotsIndicator"
    }

    private var viewPager2: ViewPager2? = null
    private val dotViews = mutableListOf<DotView>()

    private val viewPagerCallback = ViewPagerCallback()
    private val dataObserver = ViewPagerObserver()

    private val dotsSizeDefault = 10.dpToPixels()
    private val dotsMarginsDefault = 4.dpToPixels()
    private val dotsDefaultColorDefault = Color.LTGRAY
    private val dotsSelectedColorDefault = Color.BLUE
    private val dotsElevationDefault = 0f

    private val dotsSize: Int
    private val dotsMargins: Int
    private val dotsDefaultColor: Int
    private val dotsSelectedColor: Int
    private val dotsElevation: Float

    init {
        context.obtainStyledAttributes(attrs, R.styleable.DotsIndicator).let {
            dotsSize = it.getDimensionPixelSize(R.styleable.DotsIndicator_indicator_dotsSize, dotsSizeDefault)
            dotsMargins = it.getDimensionPixelSize(R.styleable.DotsIndicator_indicator_dotsMargins, dotsMarginsDefault)
            dotsDefaultColor = it.getColor(R.styleable.DotsIndicator_indicator_dotsDefaultColor, dotsDefaultColorDefault)
            dotsSelectedColor = it.getColor(R.styleable.DotsIndicator_indicator_dotsSelectedColor, dotsSelectedColorDefault)
            dotsElevation = it.getDimension(R.styleable.DotsIndicator_indicator_dotsElevation, dotsElevationDefault)

            it.recycle()
        }

        layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
        gravity = Gravity.CENTER
        orientation = HORIZONTAL
        overScrollMode = View.OVER_SCROLL_NEVER

        for (i in 0 until 5) {
            val dotView = createDotView()
            dotViews.add(dotView)
            addView(dotView)

            if (i == 4) {
                dotView.setCustomIcon(R.drawable.plus_icon_small)
            }
        }
    }

    fun setViewPager2(viewPager2: ViewPager2) {
        checkAdapter(viewPager2).let {
            this.viewPager2 = viewPager2
            it.registerAdapterDataObserver(dataObserver)

            dotViews.clear()
            removeAllViews()

            for (i in 0 until it.itemCount) {
                val dotView = createDotView()
                dotViews.add(dotView)
                addView(dotView)
            }

            if (dotViews.size > 0) {
                dotViews[viewPager2.currentItem].isDotSelected = true
            }
        }

        viewPager2.registerOnPageChangeCallback(viewPagerCallback)
    }

    fun setCustomIcon(position: Int, @DrawableRes resId: Int) {
        dotViews.getOrNull(position)?.setCustomIcon(resId)
    }

    fun selectDot(position: Int) {
        dotViews.forEachIndexed { index, dotView ->
            dotView.isDotSelected = (index == position)
        }
    }

    private fun checkAdapter(viewPager2: ViewPager2): RecyclerView.Adapter<in RecyclerView.ViewHolder> {
        return if (viewPager2.adapter == null)
            throw IllegalStateException("You have to set an adapter to the view pager before " +
                        "initializing the dots indicator !")
        else viewPager2.adapter!!
    }

    private fun createDotView(): DotView = DotView(
        context,
        dotSize = dotsSize,
        dotMargins = dotsMargins,
        dotDefaultColor = dotsDefaultColor,
        dotSelectedColor = dotsSelectedColor,
        dotElevation = dotsElevation
    )

    private fun Int.dpToPixels(): Int =
        (this * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

    inner class ViewPagerCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            Log.d(TAG, "onPageSelected(): position=$position")
            selectDot(position)
        }
    }

    inner class ViewPagerObserver : RecyclerView.AdapterDataObserver() {
        override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
            Log.w(TAG, "onItemRangeInserted(): positionStart=$positionStart, itemCount=$itemCount")
            if (positionStart > dotViews.size)
                return

            for (i in 0 until itemCount) {
                val dotView = createDotView()
                dotViews.add(positionStart, dotView)
                addView(dotView, positionStart)
            }

            viewPager2?.currentItem?.let { selectDot(it) }
        }

        override fun onItemRangeRemoved(positionStart: Int, itemCount: Int) {
            Log.e(TAG, "onItemRangeRemoved(): positionStart=$positionStart, itemCount=$itemCount")
            if (dotViews.isEmpty())
                return

            for (index in positionStart + itemCount - 1 downTo positionStart) {
                dotViews.removeAt(index)
                removeViewAt(index)
            }

            viewPager2?.currentItem?.let { selectDot(it) }
        }
    }

    class DotView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0,
        private val dotSize: Int = 0,
        private val dotMargins: Int = 0,
        private val dotDefaultColor: Int = 0,
        private val dotSelectedColor: Int = 0,
        private val dotElevation: Float = 0f,
    ) : MaterialCardView(context, attrs, defStyleAttr) {

        companion object {
            private const val DURATION = 300L
            private val INTERPOLATOR = AccelerateDecelerateInterpolator()
        }

        private var _isDotSelected = false
        var isDotSelected: Boolean
            get() = _isDotSelected
            set(value) { select(value) }

        private var isCustomIconView = false

        private var customIconView: AppCompatImageView? = null

        init {
            layoutParams = LinearLayoutCompat.LayoutParams(dotSize, dotSize).also {
                it.setMargins(dotMargins)
            }

            radius = dotSize / 2f
            cardElevation = dotElevation
            setCardBackgroundColor(dotDefaultColor)
        }

        fun setCustomIcon(@DrawableRes resId: Int) {
            val drawable = ResourcesCompat.getDrawable(resources, resId, null)
                ?: return

            isCustomIconView = true

            layoutParams = LinearLayoutCompat.LayoutParams(dotSize, dotSize).also {
                it.setMargins(dotMargins)
            }

            customIconView = AppCompatImageView(context).also {
                it.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                it.setImageDrawable(drawable)

                val backgroundColor =
                    if (isDotSelected) dotSelectedColor
                    else dotDefaultColor
                it.imageTintList = ColorStateList.valueOf(backgroundColor)
            }
            addView(customIconView)

            radius = 0f
            cardElevation = 0f
            setCardBackgroundColor(Color.TRANSPARENT)
        }

        fun removeCustomIcon() {
            isCustomIconView = false

            removeView(customIconView)
            customIconView = null

            layoutParams = LinearLayoutCompat.LayoutParams(dotSize, dotSize).also {
                it.setMargins(dotMargins)
            }

            radius = dotSize / 2f
            cardElevation = dotElevation

            val backgroundColor =
                if (isDotSelected) dotSelectedColor
                else dotDefaultColor
            setCardBackgroundColor(backgroundColor)
        }

        private fun select(isSelect: Boolean) {
            if (_isDotSelected == isSelect)
                return

            _isDotSelected = isSelect

            val fromColor =
                if (isSelect) dotDefaultColor
                else dotSelectedColor
            val toColor =
                if (isSelect) dotSelectedColor
                else dotDefaultColor

            ValueAnimator.ofArgb(fromColor, toColor).apply {
                interpolator = INTERPOLATOR
                duration = DURATION
                addUpdateListener {
                    if (isCustomIconView) {
                        // Custom Icon View
                        customIconView?.imageTintList = ColorStateList.valueOf(it.animatedValue as Int)
                    } else {
                        // Default Dot View
                        setCardBackgroundColor(it.animatedValue as Int)
                    }

                }
            }.start()
        }
    }
}