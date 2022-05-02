package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class AuthenticationPatternBottomSheet(
    private val activity: FragmentActivity,
    private val listener: Authenticators.AuthenticationCallback?,
    private val forAutofillService: Boolean
) : BaseAuthenticationBottomSheet(activity, listener, forAutofillService) {
    companion object {
        private const val TAG = "AuthenticationPatternBottomSheet"
    }

    override val authType: AuthType = AuthType.PATTERN
    override val presenter = AuthenticationBottomSheetPresenter(
        authType, this, SecurityManager.SharedPref(activity), SignUpRepository(activity)
    )

    private val pattern: String
        get() = passwordBuilder.toString()

    private val authCallback = MyAuthenticationCallback()

    private lateinit var fragment: PatternPasswordFragment

    override fun initView(): Unit = with(binding) {
        super.initView()
        tvSubtitle.text = "암호 패턴을 그려주세요"

        toggleContainer.visibility = View.INVISIBLE

        viewPager2.apply {
            isUserInputEnabled = false
            adapter = ViewPagerAdapter(activity, PatternPasswordWatcher())
        }
    }

    inner class PatternPasswordWatcher : PatternPasswordView.PatternWatcher {
        override fun onPatternUpdated(pattern: String, added: Char) {
            passwordBuilder.append(added)

            if (pattern.length == 1) {
                binding.tvSubtitle.text = "암호 패턴을 그려주세요"
                binding.tvSubtitle.setTextColor(subTitleDefaultColor)
            }
        }

        override fun onCompleted(pattern: String) {
            if (pattern.length == 1) {
                passwordBuilder.clear()
            } else {
                presenter.authenticate(pattern, authCallback)
            }
        }
    }

    inner class MyAuthenticationCallback : AuthenticationBottomSheetContract.AuthenticationCallback() {
        override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
            fragment.showError()

            showAuthFailureText()
            showAuthFailureCountText(count, limit)
            vibrateAuthFailure()

            if (count < limit) {
                postDelayed {
                    fragment.reset()
                }
            }
        }

        private fun postDelayed(runnable: Runnable) {
            Handler(Looper.getMainLooper()).postDelayed(runnable, 400)
        }
    }

    inner class ViewPagerAdapter(
        activity: FragmentActivity,
        private val watcher: PatternPasswordView.PatternWatcher
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment =
            PatternPasswordFragment(watcher).also { fragment = it }
    }
}