package com.jojo.android.mwodeola.presentation.drawer.interactive

import android.widget.Button
import android.widget.CheckBox
import androidx.activity.result.ActivityResult
import com.google.android.material.floatingactionbutton.FloatingActionButton

interface DrawerContent {

    val colorThemeTransitionListener: DrawerOwner.OnColorThemeTransitionListener
    val sharedWidgetsListener: DrawerOwner.OnSharedWidgetsListener

    fun onBackPressed(): Boolean
    fun onActivityResult(result: ActivityResult)
}