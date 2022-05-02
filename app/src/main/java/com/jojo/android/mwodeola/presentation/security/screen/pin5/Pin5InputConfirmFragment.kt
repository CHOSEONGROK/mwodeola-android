package com.jojo.android.mwodeola.presentation.security.screen.pin5

import android.annotation.SuppressLint
import android.view.View
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_UP
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.AlphabetKeyPadFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.DecimalKeyPadFragment

@SuppressLint("SetTextI18n")
class Pin5InputConfirmFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pin5: String
        get() = passwordBuilder.toString()

    private val keyPadListener = OnKeyPadListener()

    override fun canBackPage(): Boolean = true

    override fun initView(): Unit = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_SIGN_UP,
            PURPOSE_CHANGE -> "앞에서 입력했던 비밀번호를\n" +
                    "다시 한 번 입력해 주세요"
            else -> ""
        }
        tvSubtitle.text = "PIN 번호 숫자 4지라 + 영문 1자리"

        viewPager2.apply {
            adapter = ViewPagerAdapter()
            isUserInputEnabled = false
        }

        toggleContainer.forEachIndexed { index, view ->
            when (index) {
                4 -> view.visibility = View.GONE
                5 -> {}
                else -> toggleViews.add(view as MaterialCardView)
            }
        }
    }

    override fun onInterceptBackPressed(): Boolean {
        binding.tvTitle.text = when (purpose) {
            PURPOSE_SIGN_UP,
            PURPOSE_CHANGE -> "앞에서 입력했던 비밀번호를\n" +
                    "다시 한 번 입력해 주세요"
            else -> ""
        }
        return super.onInterceptBackPressed()
    }

    private fun checkPassword() {
        if (pin5 == presenter.newPassword) {
            when (purpose) {
                PURPOSE_SIGN_UP -> presenter.signUp(pin5)
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
            if (passwordBuilder.length >= 5)
                return

            passwordBuilder.append(char)
            appendToggleAt(passwordBuilder.lastIndex)

            when (passwordBuilder.length) {
                4 -> binding.viewPager2.currentItem = 1
                5 -> checkPassword()
            }
        }

        override fun onBackPressed() {
            if (passwordBuilder.isBlank())
                return

            passwordBuilder.deleteCharAt(passwordBuilder.lastIndex)
            removeToggleAt(passwordBuilder.length)

            if (passwordBuilder.length == 3) {
                binding.viewPager2.currentItem = 0
            }
        }
    }

    inner class ViewPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> DecimalKeyPadFragment(keyPadListener)
            1 -> AlphabetKeyPadFragment(keyPadListener)
            else -> throw IllegalArgumentException("position=$position")
        }
    }
}