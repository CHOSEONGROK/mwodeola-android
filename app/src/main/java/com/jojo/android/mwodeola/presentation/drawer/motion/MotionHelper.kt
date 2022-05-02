package com.jojo.android.mwodeola.presentation.drawer.motion

import android.graphics.Color
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.animation.ArgbEvaluatorCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.presentation.drawer.DrawerContract

class MotionHelper constructor(
    private val view: DrawerContract.View,
    private val drawerLayout: DrawerLayout,
    private val toolBarLayout: CollapsingToolbarMotionLayout
) : DrawerLayout.DrawerListener, MotionLayout.TransitionListener {
    companion object {
        private const val TAG = "MotionHelper"
    }

    private val interpolator = AccelerateDecelerateInterpolator()
    private val argbEvaluator = ArgbEvaluatorCompat.getInstance()

    private val darkThemeColor: Int
        get() = view.darkThemeColor
    private val lightThemeColor: Int
        get() = view.lightThemeColor

    private var isDrawerOpened = false
    private var isDarkTheme = true

    fun init() {
        toolBarLayout.addTransitionListener(this)
        drawerLayout.addDrawerListener(this)
    }

    fun close() {
        toolBarLayout.removeTransitionListener(this)
        drawerLayout.removeDrawerListener(this)
    }

    /**
     * [DrawerLayout.DrawerListener]'s override functions
     * */
    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
    override fun onDrawerStateChanged(newState: Int) {}

    override fun onDrawerOpened(drawerView: View) {
        Log.d(TAG, "onDrawerOpened()")
        view.drawerContent?.sharedWidgetsListener?.onDrawerOpened(drawerView)
    }

    override fun onDrawerClosed(drawerView: View) {
        Log.d(TAG, "onDrawerClosed()")
        view.drawerContent?.sharedWidgetsListener?.onDrawerClosed(drawerView)
    }

    /**
     * [MotionLayout.TransitionListener]'s override functions
     * */
    override fun onTransitionStarted(motionLayout: MotionLayout?, startId: Int, endId: Int) {
        val backgroundColor =
            if (isDarkTheme) darkThemeColor
            else lightThemeColor

        view.drawerContent?.colorThemeTransitionListener?.onTransitionStarted(isDarkTheme, backgroundColor)
    }

    override fun onTransitionChange(motionLayout: MotionLayout?, startId: Int, endId: Int, progress: Float) {
        //Log.i(TAG, "onTransitionChange(): startId=$startId, endId=$endId, progress=$progress")

        val interpolate = interpolator.getInterpolation(progress)
        val backgroundColor = argbEvaluator.evaluate(interpolate, darkThemeColor, lightThemeColor)
        val textColor = argbEvaluator.evaluate(interpolate, Color.WHITE, Color.BLACK)

        view.updateBackgroundColor(interpolate, backgroundColor, textColor)
        view.drawerContent?.colorThemeTransitionListener?.onTransitionChanged(interpolate, backgroundColor)

        when {
            interpolate <= 0.5 && isDarkTheme.not() -> {
                isDarkTheme = true

                view.setDarkTheme()
                view.drawerContent?.colorThemeTransitionListener?.onThemeChanged(true)
            }
            interpolate > 0.5 && isDarkTheme -> {
                isDarkTheme = false

                view.setLightTheme()
                view.drawerContent?.colorThemeTransitionListener?.onThemeChanged(false)
            }
        }
    }

    override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
        when (currentId) {
            R.id.start -> {
                view.updateBackgroundColor(0f, darkThemeColor, Color.WHITE)
                view.drawerContent?.colorThemeTransitionListener?.onTransitionChanged(0f, darkThemeColor)
                view.drawerContent?.colorThemeTransitionListener?.onTransitionCompleted(true, darkThemeColor)
            }
            R.id.end -> {
                view.updateBackgroundColor(1f, lightThemeColor, Color.BLACK)
                view.drawerContent?.colorThemeTransitionListener?.onTransitionChanged(1f, lightThemeColor)
                view.drawerContent?.colorThemeTransitionListener?.onTransitionCompleted(false, lightThemeColor)
            }
        }
    }

    override fun onTransitionTrigger(motionLayout: MotionLayout?, triggerId: Int, positive: Boolean, progress: Float) {
        Log.d(TAG, "onTransitionTrigger(): triggerId=$triggerId, positive=$positive, progress=$progress")
    }

    private fun updateVisibleViewHolders(progress: Float, backgroundColor: Int, textColor: Int) {
//        val recyclerView = binding.recyclerView
//        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//
//        val firstPosition = layoutManager.findFirstVisibleItemPosition()
//        val lastPosition = layoutManager.findLastVisibleItemPosition()

//        for (i in firstPosition..lastPosition) {
//            val viewHolder = recyclerView.findViewHolderForLayoutPosition(i) as? AccountGroupListAdapter.DayAndNightThemeViewHolder
//                ?: continue
//
//            viewHolder.onTransitionChange(progress, backgroundColor, textColor)
//        }
    }
}