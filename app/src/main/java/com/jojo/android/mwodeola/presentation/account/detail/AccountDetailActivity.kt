package com.jojo.android.mwodeola.presentation.account.detail

import android.animation.Animator
import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.ListPopupWindow
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.databinding.ActivityAccountDetailBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.account.create.CreateNewAccountActivity
import com.jojo.android.mwodeola.presentation.account.create.CreateNewAccountActivity.Companion.EXTRA_NEW_ACCOUNT_GROUP_AND_DETAIL
import com.jojo.android.mwodeola.presentation.account.datalist.dialog.SelectSnsGroupAndDetailBottomSheet
import com.jojo.android.mwodeola.presentation.account.edit.EditAccountActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.Authenticators
import com.jojo.android.mwodeola.util.dpToPixels
import java.io.Serializable

class AccountDetailActivity : BaseActivity(), AccountDetailContract.View {

    companion object {
        private const val TAG = "AccountDetailActivity"

        /** extra request **/
        const val EXTRA_REQUEST = "extra_request"

        const val CREATE = 1000
        const val LOAD = 1001

        /** extra request: create **/
        const val EXTRA_SNS_CODE = "extra_sns_group_code"
        const val EXTRA_SNS_ACCOUNT = "extra_sns_account"

        /** extra request: load **/
        const val EXTRA_ACCOUNT_GROUP_ID = "extra_account_group_id"
        const val EXTRA_ACCOUNT_ID = "extra_account_id"

        /** extra response **/
        const val EXTRA_RESPONSE = "extra_response"

        const val EXTRA_ACCOUNT_GROUP = "extra_account_group"
        const val EXTRA_DELETED_ACCOUNT_GROUP_ID = "extra_deleted_account_group_id"

        const val CREATED = 2000
        const val UPDATED = 2001
        const val DELETED = 2002
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityAccountDetailBinding.inflate(layoutInflater) }

    override val currentPosition: Int
        get() = binding.viewPager2.currentItem

    override var isDeleteMode = false

    private val presenter: AccountDetailContract.Presenter
        = AccountDetailPresenter(this, AccountRepository(this))

    private val viewpagerAdapter = ViewPagerAdapter(this, presenter)

    private val resultIntent = Intent()

