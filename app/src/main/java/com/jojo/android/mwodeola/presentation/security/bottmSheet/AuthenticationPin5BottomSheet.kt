package com.jojo.android.mwodeola.presentation.security.bottmSheet

import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.AppCompatCheckedTextView
import androidx.core.view.forEachIndexed
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.card.MaterialCardView
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.security.SecurityManager

class AuthenticationPin5BottomSheet constructor(
    private val activity: FragmentActivity,
    private val listener: Authenticators.AuthenticationCallback?,
    private val forAutofillService: Boolean
) : BaseAuthenticationBottomSheet(activity, listener, forAutofillService) {

    companion object {
        private const val TAG = "AuthenticationPin5BottomSheet"
    }

    override val authType: AuthType = AuthType.PIN_5
    override val presenter = AuthenticationBottomSheetPresenter(
        authType, this, SecurityManager.SharedPref(activity), SignUpRepository(activity)
    )

    private val pin5: String
        get() = passwordBuilder.toString()

    private val authCallback = MyAuthenticationCallback()

    override fun onBackPressed() {
        Log.d(TAG, "onBackPressed()")
        if (passwordBuilder.isNotBlank()) {
            if (passwordBuilder.length >= 4) {
                binding.viewPager2.currentItem = 0
            }
            passwordBuilder.clear()
            clearToggle()
        } else {
            super.onBackPressed()
        }
    }

    override fun initView(): Unit = with(binding) {
        super.initView()
        tvSubtitle.text = "PIN 번호 5자리 숫자를 입력해주세요"

        viewPager2.apply {
            adapter = ViewPagerAdapter(activity, OnKeyPadListener())
            isUserInputEnabled = false
        }

        toggleContainer.forEachIndexed { index, view ->
            when (index) {
                4 -> view.visibility = View.GONE
                5 -> {}
                // else -> toggleViews.add(view as MaterialCardView)
                else -> toggleViews.add(view as AppCompatCheckedTextView)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = true

    inner class OnKeyPadListener : OnSecureKeyPadListener {
        override fun onKeyClicked(char: Char) {
            if (passwordBuilder.length < 5) {
                passwordBuilder.append(char)
                appendToggleAt(passwordBuilder.lastIndex)

                if (passwordBuilder.length == 1) {
                    binding.tvSubtitle.text = "PIN 번호 5자리 숫자를 입력해주세요"
                    binding.tvSubtitle.setTextColor(subTitleDefaultColor)
                }

                if (passwordBuilder.length == 4) {
                    binding.viewPager2.currentItem = 1
                }

                if (passwordBuilder.length == 5) {
                    presenter.authenticate(pin5, authCallback)
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

    inner class MyAuthenticationCallback : AuthenticationBottomSheetContract.AuthenticationCallback() {
        override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
            showAuthFailureText()
            showAuthFailureCountText(count, limit)
            showAuthFailure()
        }
    }

    class ViewPagerAdapter(
        fragmentActivity: FragmentActivity,
        private val listener: OnSecureKeyPadListener
    ) : FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2
        override fun createFragment(position: Int): Fragment = when (position) {
            0 -> KeyPadDecimalFragment(listener)
            1 -> KeyPadAlphabetFragment(listener)
            else -> throw IllegalArgumentException("position=$position")
        }
    }
}