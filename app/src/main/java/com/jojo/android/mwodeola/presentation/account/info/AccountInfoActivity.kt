package com.jojo.android.mwodeola.presentation.account.info

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.forEach
import com.google.android.material.navigation.NavigationBarView
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.ActivityAccountInfoBinding
import com.jojo.android.mwodeola.presentation.BaseActivity

class AccountInfoActivity : BaseActivity(), AccountInfoContract.View, NavigationBarView.OnItemSelectedListener {
    companion object {
        const val TAG = "AccountInfoActivity"
        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_ACCOUNT_ID = "e"
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityAccountInfoBinding.inflate(layoutInflater) }
//
//    private lateinit var presenter: AccountInfoContract.Presenter
//
//    private val apps by lazy { packageManager.getInstalledApplications(PackageManager.GET_META_DATA) }
//
//    private val favoriteMenuItem: MenuItem
//        get() = binding.bottomNavigation.menu.findItem(R.id.action_favorite)
//
//    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
//        if (it.resultCode == RESULT_OK && it.data != null) {
//            val account =
//                it.data!!.getSerializableExtra(AccountSavingActivityOld.EXTRA_ACCOUNT_GROUP) as Account
//            presenter.account = account
//
//            // Account 의 내용이 변경됨을 알림
//            this.setResult(RESULT_OK)
//        }
//    }
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(binding.root)
//
//        val account = intent.getSerializableExtra(EXTRA_ACCOUNT_GROUP) as Account
//
//        presenter = AccountInfoPresenter(this, account)
//
//        initView()
//    }
//
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
//        when (item.itemId) {
//            R.id.action_favorite ->
//                presenter.updateFavorite()
//            R.id.action_edit ->
//                startAccountSaveActivity(presenter.account)
//            R.id.action_share -> {}
//            R.id.action_more -> {}
//        }
        return false
    }
//
//    @SuppressLint("SetTextI18n")
//    override fun showData(account: Account): Unit = with (binding) {
//        when (account.ownGroup.iconType) {
//            AccountGroup.IconType.TEXT ->
//                icon.setIconText(account.ownGroup.name)
//            AccountGroup.IconType.INSTALLED_APP_LOGO ->
//                icon.setIconImageDrawable(
//                    apps.find { it.packageName == account.ownGroup.appPackageName }
//                        ?.loadIcon(packageManager)
//                )
//            else -> {}
//        }
//
//        tvIconType.text = "IconType: ${account.ownGroup.iconType}"
//        tvAccountName.text = "AccountName: ${account.ownGroup.name}"
//        tvPackageName.text = "PackageName: ${account.ownGroup.appPackageName}"
//        tvUserId.text = "UserID: ${account.userID}"
//        tieUserPassword.setText(account.password)
//        tvUrl.text = "Url: ${account.ownGroup.url}"
//        tvMetadata.text = account.creationDate
//
//        updateFavoriteIcon(presenter.account.ownGroup.isFavorite)
//    }
//
//    override fun updateFavoriteIcon(isFavorite: Boolean) {
//        favoriteMenuItem.setIcon(
//            if (isFavorite) R.drawable.favorite_star_full
//            else R.drawable.favorite_star_empty)
////        favoriteMenuItem.setIconTintList()
//    }
//
//    override fun startAccountSaveActivity(account: Account) {
//        launcher.launch(Intent(this, AccountSavingActivityOld::class.java).apply {
//            putExtra(AccountSavingActivityOld.EXTRA_IS_NEW_DATA, false)
//            putExtra(AccountSavingActivityOld.EXTRA_ACCOUNT_GROUP, account)
//        })
//    }
//
//    private fun initView() {
//        binding.btnBack.setOnClickListener { onBackPressed() }
//        binding.bottomNavigation.setOnItemSelectedListener(this)
//        binding.bottomNavigation.menu.forEach { it.isCheckable = false }
//    }
}