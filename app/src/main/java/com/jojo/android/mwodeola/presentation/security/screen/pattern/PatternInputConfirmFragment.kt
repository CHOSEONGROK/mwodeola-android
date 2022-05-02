package com.jojo.android.mwodeola.presentation.security.screen.pattern

import android.annotation.SuppressLint
import android.view.View
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.presentation.security.screen.AuthenticationContract
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CHANGE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_CREATE
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationActivity.Companion.PURPOSE_SIGN_UP
import com.jojo.android.mwodeola.presentation.security.screen.BaseAuthenticationFragment
import com.jojo.android.mwodeola.presentation.security.screen.keyPad.LightPatternPasswordFragment

@SuppressLint("SetTextI18n")
class PatternInputConfirmFragment(
    private val presenter: AuthenticationContract.Presenter
) : BaseAuthenticationFragment(presenter) {

    private val pattern: String
        get() = passwordBuilder.toString()

    private lateinit var patternViewFragment: LightPatternPasswordFragment

    override fun canBackPage(): Boolean = true

    override fun initView(): Unit = with(binding) {
        super.initView()

        tvTitle.text = when (purpose) {
            PURPOSE_CREATE,
            PURPOSE_CHANGE-> "앞에서 입력했던 패턴 비밀번호를\n" +
                    "다시 한 번 그려주세요"
            else -> ""
        }
        tvSubtitle.text = "패턴을 그려주세요"

        toggleContainer.visibility = View.GONE

        viewPager2.isUserInputEnabled = false
        viewPager2.adapter = ViewPagerAdapter()
    }

    override fun onInterceptBackPressed(): Boolean {
        binding.tvTitle.text = when (purpose) {
            PURPOSE_CREATE,
            PURPOSE_CHANGE-> "앞에서 입력했던 패턴 비밀번호를\n" +
                    "다시 한 번 그려주세요"
            else -> ""
        }
        return super.onInterceptBackPressed()
    }

    private fun checkPassword() {
        if (pattern == presenter.newPassword) {
            when (purpose) {
                PURPOSE_CREATE -> presenter.createPassword()
                PURPOSE_CHANGE -> presenter.changePassword()
            }
        } else {
            binding.tvTitle.text =
                "앞에서 그렸던 패턴과 일치하지 않습니다\n" +
                        "다시 그려주세요"

            patternViewFragment.showError()
            showIncorrectErrorWithEndAction {
                patternViewFragment.reset()
            }
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
            if (pattern.length == 1) {
                passwordBuilder.clear()
            } else {
                checkPassword()
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