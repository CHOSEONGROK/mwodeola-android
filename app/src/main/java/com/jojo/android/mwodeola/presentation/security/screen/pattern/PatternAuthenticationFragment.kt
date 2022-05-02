package com.jojo.android.mwodeola.presentation.security.screen.pattern

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_AUTH
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_DELETE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SCREEN_LOCK
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.LightPatternPasswordFragment

@SuppressLint("SetTextI18n")
class PatternAuthenticationFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pattern: String
        get() = passwordBuilder.toString()

    private lateinit var patternViewFragment: LightPatternPasswordFragment

    private val authenticationCallback = MyAuthenticationCallback()

    override fun canBackPage(): Boolean = false

    override fun initView(): Unit = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_AUTH -> "비밀번호를 입력해 주세요"
            PURPOSE_SCREEN_LOCK -> "비밀번호를 입력해 주세요"
            PURPOSE_CHANGE -> "기존에 만들었던 비밀번호를 입력해 주세요"
            PURPOSE_DELETE -> "삭제하려면 비밀번호를 인증해야 합니다"
            else -> ""
        }

        tvSubtitle.text = "패턴을 그려주세요"

        toggleContainer.visibility = View.GONE

        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = ViewPagerAdapter()
    }

    inner class PatternPasswordWatcher : PatternPasswordView.PatternWatcher {
        override fun onPatternUpdated(pattern: String, added: Char) {
            passwordBuilder.append(added)

            if (pattern.length == 1) {
                binding.tvSubtitle.text = "패턴을 그려주세요"
                binding.tvSubtitle.setTextColor(subTitleDefaultColor)
            }
        }

        override fun onCompleted(pattern: String) {
            if (pattern.length == 1) {
                passwordBuilder.clear()
            } else {
                presenter.checkPassword(pattern, authenticationCallback)
            }
        }
    }

    inner class ViewPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment =
            LightPatternPasswordFragment(PatternPasswordWatcher())
                .also { patternViewFragment = it }
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
            patternViewFragment.showError()

            showIncorrectErrorText(count, limit)
            showIncorrectErrorWithEndAction {
                patternViewFragment.reset()
            }
        }

        override fun onLockedUser() {
            // baseActivity.finishForAuthenticationExceeded()
        }
    }
}