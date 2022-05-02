package com.jojo.android.mwodeola.presentation.drawer

import com.jojo.android.mwodeola.presentation.drawer.interactive.DrawerContent

interface DrawerContract {

    interface View {
        val darkThemeColor: Int
        val lightThemeColor: Int

        val drawerContent: DrawerContent?

        fun setDarkTheme()
        fun setLightTheme()

        fun updateBackgroundColor(progress: Float, backgroundColor: Int, textColor: Int)

        fun changeDayOrNightTheme(isNightTheme: Boolean)

        fun startSettingsActivity()
        fun finishActivity()
    }

    interface Presenter {

    }
}