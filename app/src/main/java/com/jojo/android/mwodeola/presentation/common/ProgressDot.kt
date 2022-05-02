package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.graphics.drawable.Animatable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.ViewParent
import android.widget.FrameLayout
import android.widget.LinearLayout

class ProgressDot private constructor(
    private val parent: ViewGroup,
    private val canTouchOutside: Boolean = true
) {

    private val backgroundMask: FrameLayout
    private val dotContainer: LinearLayout

    private val context: Context
        get() = parent.context

    init {
        backgroundMask = FrameLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

        }

        dotContainer = LinearLayout(context).apply {
            layoutParams = ViewGroup.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also {

            }
            gravity = Gravity.CENTER
        }

        if (canTouchOutside) {

        } else {
            val rootView = parent.rootView
            if (rootView is ViewGroup) {
                rootView.addView(backgroundMask, rootView.childCount)
            }
        }
    }


    class Builder(private val parent: ViewGroup) {


        fun build(): ProgressDot {
            return ProgressDot(parent)
        }
    }
}