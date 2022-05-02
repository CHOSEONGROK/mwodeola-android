package com.jojo.android.mwodeola.presentation.drawer.motion

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.children
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.appbar.AppBarLayout

class RefreshableCoordinatorLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : CoordinatorLayout(context, attrs, defStyleAttr),
    SwipeRefreshLayout.OnChildScrollUpCallback, AppBarLayout.OnOffsetChangedListener {

    private val tag = "RefreshableCoordinatorLayout"

    var isRefreshEnabled = true

    private var appBarLayoutVerticalOffset = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? SwipeRefreshLayout)?.setOnChildScrollUpCallback(this)

        (children.find { it is AppBarLayout } as? AppBarLayout)
            ?.addOnOffsetChangedListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (children.find { it is AppBarLayout } as? AppBarLayout)
            ?.removeOnOffsetChangedListener(this)
    }

    override fun canChildScrollUp(parent: SwipeRefreshLayout, child: View?): Boolean =
        if (isRefreshEnabled) appBarLayoutVerticalOffset < 0
        else true

    override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
        appBarLayoutVerticalOffset = verticalOffset
    }
}