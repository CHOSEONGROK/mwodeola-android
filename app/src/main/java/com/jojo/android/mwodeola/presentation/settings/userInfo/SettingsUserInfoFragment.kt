package com.jojo.android.mwodeola.presentation.settings.userInfo

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.jojo.android.mwodeola.data.users.UserInfo
import com.jojo.android.mwodeola.databinding.FragmentSettingsUserInfoBinding
import com.jojo.android.mwodeola.model.sign.SignUpRepository
import com.jojo.android.mwodeola.presentation.BaseFragment
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.intro.IntroActivity
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.settings.SettingsSupportFragment

class SettingsUserInfoFragment : BaseFragment(), SettingsSupportFragment, SettingsUserInfoContract.View {

    override val toolBarTitle: String = "내 정보"

    private lateinit var binding: FragmentSettingsUserInfoBinding
    private lateinit var presenter: SettingsUserInfoPresenter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSettingsUserInfoBinding.inflate(inflater)
        presenter = SettingsUserInfoPresenter(
            this,
            SignUpRepository(requireContext()),
            SecurityManager.SharedPref(requireContext())
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with (binding) {
            userNameContainer.setOnClickListener { showToast("준비 중입니다.") }
            userPhoneContainer.setOnClickListener { showToast("준비 중입니다.") }
            userEmailContainer.setOnClickListener { showToast("준비 중입니다.") }

            btnSignOut.setOnClickListener {
                showSignOutConfirmDialog()
            }
            btnWithdrawal.setOnClickListener {
                showWithdrawalBottomSheet()
            }
        }

        presenter.loadUserInfo()
    }

    override fun showUserInfo(userInfo: UserInfo) = with(binding) {
        tvUserName.text = userInfo.user_name
        tvUserEmail.text = userInfo.email
        tvUserPhone.text = userInfo.phone_number
    }

    override fun showSignOutConfirmDialog() {
        BottomUpDialog.Builder(requireActivity().supportFragmentManager)
            .title("로그아웃 하시겠습니까?")
            .positiveButton {
                presenter.signOut(requireContext())
            }
            .show()
    }

    override fun showWithdrawalBottomSheet() {

    }

    override fun restartApp() {
        requireActivity().let {
            it.finishAffinity()
            it.startActivity(Intent(it, IntroActivity::class.java))
        }
//        Runtime.getRuntime().exit(0)
        //Intent.makeRestartActivityTask()
    }

//    private fun restart(context: Context) {
//        val intent = packageManager.getLaunchIntentForPackage(packageName)
//        val componentName = intent!!.component
//        val mainIntent = Intent.makeRestartActivityTask(componentName)
//        context.startActivity(mainIntent)
//        Runtime.getRuntime().exit(0)
//    }

    override fun showToast(message: String?) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}