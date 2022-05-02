package com.jojo.android.mwodeola.util

import android.app.Activity
import android.content.Context
import android.graphics.Point
import android.os.Build
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.fragment.app.DialogFragment

object Utils {

    fun Context.dialogFragmentResize(dialogFragment: DialogFragment, width: Float, height: Float) {
        val windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        if (Build.VERSION.SDK_INT < 30) {
            val display = windowManager.defaultDisplay
            val size = Point()
            display.getSize(size)

            val window = dialogFragment.dialog?.window
            val x = (size.x * width).toInt()
            val y = (size.y * height).toInt()
            window?.setLayout(x, y)

        } else {
            val rect = windowManager.currentWindowMetrics.bounds

            val window = dialogFragment.dialog?.window
            val x = (rect.width() * width).toInt()
            val y = (rect.height() * height).toInt()
            window?.setLayout(x, y)
        }
    }

    fun showSoftInput(editText: EditText) {
        if (editText.requestFocus()) {
            (editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(editText, InputMethodManager.SHOW_FORCED)
        }
    }
    fun hideSoftInput(view: View) {
        (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun getWindowHeight(activity: Activity): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            activity.display?.getRealMetrics(displayMetrics)
        }
        return displayMetrics.heightPixels
    }

    fun getWindowPixels(activity: Activity): DisplayMetrics {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            activity.display?.getRealMetrics(displayMetrics)
        }
        return displayMetrics
    }
}