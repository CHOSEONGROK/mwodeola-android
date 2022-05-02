package com.jojo.android.mwodeola.presentation.security.screen.pattern

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CREATE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.LightPatternPasswordFragment

@SuppressLint("SetTextI18n")
class PatternInputFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pattern: String
        get() = passwordBuilder.toString()

    private lateinit var patternViewFragment: LightPatternPasswordFragment

    override fun canBackPage(): Boolean = false

    override fun initView(): Unit = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_CREATE,
            PURPOSE_CHANGE -> "새로 만들 비밀번호를 입력해 주세요"
            else -> ""
        }
        tvSubtitle.text = "패턴을 그려주세요"

        toggleContainer.visibility = View.GONE

        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = ViewPagerAdapter()
    }

    private fun showInvalidPatternError() {
        binding.tvTitle.text =
            "적어도 4개 이상의 점을 연결해 주셔야 합니다\n" +
                    "다시 그려주세요"

        patternViewFragment.showError()
        showIncorrectErrorWithEndAction {
            patternViewFragment.reset()
        }
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
            when {
                pattern.length == 1 ->
                    passwordBuilder.clear()
                pattern.length < 4 ->
                    showInvalidPatternError()
                else -> {
                    presenter.newPassword = passwordBuilder.toString()
                    patternViewFragment.reset()
                    clearPassword()
                    moveNextPage()
                }
            }
        }
    }

    inner class ViewPagerAdapter : FragmentStateAdapter(this) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment =
            LightPatternPasswordFragment(PatternPasswordWatcher())
                .also { patternViewFragment = it }
    }
}