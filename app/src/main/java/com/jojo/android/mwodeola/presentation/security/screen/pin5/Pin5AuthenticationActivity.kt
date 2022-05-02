package com.jojo.android.mwodeola.presentation.security.screen.pin5

import android.os.Bundle
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationPresenter
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class Pin5AuthenticationActivity : BaseAuthenticationActivity() {

    override val authType: AuthType = AuthType.PIN_5
    override val presenter by lazy {
        AuthenticationPresenter(
            this,
            authType,
            SecurityManager.SharedPref(this),
            SignUpRepository(this)
        )
    }

    override fun authenticationFragment(): BaseAuthenticationFragment =
        Pin5AuthenticationFragment(presenter)

    override fun passwordInputFragment(): BaseAuthenticationFragment =
        Pin5InputFragment(presenter)

    override fun passwordInputConfirmFragment(): BaseAuthenticationFragment =
        Pin5InputConfirmFragment(presenter)
}