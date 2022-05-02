package com.jojo.android.mwodeola.presentation.security.screen.pin6

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_AUTH
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_DELETE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SCREEN_LOCK
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.DecimalKeyPadFragment


@SuppressLint("SetTextI18n")
class Pin6AuthenticationFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pin6: String
        get() = passwordBuilder.toString()

    private val authenticationCallback = MyAuthenticationCallback()

    override fun canBackPage(): Boolean = false

    override fun initView() = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_AUTH -> "비밀번호를 입력해 주세요"
            PURPOSE_SCREEN_LOCK -> "비밀번호를 입력해 주세요"
            PURPOSE_CHANGE -> "기존에 만들었던 비밀번호를 입력해 주세요"
            PURPOSE_DELETE -> "삭제하려면 비밀번호를 인증해야 합니다"
            else -> ""
        }
        tvSubtitle.text = "PIN 번호 숫자 6자리"

        toggleContainer.forEach {
            if (it is MaterialCardView) {
                toggleViews.add(it)
            } else {
                it.visibility = View.GONE
            }
        }

        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = ViewPagerAdapter()
    }

    inner class OnKeyPadListener : OnSecureKeyPadListener {
        override fun onKeyClicked(char: Char) {
            if (passwordBuilder.length < 6) {
                passwordBuilder.append(char)
                appendToggleAt(passwordBuilder.lastIndex)

                if (passwordBuilder.length == 1) {
                    binding.tvSubtitle.text = "PIN 번호 숫자 6자리"
                    binding.tvSubtitle.setTextColor(subTitleDefaultColor)
                }

                if (passwordBuilder.length == 6) {
                    presenter.checkPassword(pin6, authenticationCallback)
                }
            }
        }

        override fun onBackPressed() {
            if (passwordBuilder.isBlank())
                return

            passwordBuilder.deleteCharAt(passwordBuilder.lastIndex)
            removeToggleAt(passwordBuilder.length)
        }
    }

    inner class ViewPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment =
            DecimalKeyPadFragment(OnKeyPadListener())
    }

    inner class MyAuthenticationCallback : AuthenticationContract.AuthenticationCallback() {
        override fun onSucceed() {
            when (purpose) {
                PURPOSE_AUTH,
                PURPOSE_SCREEN_LOCK -> finishForSucceed()
                PURPOSE_CHANGE -> moveNextPage()
                PURPOSE_DELETE -> presenter.deletePassword()
            }
        }

        override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
            passwordBuilder.clear()
            showIncorrectError()
            showIncorrectErrorText(count, limit)
        }

        override fun onLockedUser() {
            baseActivity.finishForAuthenticationExceeded()
        }
    }
}