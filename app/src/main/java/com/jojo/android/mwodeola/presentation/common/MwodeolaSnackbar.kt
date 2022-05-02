package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.graphics.Color
import android.view.View
import androidx.annotation.ColorInt
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_SLIDE
import com.google.android.material.snackbar.Snackbar
import com.jojo.android.mwodeola.R

object MwodeolaSnackbar {

    fun show(view: View, message: String, anchorView: View? = null) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
            .setAnimationMode(ANIMATION_MODE_SLIDE)

//            .setTextColor(Color.BLACK)
//            .setBackgroundTint(getAppThemeColor(view.context))
        if (anchorView != null) {
            snackbar.anchorView = anchorView
        }

        snackbar.show()
    }

    @ColorInt
    private fun getAppThemeColor(context: Context): Int =
        ResourcesCompat.getColor(context.resources, R.color.app_theme_color, null)


    class Builder(private val view: View) {
        private var message: String? = null
        private var anchorView: View? = null

        fun setMessage(message: String): Builder = apply {
            this.message = message
        }
        fun setAnchorView(anchor: View): Builder = apply {
            this.anchorView = anchor
        }

        fun show() {
            if (message == null)
                return

            Snackbar.make(view, message!!, Snackbar.LENGTH_LONG).also {
                it.animationMode = ANIMATION_MODE_SLIDE

                if (this.anchorView != null) {
                    it.anchorView = this.anchorView
                }
            }.show()
        }
    }
}
