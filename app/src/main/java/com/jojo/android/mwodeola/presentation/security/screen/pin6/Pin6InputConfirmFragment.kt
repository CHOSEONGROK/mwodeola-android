package com.jojo.android.mwodeola.presentation.security.screen.pin6

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.forEach
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CREATE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_UP
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.DecimalKeyPadFragment


@SuppressLint("SetTextI18n")
class Pin6InputConfirmFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pin6: String
        get() = passwordBuilder.toString()

    override fun canBackPage(): Boolean = true

    override fun initView(): Unit = with(binding) {
        tvTitle.text = when (purpose) {
            PURPOSE_CREATE,
            PURPOSE_CHANGE -> "앞에서 입력했던 비밀번호를\n" +
                    "다시 한 번 입력해 주세요"
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

    override fun onInterceptBackPressed(): Boolean {
        binding.tvTitle.text = when (purpose) {
            PURPOSE_CREATE,
            PURPOSE_CHANGE -> "앞에서 입력했던 비밀번호를\n" +
                    "다시 한 번 입력해 주세요"
            else -> ""
        }
        return super.onInterceptBackPressed()
    }

    private fun checkPassword() {
        if (passwordBuilder.toString() == presenter.newPassword) {
            when (purpose) {
                PURPOSE_CREATE -> presenter.createPassword()
                PURPOSE_CHANGE -> presenter.changePassword()
            }
        } else {
            showIncorrectError()
            binding.tvTitle.text =
                "앞에 입력하신 비밀번호와 일치하지 않습니다\n" +
                        "다시 입력해 주세요"
        }
    }

    inner class OnKeyPadListener : OnSecureKeyPadListener {
        override fun onKeyClicked(char: Char) {
            if (passwordBuilder.length <= 6) {
                passwordBuilder.append(char)
                appendToggleAt(passwordBuilder.lastIndex)

                if (passwordBuilder.length == 6) {
                    checkPassword()
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
}