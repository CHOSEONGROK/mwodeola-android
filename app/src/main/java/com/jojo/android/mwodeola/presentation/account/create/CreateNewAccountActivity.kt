package com.jojo.android.mwodeola.presentation.account.create

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.data.account.AccountGroupAndDetails
import com.jojo.android.mwodeola.data.common.SnsInfo
import com.jojo.android.mwodeola.databinding.ActivityCreateNewAccountBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.common.CommonRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.account.create.bottomSheet.AppNameSelectBottomSheet
import com.jojo.android.mwodeola.presentation.account.detail.AccountDetailActivity
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.common.SquircleIcon
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.util.dpToPixels

class CreateNewAccountActivity : BaseActivity(), CreateNewAccountContract.View {
    companion object {
        private const val TAG = "CreateNewAccountActivity"

        // response extras
        const val EXTRA_SNS_CODE = AccountDetailActivity.EXTRA_SNS_CODE
        const val EXTRA_SNS_ACCOUNT = AccountDetailActivity.EXTRA_SNS_ACCOUNT

        // request extras
        const val EXTRA_NEW_ACCOUNT_GROUP_AND_DETAIL = "extra_new_account_group_and_detail"

        private const val ANIM_DURATION = 300L
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityCreateNewAccountBinding.inflate(layoutInflater) }
    private val presenter: CreateNewAccountContract.Presenter
            by lazy { CreateNewAccountPresenter(this, AccountRepository(this), CommonRepository(this)) }

    override var mAppInfo: AppInfo? = null

    override val webUrl: String
        get() = binding.edtWebUrl.text.toString()
    override val userID: String
        get() = binding.edtUserId.text.toString()
    override val password: String
        get() = binding.edtUserPassword.text.toString()
    override val passwordPin4: String
        get() = binding.edtUserPasswordPin4.text.toString()
    override val passwordPin6: String
        get() = binding.edtUserPasswordPin6.text.toString()
    override val passwordPattern: String
        get() = binding.userPasswordPatternView.pattern
    override val memo: String
        get() = binding.edtMemo.text.toString()

    private val isShowingWidgets: Boolean
        get() = binding.detailCardView.visibility == View.VISIBLE

    private var bottomSheet: AppNameSelectBottomSheet? = null
    private var bottomSheetListener = AppNameSelectedListener()

    private var snsCode: Int = -1
    private var snsAccount: Account? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initView()

        snsCode = intent.getIntExtra(EXTRA_SNS_CODE, -1)
        snsAccount = intent.getSerializableExtra(EXTRA_SNS_ACCOUNT) as? Account

