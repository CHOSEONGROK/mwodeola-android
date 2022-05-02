package com.jojo.android.mwodeola.presentation.security.screenLock

import androidx.viewbinding.ViewBinding

interface ScreenLockSubscriber {
    val isScreenLockEnabled: Boolean
    val binding: ViewBinding

    fun onResumeFromBackground()
}