package com.jojo.android.mwodeola.presentation.drawer.motion

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.motion.widget.MotionLayout
import com.google.android.material.appbar.AppBarLayout

class CollapsingToolbarMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : MotionLayout(context, attrs, defStyleAttr), AppBarLayout.OnOffsetChangedListener {

    val appBarLayoutVerticalOffset: Int
        get() = verticalOffset

    private var verticalOffset = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? AppBarLayout)?.addOnOffsetChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (parent as? AppBarLayout)?.removeOnOffsetChangedListener(this)
    }

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        this.verticalOffset = verticalOffset

        progress = -verticalOffset / appBarLayout?.totalScrollRange?.toFloat()!!
    }
}