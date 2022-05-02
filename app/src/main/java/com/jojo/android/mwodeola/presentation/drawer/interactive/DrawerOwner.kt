package com.jojo.android.mwodeola.presentation.drawer.interactive

import android.content.Intent
import android.view.View
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.constraintlayout.utils.widget.ImageFilterView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jojo.android.mwodeola.presentation.common.IncubatableFloatingActionButton

interface DrawerOwner {

    interface OnColorThemeTransitionListener {

        fun onTransitionStarted(isDarkTheme: Boolean, backgroundColor: Int)
        fun onTransitionCompleted(isDarkTheme: Boolean, backgroundColor: Int)

        /**
         * progress: 0 -> Dark Theme
         * progress: 1 -> Light Theme
         * */
        fun onTransitionChanged(progress: Float, backgroundColor: Int)

        /**
         * Called only once when theme changed(Dark -> Light or Light Dark)
         * */
        fun onThemeChanged(isDarkTheme: Boolean)
    }

    interface OnSharedWidgetsListener {
        fun onDrawerOpened(drawerView: View)
        fun onDrawerClosed(drawerView: View)

        fun onDeleteClicked(view: View)
        fun onSearchClicked(view: View)
        fun onFilterClicked(view: ImageFilterView)
    }

    var title: String
    var subtitle: String

    val swipeRefreshLayout: SwipeRefreshLayout
    val checkBoxSelectAllContainer: FrameLayout
    val checkBoxSelectAll: CheckBox
    val incubatableFab: IncubatableFloatingActionButton

    fun startSelectionMode()
    fun cancelSelectionMode()

    fun launchActivityForResult(cls: Class<*>, intent: Intent?)
}