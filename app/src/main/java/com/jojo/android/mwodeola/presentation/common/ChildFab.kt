package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.util.TypedValue
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jojo.android.mwodeola.util.dpToPixels

object ChildFab {

    fun parent(fab: IncubatableFloatingActionButton): ChildFabBuilder {
        return ChildFabBuilder(fab)
    }

    fun delete(parent: IncubatableFloatingActionButton, position: Int) {
        parent.removeChildAt(position)
    }

    fun delete(parent: IncubatableFloatingActionButton, label: String) {
        parent.removeChildWith(label)
    }

    class ChildFabBuilder(private val parentFab: IncubatableFloatingActionButton) {
        companion object {
            const val TAG = "child_fab"
        }

        private val childFab = FloatingActionButton(parentFab.context).apply {
            id = hashCode()
            tag = TAG

            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { params ->
                params.topToTop = parentFab.parentFabMaskId
                params.startToStart = parentFab.parentFabMaskId
                params.endToEnd = parentFab.parentFabMaskId
            }
            customSize = 40.dpToPixels(context)

            val typedValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.colorPrimary, typedValue, true)
            val colorPrimary = typedValue.data

            imageTintList = ColorStateList.valueOf(colorPrimary)
            backgroundTintList = ColorStateList.valueOf(Color.WHITE)
        }

        private val label = TextView(parentFab.context).apply {
            layoutParams = ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT).also { params ->
                params.topToTop = childFab.id
                params.bottomToBottom = childFab.id
//                params.startToEnd = childFab.id
//                params.marginStart = 12.dpToPixels(context)
                params.endToStart = childFab.id
                params.marginEnd = 12.dpToPixels(context)
            }
//            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.BLACK)
            text = ""
        }

        private val context: Context
            get() = parentFab.context

        fun icon(@DrawableRes resId: Int) = apply {
            childFab.setImageResource(resId)
        }

        fun icon(drawable: Drawable?) = apply {
            childFab.setImageDrawable(drawable)
        }

        fun iconColor(@ColorRes resId: Int) {
            val color = ResourcesCompat.getColor(context.resources, resId, null)
            childFab.imageTintList = ColorStateList.valueOf(color)
        }

        fun label(text: String) = apply {
            label.text = text
        }

        fun build() {
            parentFab.addChild(childFab, label)
        }
    }
}
