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

class AuthenticationPin6BottomSheet constructor(
    private val activity: FragmentActivity,
    private val listener: Authenticators.AuthenticationCallback?,
    private val forAutofillService: Boolean
) : BaseAuthenticationBottomSheet(activity, listener, forAutofillService) {

    companion object {
        private const val TAG = "AuthenticationPin6BottomSheet"
    }

    private val pin6: String
        get() = passwordBuilder.toString()

    override val authType: AuthType = AuthType.PIN_6
    override val presenter = AuthenticationBottomSheetPresenter(
        authType, this, SecurityManager.SharedPref(activity), SignUpRepository(activity)
    )

    private val authCallback = MyAuthenticationCallback()

    override fun initView(): Unit = with(binding) {
        super.initView()
        tvSubtitle.text = "PIN 번호 6자리 숫자를 입력해주세요"

        viewPager2.apply {
            adapter = ViewPagerAdapter(activity, OnKeyPadListener())
            isUserInputEnabled = false
        }

        toggleContainer.forEachIndexed { index, view ->
            when (index) {
                5 -> view.visibility = View.GONE
                // else -> toggleViews.add(view as MaterialCardView)
                else -> toggleViews.add(view as AppCompatCheckedTextView)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean = true

    inner class OnKeyPadListener : OnSecureKeyPadListener {
        override fun onKeyClicked(char: Char) {
            if (passwordBuilder.length < 6) {
                passwordBuilder.append(char)
                Log.d(TAG, "onKeyClicked(): $passwordBuilder")
                Log.d(TAG, "onKeyClicked(): ${passwordBuilder.lastIndex}")
                appendToggleAt(passwordBuilder.lastIndex)

                if (passwordBuilder.length == 1) {
                    binding.tvSubtitle.text = "PIN 번호 6자리 숫자를 입력해주세요"
                    binding.tvSubtitle.setTextColor(subTitleDefaultColor)
                }
                if (passwordBuilder.length == 6) {
                    presenter.authenticate(pin6, authCallback)
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

    inner class MyAuthenticationCallback : AuthenticationBottomSheetContract.AuthenticationCallback() {
        override fun onIncorrectPassword(count: Int, limit: Int, isExceed: Boolean) {
            showAuthFailureText()
            showAuthFailureCountText(count, limit)
            showAuthFailure()
        }
    }

    class ViewPagerAdapter(
        activity: FragmentActivity,
        private val listener: OnSecureKeyPadListener
    ) : FragmentStateAdapter(activity) {
        override fun getItemCount(): Int = 1
        override fun createFragment(position: Int): Fragment =
            KeyPadDecimalFragment(listener)
    }
}