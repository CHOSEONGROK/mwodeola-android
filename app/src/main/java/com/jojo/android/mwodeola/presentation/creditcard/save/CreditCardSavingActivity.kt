package com.jojo.android.mwodeola.presentation.creditcard.save

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.jojo.android.mwodeola.presentation.BaseActivity

class CreditCardSavingActivity : BaseActivity(), CreditCardSavingContract.Presenter {

    override val isScreenLockEnabled: Boolean = true
    override val binding: ViewBinding
        get() = TODO("Not yet implemented")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}