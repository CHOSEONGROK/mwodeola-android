package com.jojo.android.mwodeola.presentation.security.screen.pin6

import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationPresenter
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class Pin6AuthenticationActivity : BaseAuthenticationActivity() {

    override val authType: AuthType = AuthType.PIN_6
    override val presenter by lazy {
        AuthenticationPresenter(
            this,
            authType,
            SecurityManager.SharedPref(this),
            SignUpRepository(this)
        )
    }

    private val randomDecimal = MutableList(10) { it }.apply { shuffle() }

    override fun authenticationFragment(): BaseAuthenticationFragment =
        Pin6AuthenticationFragment(presenter)

    override fun passwordInputFragment(): BaseAuthenticationFragment =
        Pin6InputFragment(presenter)

    override fun passwordInputConfirmFragment(): BaseAuthenticationFragment =
        Pin6InputConfirmFragment(presenter)
}