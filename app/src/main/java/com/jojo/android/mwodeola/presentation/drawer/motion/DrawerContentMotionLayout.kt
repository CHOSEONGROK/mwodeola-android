package com.jojo.android.mwodeola.presentation.drawer.motion

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.drawerlayout.widget.DrawerLayout
import com.jojo.android.mwodeola.R

//class DrawerContentMotionLayout @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
//) : MotionLayout(context, attrs, defStyleAttr), DrawerLayout.DrawerListener {
//
//    override fun onAttachedToWindow() {
//        super.onAttachedToWindow()
//        (parent as? DrawerLayout)?.addDrawerListener(this)
//    }
//
//    override fun onDetachedFromWindow() {
//        super.onDetachedFromWindow()
//        (parent as? DrawerLayout)?.removeDrawerListener(this)
//    }
//
//    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
//        progress = slideOffset
//    }
//
//    override fun onDrawerOpened(drawerView: View) {}
//    override fun onDrawerClosed(drawerView: View) {}
//    override fun onDrawerStateChanged(newState: Int) {}
//}
class DrawerContentMotionLayout @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr), DrawerLayout.DrawerListener {

    companion object {
        private const val TAG = "DrawerContentMotionLayout"
    }

    private var drawerMenuWidth = 0

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        (parent as? DrawerLayout)?.addDrawerListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        (parent as? DrawerLayout)?.removeDrawerListener(this)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        drawerMenuWidth = (parent as? ViewGroup)?.findViewById<ConstraintLayout>(R.id.layout_drawer_menu)?.width ?: 0
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        translationX = drawerMenuWidth * slideOffset
    }

    override fun onDrawerOpened(drawerView: View) {}
    override fun onDrawerClosed(drawerView: View) {}
    override fun onDrawerStateChanged(newState: Int) {}
}