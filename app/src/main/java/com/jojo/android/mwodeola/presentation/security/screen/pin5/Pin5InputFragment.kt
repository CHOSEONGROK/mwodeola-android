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
class Pin5InputFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pin5: String
        get() = passwordBuilder.toString()

    private val keyPadListener = OnKeyPadListener()

    override fun canBackPage(): Boolean = false

    override fun initView(): Unit = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_SIGN_UP -> "뭐더라에서 사용할 비밀번호를 생성합니다"
            PURPOSE_CHANGE -> "새로 만들 비밀번호를 입력해 주세요"
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

    inner class OnKeyPadListener : OnSecureKeyPadListener {
        override fun onKeyClicked(char: Char) {
            if (passwordBuilder.length >= 5)
                return

            passwordBuilder.append(char)
            appendToggleAt(passwordBuilder.lastIndex)

            when (passwordBuilder.length) {
                4 -> binding.viewPager2.currentItem = 1
                5 -> {
                    presenter.newPassword = pin5
                    moveNextPage()
                    clearPassword()
                }
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