    private val launcherForCreate = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode != RESULT_OK) {
            finish()
            return@registerForActivityResult
        }

        val newAccountGroupAndDetail =
            it.data?.getSerializableExtra(EXTRA_NEW_ACCOUNT_GROUP_AND_DETAIL) as? AccountGroupAndDetails

        if (newAccountGroupAndDetail != null) {
            presenter.updateAccounts(newAccountGroupAndDetail)

            resultIntent.putExtra(EXTRA_RESPONSE, CREATED)
        } else {
            finish()
        }
    }

    private val launcherForEdit = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        val account = it.data?.getSerializableExtra(EditAccountActivity.EXTRA_ACCOUNT) as? Account
        val deletedAccountId = it.data?.getStringExtra(EditAccountActivity.EXTRA_DELETED_ACCOUNT_ID)

        when {
            // Account Updated
            account != null -> {
                presenter.updateAccount(account)

                if (resultIntent.hasExtra(EXTRA_RESPONSE).not()) {
                    resultIntent.putExtra(EXTRA_RESPONSE, UPDATED)
                }
            }
            // Account Deleted
            deletedAccountId != null -> {
                presenter.deletedAccount(deletedAccountId)
            }
        }
    }

    private val holoRedColor by lazy { ResourcesCompat.getColor(resources, android.R.color.holo_red_light, null) }
    private val textDefaultColor by lazy { ResourcesCompat.getColor(resources, R.color.text_view_text_default_color, null) }
    private val btnDeleteAnimatorOn = createDeleteButtonAnimator(0f, 1f)
    private val btnDeleteAnimatorOff = createDeleteButtonAnimator(1f, 0f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()
        handleIntent()
    }

    override fun onBackPressed() {
        if (isDeleteMode) {
            updateDeleteButton(false)
        } else {
            super.onBackPressed()
        }
    }

    override fun showAccounts(accounts: AccountGroupAndDetails, position: Int): Unit = with(binding) {
        val ownGroup = accounts.own_group

        if (ownGroup.isSnsGroup) {
            icon.setSnsGroupIcon(ownGroup.sns)
        } else {
            icon.setNormalGroupIcon(ownGroup)
        }

        tvAccountGroupName.text = ownGroup.group_name
        tvWebUrl.text = ownGroup.web_url
        favoriteIcon.isEnabled = accounts.own_group.is_favorite

        viewpagerAdapter.submit(accounts) {
            binding.viewPager2.currentItem = position
            binding.dotsIndicator.setCustomIcon(accounts.size, R.drawable.plus_icon_small)
        }
    }

    override fun addNewAccountDetail(accountsAfterAdded: AccountGroupAndDetails, position: Int) {
        viewpagerAdapter.submit(accountsAfterAdded) {
            binding.viewPager2.currentItem = position
        }
    }

    override fun deleteAccountDetail(accountsAfterDeleted: AccountGroupAndDetails, position: Int) {
        // viewpagerAdapter.removeAt(position)
        viewpagerAdapter.submit(accountsAfterDeleted)
    }

    override fun updateDeleteButton(performHaptic: Boolean) {
        isDeleteMode = isDeleteMode.not()

        if (isDeleteMode) {
            if (btnDeleteAnimatorOff.isRunning) {
                btnDeleteAnimatorOff.end()
            }
            btnDeleteAnimatorOn.start()
        } else {
            if (btnDeleteAnimatorOn.isRunning) {
                btnDeleteAnimatorOn.end()
            }
            btnDeleteAnimatorOff.start()
        }

        binding.btnDeleteLabel.text =
            if (isDeleteMode) "취소"
            else "삭제"
        binding.btnDeleteLabel.setTextColor(
            if (isDeleteMode) holoRedColor
            else textDefaultColor
        )

        binding.btnDeleteGroup.visibility =
            if (isDeleteMode) View.VISIBLE
            else View.GONE

        viewpagerAdapter.updateDeleteButton(isDeleteMode)

        if (performHaptic) {
            binding.btnDeleteIcon.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
        }
    }

    override fun showAddNormalDetailBottomSheet() {
        AddDetailBottomSheet.Builder(this)
            .groupId(presenter.accounts.own_group.id)
            .listener {
                presenter.addNewDetail(it)
            }
            .show()
    }

    override fun showAddSnsDetailBottomSheet() {
        SelectSnsGroupAndDetailBottomSheet(this)
            .setExclusionList(presenter.accounts.accounts)
            .setListener {
                presenter.addNewSnsDetail(it.detail.id)
            }
            .show()
    }

    override fun showDeleteAccountDetailConfirmDialog(account: Account) {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("이 계정을 삭제하시겠습니까?")
            .positiveButton {
                presenter.deleteAccount(account)
            }
            .show()
    }

    override fun showDeleteAccountGroupConfirmDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("이 계정의 그룹과 정보들을 모두 삭제하시겠습니까?")
            .positiveButton {
                presenter.deleteAccountGroup()
            }
            .show()
    }

    override fun startCreateNewAccountActivity() {
        launcherForCreate.launch(Intent(this, CreateNewAccountActivity::class.java))
    }

    override fun startCreateNewAccountActivityWithSnsDetail(snsAccount: Account) {
        launcherForCreate.launch(Intent(this, CreateNewAccountActivity::class.java).apply {
            putExtra(CreateNewAccountActivity.EXTRA_SNS_ACCOUNT, snsAccount)
        })
    }

    override fun startCreateNewAccountActivityForSnsAccount(snsCode: Int) {
        launcherForCreate.launch(Intent(this, CreateNewAccountActivity::class.java).apply {
            putExtra(CreateNewAccountActivity.EXTRA_SNS_CODE, snsCode)
        })
    }

    override fun startEditAccountActivity(account: Account) {
        val activity = this@AccountDetailActivity

        presenter.authenticate(this, object : Authenticators.AuthenticationCallback() {
            override fun onSucceed() {
                launcherForEdit.launch(Intent(activity, EditAccountActivity::class.java).apply {
                    putExtra(EditAccountActivity.EXTRA_ACCOUNT, account)
                })
            }
            override fun onFailure() {}
            override fun onExceedAuthLimit(limit: Int) {
                showAuthenticationExceedDialog(limit)
            }
        })
    }

    override fun showAuthenticationExceedDialog(limit: Int) {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("비밀번호 인증 횟수 제한을 초과하여 계정이 잠금 처리됩니다")
            .subtitle("인증 오류 횟수: ${limit}번")
            .confirmedButton {
                finishForAuthenticationExceeded()
            }
            .show()
    }

    override fun finishForDeletedAccountGroup(accountGroupId: String?) {
        if (accountGroupId != null) {
            resultIntent.putExtra(EXTRA_RESPONSE, DELETED)
            resultIntent.run {
                putExtra(EXTRA_RESPONSE, DELETED)
                putExtra(EXTRA_DELETED_ACCOUNT_GROUP_ID, accountGroupId)
            }
            setResult(RESULT_OK, resultIntent)
        }

        finish()
    }

    override fun finishForAuthenticationExceeded() {
        finishAffinity()
    }

    override fun finish() {
        Log.d(TAG, "finish()")
        if (resultIntent.hasExtra(EXTRA_RESPONSE)) {
            resultIntent.putExtra(EXTRA_ACCOUNT_GROUP, presenter.accounts.own_group as Serializable)
            setResult(RESULT_OK, resultIntent)
        }
        super.finish()
    }

    private fun initView(): Unit = with(binding) {
        root.clipChildren = false

        viewPager2.let {
            it.adapter = viewpagerAdapter
            it.clipChildren = false
            it.registerOnPageChangeCallback(DetailPageChangeCallback())

            val pageTransformer = ZoomOutPageTransformer(it)
            it.setPageTransformer(pageTransformer)

            (it.getChildAt(0) as RecyclerView).let { recyclerView ->
                recyclerView.setPadding(32.dpToPixels(baseContext), 0, 32.dpToPixels(baseContext), 0)
                recyclerView.clipToPadding = false
                recyclerView.clipChildren = false
                recyclerView.itemAnimator = pageTransformer
            }

            dotsIndicator.setViewPager2(it)
            dotsIndicator.setCustomIcon(0, R.drawable.plus_icon_small)
        }

        btnBack.setOnClickListener { onBackPressed() }
        btnDeleteGroup.setOnClickListener {
            showDeleteAccountGroupConfirmDialog()
        }

        favoriteIcon.isEnabled = false
        btnFavorite.setOnClickListener {
            favoriteIcon.isEnabled = favoriteIcon.isEnabled.not()
            presenter.updateFavorite(favoriteIcon.isEnabled)
        }
        btnEdit.setOnClickListener {
            startEditAccountActivity(presenter.accounts[currentPosition])
        }
        btnShare.setOnClickListener {

        }
        btnDelete.setOnClickListener {
            updateDeleteButton(true)
        }
    }

    private fun handleIntent() {
        when (intent.getIntExtra(EXTRA_REQUEST, -1)) {
            CREATE -> {
                val snsGroupCode = intent.getIntExtra(EXTRA_SNS_CODE, -1)
                val snsAccount = intent.getSerializableExtra(EXTRA_SNS_ACCOUNT) as? Account

                when {
                    // SNS 계정 그룹 생성
                    snsGroupCode != -1 ->
                        startCreateNewAccountActivityForSnsAccount(snsGroupCode)
                    // 일반 계정 그룹 + SNS detail 생성
                    snsAccount != null ->
                        startCreateNewAccountActivityWithSnsDetail(snsAccount)
                    // 일반 계정 그룹 + 일반 detail 생성
                    else ->
                        startCreateNewAccountActivity()
                }
            }
            LOAD -> {
                val accountGroupId = intent.getStringExtra(EXTRA_ACCOUNT_GROUP_ID)!!
                val accountId = intent.getStringExtra(EXTRA_ACCOUNT_ID)

                presenter.loadAccounts(accountGroupId, accountId)
            }
        }
    }

    private fun showTheMorePopup(view: View) {
        val theMoreMenuItems = arrayOf("a", "b", "c")
        PopupMenu(this, view).also {
            it.menuInflater.inflate(R.menu.user_account_info_bot_nav_item, it.menu)

        }.show()
        ListPopupWindow(this).also {
            it.anchorView = view
            //it.setPromptView()
            it.setAdapter(ArrayAdapter(baseContext, android.R.layout.simple_list_item_1, theMoreMenuItems))
            // it.setOnItemClickListener(this@AccountDetailActivity)
        }.show()
    }

    private fun createDeleteButtonAnimator(from: Float, to: Float): Animator =
        ValueAnimator.ofFloat(from, to).apply {
            interpolator = OvershootInterpolator()
            duration = 500
            addUpdateListener {
                val crossFade = it.animatedValue as Float
                if (crossFade in 0.0..1.0) {
                    binding.btnDeleteIcon.crossfade = it.animatedValue as Float
                }
                binding.btnDeleteIcon.rotation = crossFade * 180
            }
        }

    inner class DetailPageChangeCallback : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            val isEnabled = (position < viewpagerAdapter.lastIndex)

            with(binding) {
                if (btnEdit.isEnabled != isEnabled) {
                    btnEdit.isEnabled = isEnabled
                    btnEditIcon.isEnabled = isEnabled
                    btnEditLabel.isEnabled = isEnabled
                }
                if (btnShare.isEnabled != isEnabled) {
                    btnShare.isEnabled = isEnabled
                    btnShareIcon.isEnabled = isEnabled
                    btnShareLabel.isEnabled = isEnabled
                }
            }
        }
    }
}