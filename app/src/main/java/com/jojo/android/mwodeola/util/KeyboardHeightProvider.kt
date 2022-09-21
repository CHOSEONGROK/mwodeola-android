package com.jojo.android.mwodeola.util

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.graphics.Rect
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.PopupWindow

/**
 * Created by Cristian Holdunu on 11/01/2019.
 */
class KeyboardHeightProvider(private val activity: Activity) : PopupWindow(activity) {

    interface OnKeyboardListener {
        fun onHeightChanged(height: Int, isShowing: Boolean)
    }

    val isShowingKeyboard: Boolean
        get() = lastKeyboardHeight > 0
    val lastKeyboardHeight: Int
        get() = _lastKeyboardHeight

    private val resizableView: View
        get() = contentView
//        get() = binding.root
    private var parentView: View? = null
    private var _lastKeyboardHeight = -1

    private val globalLayoutListener = OnMyGlobalLayoutListener()

    private var listener: OnKeyboardListener? = null

    init {
        contentView = View(activity)
        softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
        inputMethodMode = PopupWindow.INPUT_METHOD_NEEDED

        width = 0
        height = WindowManager.LayoutParams.MATCH_PARENT
    }

    fun onResume() {
        parentView = activity.findViewById(android.R.id.content)
        parentView?.post {
            resizableView.viewTreeObserver.addOnGlobalLayoutListener(globalLayoutListener)
            if (!isShowing && parentView?.windowToken != null) {
                showAtLocation(parentView, Gravity.NO_GRAVITY, 0, 0)
            }
        }
    }

    fun onPause() {
        resizableView.viewTreeObserver.removeOnGlobalLayoutListener(globalLayoutListener)
        dismiss()
    }

    fun setOnKeyboardListener(listener: OnKeyboardListener) = apply {
        this.listener = listener
    }

    fun removeOnKeyboardListener() {
        listener = null
    }

    fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(parentView?.windowToken, 0)
    }

    inner class OnMyGlobalLayoutListener : ViewTreeObserver.OnGlobalLayoutListener {

        private val topCutoutHeight: Int
            get() {
                val decorView = activity.window.decorView ?: return 0
                var cutOffHeight = 0
                decorView.rootWindowInsets?.let { windowInsets ->
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        val displayCutout = windowInsets.displayCutout
                        if (displayCutout != null) {
                            val list = displayCutout.boundingRects
                            for (rect in list) {
                                if (rect.top == 0) {
                                    cutOffHeight += rect.bottom - rect.top
                                }
                            }
                        }
                    }
                }
                return cutOffHeight
            }

        override fun onGlobalLayout() {
            computeKeyboardState()
        }

        private fun computeKeyboardState() {
            val screenSize = Point()
            activity.windowManager.defaultDisplay.getSize(screenSize)
            val rect = Rect()
            resizableView.getWindowVisibleDisplayFrame(rect)
            val orientation = activity.resources.configuration.orientation

            val keyboardHeight = screenSize.y + topCutoutHeight - rect.bottom
            KeyboardInfo.keyboardState =
                if (keyboardHeight > 0) KeyboardInfo.STATE_OPENED
                else KeyboardInfo.STATE_CLOSED
            if (keyboardHeight > 0) {
                KeyboardInfo.keyboardHeight = keyboardHeight
            }
            if (keyboardHeight != _lastKeyboardHeight) {
                notifyKeyboardHeightChanged(keyboardHeight, orientation)
            }
            _lastKeyboardHeight = keyboardHeight
        }

        private fun notifyKeyboardHeightChanged(height: Int, orientation: Int) {
            listener?.onHeightChanged(height, height > 0)
        }
    }
}