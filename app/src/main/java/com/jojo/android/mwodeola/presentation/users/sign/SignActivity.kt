package com.jojo.android.mwodeola.presentation.users.sign

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AccelerateInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.viewpager2.widget.ViewPager2
import com.jojo.android.mwodeola.autofill.utils.dpToPixels

import com.jojo.android.mwodeola.databinding.ActivitySignBinding
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.common.MwodeolaSnackbar
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.presentation.users.FirebasePhoneAuth
import com.jojo.android.mwodeola.presentation.users.SmsRetrieveHelper

class SignActivity : BaseActivity(), SignContract.ParentView {

    companion object {
        const val TAG = "SignActivity"

        const val FRAGMENT_ITEM_COUNT = 3
        const val FRAGMENT_POSITION_PHONE = 0
        const val FRAGMENT_POSITION_USER_NAME = 1
        const val FRAGMENT_POSITION_EMAIL = 2
    }

    override val isScreenLockEnabled: Boolean = false
    override val binding by lazy { ActivitySignBinding.inflate(layoutInflater) }

    override val presenter: SignContract.Presenter =
        SignPresenter(
            this,
            SignUpRepository(this),
            FirebasePhoneAuth(this))

    override val smsRetrieveHelper = SmsRetrieveHelper(this)

    private val childViews: List<SignContract.BaseChildView>
        get() = supportFragmentManager.fragments.filterIsInstance<SignContract.BaseChildView>()

    private val currentPage: Int
        get() = binding.viewPager2.currentItem

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        setResult(it.resultCode, it.data)
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        smsRetrieveHelper.init()
            .setOnSmsRetrieveCallback(OnMySmsRetrieverCallback())

        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        smsRetrieveHelper.handleActivityResult(requestCode, resultCode, data)
    }

    override fun onBackPressed() {
        when (currentPage) {
            FRAGMENT_POSITION_PHONE,
            FRAGMENT_POSITION_USER_NAME ->
                showDialog()
            FRAGMENT_POSITION_EMAIL ->
                binding.viewPager2.currentItem--
            else -> super.onBackPressed()
        }
    }

    /**
     * [SignContract.ParentView]'s override methods
     * */
    override fun initView(): Unit = with (binding) {
        viewPager2.let {
            it.adapter = SignFragmentStateAdapter(this@SignActivity)
            it.registerOnPageChangeCallback(OnSignPageChangeCallback())
            it.isUserInputEnabled = false
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
        btnNext.setOnClickListener {
            childViews[currentPage].onClickedNextButton()
        }
    }

    override fun moveNextPage() {
        if (currentPage < FRAGMENT_ITEM_COUNT) {
            binding.viewPager2.currentItem++
        }
    }

    override fun enabledNextButton(isEnabled: Boolean) {
//        binding.btnNext.isEnabled = isEnabled
        if (isEnabled) {
            binding.btnNext.animate().translationY(0f).alpha(1f)
                .setDuration(200L).setInterpolator(AccelerateInterpolator()).start()
        } else {
            binding.btnNext.animate().translationY(60f.dpToPixels(baseContext).toFloat()).alpha(0f)
                .setDuration(200L).setInterpolator(AccelerateInterpolator()).start()
        }
    }

    override fun updateNextButton(text: String) {
        binding.btnNext.text = text
    }

    override fun startAppLockActivityForSignUp(name: String, email: String, phoneNumber: String) {
        Authenticators.ScreenBuilder(this)
            .signUp(name, email, phoneNumber)
            .launcher(launcher)
            .execute()
    }

    override fun startAppLockActivityForSignIn(phoneNumber: String) {
        Authenticators.ScreenBuilder(this)
            .signIn(phoneNumber)
            .launcher(launcher)
            .execute()
    }

    override fun showSnackBar(message: String) {
        MwodeolaSnackbar.Builder(binding.root)
            .setMessage(message)
            .setAnchorView(binding.btnNext)
            .show()
    }

    override fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun showDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("여기서 그만하고 나가시겠습니까?")
            .positiveButton("계속하기")
            .negativeButton("안할래요") {
                finish()
            }
            .show()
    }

    /**
     * [SignContract.BaseChildView]'s override methods
     * */
    override fun onSelectedPage(isInitial: Boolean) {}
    override fun onClickedNextButton() {}

    override fun setPhoneNumber(phoneNumber: String) =
        childViews[FRAGMENT_POSITION_PHONE].setPhoneNumber(phoneNumber)

    override fun showSmsAuthInputWidget() =
        childViews[FRAGMENT_POSITION_PHONE].showSmsAuthInputWidget()

    override fun requestFocusSmsCode() =
        childViews[FRAGMENT_POSITION_PHONE].requestFocusSmsCode()

    override fun setSmsCode(code: String) =
        childViews[FRAGMENT_POSITION_PHONE].setSmsCode(code)

    override fun clearSmsCode() =
        childViews[FRAGMENT_POSITION_PHONE].clearSmsCode()

    override fun showIncorrectSmsCodeError() =
        childViews[FRAGMENT_POSITION_PHONE].showIncorrectSmsCodeError()

    override fun updateResendButtonText(remainingTimeString: String) =
        childViews[FRAGMENT_POSITION_PHONE].updateResendButtonText(remainingTimeString)

    override fun setEmail(email: String) =
        childViews[FRAGMENT_POSITION_EMAIL].setEmail(email)

    override fun showPhoneNumberError() =
        childViews[FRAGMENT_POSITION_PHONE].showPhoneNumberError()

    override fun showEmailExistError() =
        childViews[FRAGMENT_POSITION_EMAIL].showEmailExistError()

    private fun movePage(position: Int) {
        Log.i(TAG, "movePage(): position=$position")
        when (position) {
            -1 -> onBackPressed()
            in 0 until FRAGMENT_ITEM_COUNT ->
                binding.viewPager2.currentItem = position
        }
    }

    inner class OnSignPageChangeCallback : ViewPager2.OnPageChangeCallback() {
        private val initialStates = booleanArrayOf(false, true, true)

        override fun onPageScrollStateChanged(state: Int) {
            super.onPageScrollStateChanged(state)
            if (state == ViewPager2.SCROLL_STATE_IDLE) {
                childViews[currentPage].onSelectedPage(initialStates[currentPage])
                initialStates[currentPage] = false
            }
        }
    }

    inner class OnMySmsRetrieverCallback : SmsRetrieveHelper.OnSmsRetrieveCallback {
        override fun onPickedEmail(email: String) {
            childViews.getOrNull(FRAGMENT_POSITION_EMAIL)?.setEmail(email)
        }

        override fun onPickedPhone(phoneNumber: String) {
            childViews.getOrNull(FRAGMENT_POSITION_PHONE)?.setPhoneNumber(phoneNumber)
        }

        override fun onSmsRetrieved(message: String, smsCode: String?) {
            Log.d(TAG, "onSmsRetrieved(): message=$message, smsCode=$smsCode")
            if (smsCode != null) {
                childViews.getOrNull(FRAGMENT_POSITION_PHONE)?.setSmsCode(smsCode)
            }
        }
    }
}