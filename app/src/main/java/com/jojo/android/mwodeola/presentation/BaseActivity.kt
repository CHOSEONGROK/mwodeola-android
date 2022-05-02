package com.jojo.android.mwodeola.presentation

import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.jojo.android.mwodeola.presentation.security.screenLock.ScreenLockSubscriber

abstract class BaseActivity : AppCompatActivity(), ScreenLockSubscriber {

    abstract override val isScreenLockEnabled: Boolean
    abstract override val binding: ViewBinding

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        // window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
    }

    override fun setContentView(view: View?) {
        super.setContentView(view)
        disableAutofill(view)
    }

    override fun onResumeFromBackground() {

    }

    override fun setContentView(view: View?, params: ViewGroup.LayoutParams?) {
        super.setContentView(view, params)
        disableAutofill(view)
    }


    private fun disableAutofill(view: View?) {
        if (Build.VERSION.SDK_INT >= 26) {
            view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            //view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
    }
}