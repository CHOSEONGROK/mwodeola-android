package com.jojo.android.mwodeola.presentation

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

open class BaseFragment : Fragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        disableAutofill(view)
    }

    private fun disableAutofill(view: View?) {
        if (Build.VERSION.SDK_INT >= 26) {
            view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO
            //view?.importantForAutofill = View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS
        }
    }
}