package com.jojo.android.mwodeola.util

import android.content.Intent
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.core.content.ContextCompat

object Compat {

    data class SystemBarInset(val statusBarHeight: Int, val navigationBarHeight: Int)

    fun getSystemBarInset(window: Window): SystemBarInset {
        var statusBarHeight = 0
        var navigationBarHeight = 0

        with(window) {
            val statusBarId = context.resources
                .getIdentifier("status_bar_height", "dimen", "android")
            val navigationBarId = context.resources
                .getIdentifier("navigation_bar_height", "dimen", "android")

            if (statusBarId > 0 && navigationBarId > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(statusBarId)
                navigationBarHeight = context.resources.getDimensionPixelSize(navigationBarId)
            }
        }

        return SystemBarInset(statusBarHeight, navigationBarHeight)
    }

    fun setTransparentSystemBar(window: Window): SystemBarInset {
        var statusBarHeight = 0
        var navigationBarHeight = 0

        with(window) {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                setDecorFitsSystemWindows(false)
            }
            
            statusBarColor = ContextCompat.getColor(context, android.R.color.transparent)
            navigationBarColor = ContextCompat.getColor(context, android.R.color.transparent)

            val statusBarId = context.resources
                .getIdentifier("status_bar_height", "dimen", "android")
            val navigationBarId = context.resources
                .getIdentifier("navigation_bar_height", "dimen", "android")

            if (statusBarId > 0 && navigationBarId > 0) {
                statusBarHeight = context.resources.getDimensionPixelSize(statusBarId)
                navigationBarHeight = context.resources.getDimensionPixelSize(navigationBarId)
            }
        }

        return SystemBarInset(statusBarHeight, navigationBarHeight)
    }

    fun setDefaultStatusBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                0, WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
            )
        } else {
            window.decorView.systemUiVisibility = 0
        }
    }

    fun setLightStatusBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS)
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            } else {
                View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    fun setLightNavigationBar(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.setSystemBarsAppearance(
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,
                WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR or
                        View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        }
    }
}