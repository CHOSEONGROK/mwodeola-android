package com.jojo.android.mwodeola.presentation.security.screen.pattern

import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationPresenter
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment

class PatternAuthenticationActivity : BaseAuthenticationActivity() {

    override val authType: AuthType = AuthType.PATTERN
    override val presenter by lazy {
        AuthenticationPresenter(
            this,
            authType,
            SecurityManager.SharedPref(this),
            SignUpRepository(this)
        )
    }

    override fun authenticationFragment(): BaseAuthenticationFragment =
        PatternAuthenticationFragment(presenter)

    override fun passwordInputFragment(): BaseAuthenticationFragment =
        PatternInputFragment(presenter)

    override fun passwordInputConfirmFragment(): BaseAuthenticationFragment =
        PatternInputConfirmFragment(presenter)
}