        when {
            // SNS 계정 그룹 생성
            snsCode != -1 -> {
                presenter.loadSnsAccountGroup(snsCode)
                presenter.loadUserIds()

                hideHelpLabels()
            }
            // 일반 계정 그룹 + SNS detail 생성
            snsAccount != null -> {
                presenter.loadAllAccountGroups(packageManager)

                showHelpLabels()
                //showMainIcon()
                //showSubIcon(snsAccount!!.own_group.sns)
                setSnsDetailInfo(snsAccount!!)
            }
            // 일반 계정 그룹 + 일반 detail 생성
            else -> {
                presenter.loadAllAccountGroups(packageManager)
                presenter.loadUserIds()

                showHelpLabels()
            }
        }
    }

    override fun onBackPressed() {
        showFinishConfirmDialog()
    }

    override fun onDestroy() {
        presenter.onDestroy()
        super.onDestroy()
    }

    override fun initView(): Unit = with(binding) {
        binding.root.layoutTransition?.setDuration(400L) // default 300
        scrollViewContainer.layoutTransition?.setDuration(400L) // default 300

        btnBack.setOnClickListener { onBackPressed() }

        edtLayoutAccountGroupName.isEnabled = false

        edtWebUrl.addTextChangedListener(MyTextWatcher(edtWebUrl))
        edtWebUrl.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus.not()) {
                if (presenter.isValidWebUrl(webUrl).not()) {
                    edtLayoutWebUrl.error = "웹 사이트 형식이 올바르지 않습니다."
                }
            }
        }

        edtUserId.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtUserPassword.requestFocus()
            }
            false
        }

        edtUserId.addTextChangedListener(MyTextWatcher(edtUserId))
        edtUserPassword.addTextChangedListener(MyTextWatcher(edtUserPassword))
        edtUserPasswordPin4.addTextChangedListener(MyTextWatcher(edtUserPasswordPin4))
        edtUserPasswordPin6.addTextChangedListener(MyTextWatcher(edtUserPasswordPin6))
        edtMemo.addTextChangedListener(MyTextWatcher(edtMemo))

        val patternWatcher = PatternPasswordWatcher()
        userPasswordPatternView.setPatternWatcher(patternWatcher)
        userPasswordPatternResetButton.setOnClickListener(patternWatcher)

        btnExpand.setOnClickListener {
            if (btnExpandLabel.text == "항목 더 보기") {
                btnExpandLabel.text = "항목 접기"
                btnExpandIcon.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(150L)
                    .rotation(180f)
                    .start()

                userPasswordPin4Container.isVisible = true
                userPasswordPin6Container.isVisible = true
                userPasswordPatternContainer.isVisible = true
            } else {
                btnExpandLabel.text = "항목 더 보기"
                btnExpandIcon.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(150L)
                    .rotation(0f)
                    .start()

                userPasswordPin4Container.isVisible = false
                userPasswordPin6Container.isVisible = false
                userPasswordPatternContainer.isVisible = false
            }
        }

        btnCreate.isEnabled = false
        btnCreate.setOnClickListener {
            when {
                snsCode != -1 ->
                    presenter.createSnsGroupAndSnsDetail()
                snsAccount != null ->
                    presenter.createNormalGroupAndSnsDetail(snsAccount!!.detail.id)
                else ->
                    presenter.createNormalGroupAndNormalDetail()
            }
        }
    }

    override fun showSnsGroup(snsInfo: SnsInfo): Unit = with(binding) {
        // presenter.checkField() 때문에 해놓음.
        mAppInfo = AppInfo(null, snsInfo.name, snsInfo.app_package_name, null)
        
        mainIcon.setSnsGroupIcon(snsInfo.id)
        edtAccountGroupName.setText(snsInfo.name)
        edtLayoutAccountGroupName.isEnabled = false
        edtLayoutAccountGroupName.endIconDrawable =
            ResourcesCompat.getDrawable(resources, R.drawable.baseline_check_circle_24, null)
        edtLayoutAccountGroupName.helperText = "앱과 성공적으로 연동되었습니다."

        edtWebUrl.setText(snsInfo.web_url)
        
        showMainIcon()
        showInputWidgets()
        showCreateButton(false)
    }

    override fun setSnsDetailInfo(snsAccount: Account): Unit = with(binding) {
        detailCardViewTopAnchorIcon.setSnsGroupIcon(snsAccount.own_group.sns)

        edtUserId.setText(snsAccount.detail.user_id)
        edtLayoutUserId.isEnabled = false
        edtUserPassword.setText(snsAccount.detail.user_password)
        edtLayoutUserPassword.isEnabled = false
        edtUserPasswordPin4.setText(snsAccount.detail.user_password_pin4)
        edtLayoutUserPasswordPin4.isEnabled = false
        edtUserPasswordPin6.setText(snsAccount.detail.user_password_pin6)
        edtLayoutUserPasswordPin6.isEnabled = false
        userPasswordPatternIcon.isEnabled = true
        userPasswordPatternLabel.text = "패턴 비밀번호 (있음)"
        userPasswordPatternLabel.isEnabled = true
        userPasswordPatternView.isVisible = false
        userPasswordPatternView.isEditable = false
        userPasswordPatternResetButton.isEnabled = false
        userPasswordPatternResetButton.isVisible = false
        edtMemo.setText(snsAccount.detail.memo)
        edtLayoutMemo.isEnabled = false
    }

    override fun setExistingGroupsForBottomSheet(groups: List<AccountGroup>) {
        bottomSheet?.setUnavailableList(groups)
    }

    override fun setInstalledUserAppsForBottomSheet(apps: List<AppInfo>) {
        bottomSheet?.setInstalledUserApps(apps)
    }

    override fun setUserIdsForAutoComplete(userIds: List<String>) {
        binding.edtUserId.setAdapter(
            ArrayAdapter(this, R.layout.auto_complete_text_view_list_item_simple, userIds)
        )
    }

    override fun enabledGroupNameLayout() {
        binding.edtLayoutAccountGroupName.isEnabled = true
        binding.edtLayoutAccountGroupNameVirtualView.setOnClickListener {
            if (presenter.isCompletedLoadingAccountGroups) {
                showGroupNameSelectDialog()
            }
        }
    }

    override fun showCreateButton(enabled: Boolean) {
        binding.btnCreate.isVisible = true
        binding.btnCreate.isEnabled = enabled
    }

    override fun showHelpLabels(): Unit = with(binding) {
        label1.alpha = 0f
        label1.translationY = -32.dpToPixels(baseContext).toFloat()
        label2.alpha = 0f
        label2.translationY = -32.dpToPixels(baseContext).toFloat()
        label1.animate().setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(500)
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(200)
            .withEndAction {
                label2.animate().setInterpolator(AccelerateDecelerateInterpolator())
                    .setDuration(500)
                    .alpha(1f)
                    .translationY(0f)
                    .start()
            }
            .start()
    }

    override fun hideHelpLabels(): Unit = with(binding) {
        label1.isVisible = false
        label2.isVisible = false
    }

    override fun showMainIcon(): Unit = with(binding) {
        mainIcon.isVisible = true
    }

    override fun showInputWidgets(): Unit = with(binding) {
        groupWidgetContainer.isVisible = true
        detailCardView.isVisible = true

        if (snsAccount != null) {
            detailCardViewTopAnchorIcon.isVisible = true
        }
    }

    override fun showFinishConfirmDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("계정 만들기를 종료하시겠습니까?")
            .positiveButton { finish() }
            .show()
    }

    override fun showToast(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun finishForSuccess(account: AccountGroupAndDetails) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_NEW_ACCOUNT_GROUP_AND_DETAIL, account)
        })
        finish()
    }

    private fun showGroupNameSelectDialog() {
        bottomSheet = AppNameSelectBottomSheet()
            .setUnavailableList(presenter.existingGroups)
            .setOnItemSelectedListener(bottomSheetListener)

        if (presenter.isCompletedInstalledAppLoadingJob) {
            bottomSheet?.setInstalledUserApps(presenter.installedUserApps)
        }

        bottomSheet?.show(supportFragmentManager, "bottom_sheet")
    }

    inner class AppNameSelectedListener : AppNameSelectBottomSheet.OnItemSelectedListener {
        override fun onSelected(appInfo: AppInfo) {
            mAppInfo = appInfo

            binding.edtAccountGroupName.setText(appInfo.label)
            binding.edtLayoutAccountGroupName.hint = "새 계정의 그룹명"
            binding.edtLayoutAccountGroupName.endIconDrawable =
                if (appInfo.packageName == null) null
                else ResourcesCompat.getDrawable(resources, R.drawable.baseline_check_circle_24, null)
            binding.edtLayoutAccountGroupName.helperText =
                if (appInfo.packageName == null) null
                else "앱과 성공적으로 연동되었습니다."

            if (appInfo.icon != null) {
                binding.mainIcon.setIconImageDrawable(appInfo.icon)
            } else {
                binding.mainIcon.setIconText(appInfo.label)
                binding.mainIcon.setIconBackgroundColor(SquircleIcon.COLOR_ORANGE)
            }

            if (isShowingWidgets.not()) {
                hideHelpLabels()

                showMainIcon()
                showInputWidgets()
                showCreateButton(snsAccount != null)
            }
        }
    }

    inner class MyTextWatcher(editText: EditText) : TextWatcher2(editText) {
        private val icon: ImageView? = when (editText) {
            binding.edtWebUrl -> binding.webUrlIcon
            binding.edtUserId -> binding.userIdIcon
            binding.edtUserPassword -> binding.userPasswordIcon
            binding.edtUserPasswordPin4 -> binding.userPasswordPin4Icon
            binding.edtUserPasswordPin6 -> binding.userPasswordPin6Icon
            binding.edtMemo -> binding.memoIcon
            else -> null
        }

        private val label: TextView? = when (editText) {
            binding.edtWebUrl -> binding.webUrlLabel
            binding.edtUserId -> binding.userIdLabel
            binding.edtUserPassword -> binding.userPasswordLabel
            binding.edtUserPasswordPin4 -> binding.userPasswordPin4Label
            binding.edtUserPasswordPin6 -> binding.userPasswordPin6Label
            binding.edtMemo -> binding.memoLabel
            else -> null
        }

        private val toggleOnLength: Int = when (editText) {
            binding.edtUserId, binding.edtUserPassword, binding.edtMemo -> 1
            binding.edtUserPasswordPin4 -> 4
            binding.edtUserPasswordPin6 -> 6
            else -> -1
        }

        private val btnCreate: TextView = binding.btnCreate

        init {
            icon?.isEnabled = false
            label?.isEnabled = false
        }

        override fun onTextChanged(editText: EditText, text: CharSequence) {
            if (editText == binding.edtWebUrl) {
                val isValid = presenter.isValidWebUrl(webUrl)

                icon?.isEnabled = isValid
                label?.isEnabled = isValid

                btnCreate.isEnabled = presenter.checkInputFields()

                if (isValid) {
                    binding.edtLayoutWebUrl.error = null
                }
            } else {
                val isToggleOn = (text.length >= toggleOnLength)

                icon?.isEnabled = isToggleOn
                label?.isEnabled = isToggleOn

                btnCreate.isEnabled = presenter.checkInputFields()
            }
        }
    }

    inner class PatternPasswordWatcher : PatternPasswordView.PatternWatcher, View.OnClickListener {
        private val icon: ImageView = binding.userPasswordPatternIcon
        private val label: TextView = binding.userPasswordPatternLabel
        private val patternView: PatternPasswordView = binding.userPasswordPatternView
        private val btnReset: Button = binding.userPasswordPatternResetButton
        private val btnCreate: TextView = binding.btnCreate

        init {
            icon.isEnabled = false
            label.isEnabled = false
        }

        override fun onPatternUpdated(pattern: String, added: Char) {}
        override fun onCompleted(pattern: String) {
            if (pattern.isEmpty())
                return

            icon.isEnabled = true
            label.isEnabled = true

            btnReset.isEnabled = true
            btnCreate.isEnabled = presenter.checkInputFields()
        }

        override fun onClick(view: View?) {
            patternView.reset()

            icon.isEnabled = false
            label.isEnabled = false

            btnReset.isEnabled = false
            btnCreate.isEnabled = presenter.checkInputFields()
        }
    }
}