package com.jojo.android.mwodeola.presentation.common

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog

open class BaseBottomSheetDialog(private val activity: Activity) : BottomSheetDialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

         disableShapeAnimations()
    }

    override fun setContentView(view: View) {
        super.setContentView(view)
        disableAutofill(view)
    }

    override fun setContentView(view: View, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        disableAutofill(view)
    }

    protected fun getWindowHeight(ratio: Float): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window?.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            activity.display?.getRealMetrics(displayMetrics)
        }
        return (displayMetrics.heightPixels * ratio).toInt()
    }

    protected fun showSoftKeyboard(view: EditText) {
        if (view.requestFocus()) {
            (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .showSoftInput(view, InputMethodManager.SHOW_FORCED)
        }
    }

    protected fun hideSoftInput(view: View) {
        (context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(view.windowToken, 0)
    }

    @SuppressLint("RestrictedApi", "VisibleForTests")
    protected fun disableShapeAnimations() {
//        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
//            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
//                    behavior.state = BottomSheetBehavior.STATE_COLLAPSED
//                }
//            }
//        })
        behavior.disableShapeAnimations()
    }

    protected fun disableAutofill(view: View?) {
        if (Build.VERSION.SDK_INT >= 26) {
            view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
    }

    protected fun behaviorStateToString(state: Int): String = when (state) {
        BottomSheetBehavior.STATE_EXPANDED -> "STATE_EXPANDED"
        BottomSheetBehavior.STATE_HALF_EXPANDED -> "STATE_HALF_EXPANDED"
        BottomSheetBehavior.STATE_COLLAPSED -> "STATE_COLLAPSED"
        BottomSheetBehavior.STATE_HIDDEN -> "STATE_HIDDEN"
        BottomSheetBehavior.STATE_DRAGGING -> "STATE_DRAGGING"
        BottomSheetBehavior.STATE_SETTLING -> "STATE_SETTLING"
        else -> "STATE_ELSE"
    }
}