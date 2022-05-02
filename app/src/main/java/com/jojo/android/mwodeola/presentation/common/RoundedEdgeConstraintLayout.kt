package com.jojo.android.mwodeola.presentation.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import com.jojo.android.mwodeola.R

class RoundedEdgeConstraintLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    companion object {
        const val TAG = "RoundedEdgeConstraintLayout"

        const val ROUNDED_NONE = 0
        const val ROUNDED_TOP = 1
        const val ROUNDED_BOTTOM = 2
        const val ROUNDED_BOTH_TOP_AND_BOTTOM = 3

        private val BOX_CORNER_RADII_TOP_DEFAULT = FloatArray(8) { if (it < 4) 50f else 0f }
        private val BOX_CORNER_RADII_BOTTOM_DEFAULT = FloatArray(8) { if (it < 4) 0f else 50f }
        private val BOX_CORNER_RADII_TOP_AND_BOTTOM_DEFAULT = FloatArray(8) { 50f }

        private val BOX_BG_NONE = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
        }

        private val BOX_BG_TOP = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = BOX_CORNER_RADII_TOP_DEFAULT
            setColor(Color.WHITE)
        }

        private val BOX_BG_BOTTOM = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = BOX_CORNER_RADII_BOTTOM_DEFAULT
            setColor(Color.WHITE)
        }

        private val BOX_BG_BOTH_TOP_AND_BOTTOM = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadii = BOX_CORNER_RADII_TOP_AND_BOTTOM_DEFAULT
//            cornerRadius = 50f
            setColor(Color.WHITE)
        }

        fun getRoundedConstantString(constant: Int) = when (constant) {
            ROUNDED_NONE -> "ROUNDED_NONE"
            ROUNDED_TOP -> "ROUNDED_TOP"
            ROUNDED_BOTTOM -> "ROUNDED_BOTTOM"
            ROUNDED_BOTH_TOP_AND_BOTTOM -> "ROUNDED_BOTH_TOP_AND_BOTTOM"
            else -> "Unknown"
        }
    }

    private var _shape: Int = ROUNDED_NONE
    var shape: Int
        get() = _shape
        set(value) { setShape(value) }

    private var separatorViewId: Int = NO_ID
    var separatorView: View? = null

    init {
//        val outValue = TypedValue()
//        context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
//        this.setBackgroundResource(outValue.resourceId)

        context.obtainStyledAttributes(attrs, R.styleable.RoundedEdgeConstraintLayout, defStyleAttr, defStyleRes).let {
            val boxBackgroundColor =
                it.getColor(R.styleable.RoundedEdgeConstraintLayout_boxBackgroundColor, Color.WHITE)
            separatorViewId = it.getResourceId(R.styleable.RoundedEdgeConstraintLayout_separator_id, NO_ID)

//            this.setBackgroundColor(boxBackgroundColor)
            this.background = BOX_BG_NONE

            it.recycle()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        if (separatorViewId != NO_ID) {
            traverseChildViewNode()
                .find { it.id == separatorViewId }
                ?.let { separatorView = it }
        }

        showOrHideSeparator()
    }

    @SuppressLint("DrawAllocation")
    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        val radius = height / 2f

        BOX_BG_TOP.cornerRadii = FloatArray(8) { if (it < 4) radius else 0f }
        BOX_BG_BOTTOM.cornerRadii = FloatArray(8) { if (it < 4) 0f else radius }
        BOX_BG_BOTH_TOP_AND_BOTTOM.cornerRadii = FloatArray(8) { radius * 2 }
    }

    /** 단위: DP */
    fun setCornerRadius(radius: Float) {
        val dp = radius.dpToPixels(context)
        BOX_BG_TOP.cornerRadii = FloatArray(8) { if (it < 4) dp else 0f }
        BOX_BG_BOTTOM.cornerRadii = FloatArray(8) { if (it < 4) 0f else dp }
        BOX_BG_BOTH_TOP_AND_BOTTOM.cornerRadii = FloatArray(8) { dp }
    }

    @JvmName("setShape1")
    private fun setShape(type: Int) {
        if (type in ROUNDED_NONE..ROUNDED_BOTH_TOP_AND_BOTTOM) {
            _shape = type
            background = when (type) {
                ROUNDED_NONE -> BOX_BG_NONE
                ROUNDED_TOP -> BOX_BG_TOP
                ROUNDED_BOTTOM -> BOX_BG_BOTTOM
                ROUNDED_BOTH_TOP_AND_BOTTOM -> BOX_BG_BOTH_TOP_AND_BOTTOM
                else -> BOX_BG_NONE
            }
            showOrHideSeparator()
        }
    }

    private fun showOrHideSeparator() {
        separatorView?.visibility =
            if (_shape == ROUNDED_BOTTOM || _shape == ROUNDED_BOTH_TOP_AND_BOTTOM) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun Float.dpToPixels(context: Context): Float =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
}

private fun ViewGroup.traverseChildViewNode(): List<View> = arrayListOf<View>().also {
    children.forEach { child ->
        it.add(child)
        if (child is ViewGroup) {
            it.addAll(child.traverseChildViewNode())
        }
    }
}