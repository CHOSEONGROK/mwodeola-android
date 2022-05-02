package com.jojo.android.mwodeola.presentation.account.edit

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewTreeObserver
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.animation.doOnEnd
import androidx.core.animation.doOnStart
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.transition.TransitionManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputLayout
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.databinding.ActivityEditAccountBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.account.create.TextWatcher2
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.util.*

class EditAccountActivity : BaseActivity(), EditAccountContract.View {
    companion object {
        private const val TAG = "EditAccountActivity"

        const val EXTRA_ACCOUNT = "extra_account"
        const val EXTRA_DELETED_ACCOUNT_ID = "extra_deleted_account_id"
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityEditAccountBinding.inflate(layoutInflater) }

    override val viewBinder by lazy { EditAccountActivityViewBinder(this, presenter) }

    var softInputObserver: KeyboardHeightProvider? = null

    private lateinit var presenter: EditAccountContract.Presenter

    private val bottomSheetBehavior by lazy { BottomSheetBehavior.from(binding.bottomSheet) }

    private val backDropOpenAnimator = createBackDropOpenButtonAnimator(0f, 1f)
    private val backDropCloseAnimator = createBackDropOpenButtonAnimator(1f, 0f)

    private var isBackDropOpened = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        softInputObserver = KeyboardHeightProvider(this)

        initView()
    }

    override fun onResume() {
        super.onResume()
        softInputObserver?.onResume()
    }

    override fun onPause() {
        super.onPause()
        softInputObserver?.onPause()
    }

    override fun onBackPressed() {
        when {
            viewBinder.isRunningRemoveMode ->
                viewBinder.cancelRemoveMode()
            isBackDropOpened ->
                hideBackDrop()
            presenter.checkDataChanged() ->
                showExitWithoutSavingConfirmDialog()
            else ->
                finish()
        }
    }

    override fun setUserIdsForAutoComplete(userIds: List<String>) {
        binding.userIdEdt.setAdapter(
            ArrayAdapter(this, R.layout.auto_complete_text_view_list_item_simple, userIds)
        )
    }

    override fun showBackDrop() {
        isBackDropOpened = true

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        if (backDropCloseAnimator.isRunning) {
            backDropCloseAnimator.end()
        }
        backDropOpenAnimator.start()

        binding.bottomSheetContentsBottomGuideline.setGuidelineEnd(
            120.dpToPixels(this) + binding.backDropContainer.height - 56.dpToPixels(this)
        )
    }

    override fun hideBackDrop() {
        isBackDropOpened = false

        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED
        if (backDropOpenAnimator.isRunning) {
            backDropOpenAnimator.end()
        }
        backDropCloseAnimator.start()

        binding.bottomSheetContentsBottomGuideline.setGuidelineEnd(120.dpToPixels(this))
    }

    override fun cancelRemoveMode() {
        viewBinder.cancelRemoveMode()
    }

    override fun showExitWithoutSavingConfirmDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("저장하지 않고 나가시겠습니까?")
            .positiveButton {
                finish()
            }.show()
    }

    override fun showDeleteConfirmDialog() {
        BottomUpDialog.Builder(supportFragmentManager)
            .title("정말 삭제하시겠습니까?")
            .positiveButton {
                presenter.delete()
            }.show()
    }

    override fun finishForUpdated(account: Account) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_ACCOUNT, account)
        })
        finish()
    }

    override fun finishForDeleted(accountId: String) {
        setResult(RESULT_OK, Intent().apply {
            putExtra(EXTRA_DELETED_ACCOUNT_ID, accountId)
        })
        finish()
    }

    private fun initView(): Unit = with(binding) {
        bottomSheetBehavior.state = BottomSheetBehavior.STATE_EXPANDED

        Compat.setLightStatusBar(window)
        window.statusBarColor = ResourcesCompat.getColor(resources, R.color.app_theme_color_dark, null)
        root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                Log.d(TAG, "onGlobalLayout(): rootViewHeight=${root.height}, backDropHeight=${backDropContainer.height}")
                if (root.height != 0 && backDropContainer.height != 0) {
                    bottomSheetBehavior.peekHeight = root.height - backDropContainer.height
                    root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            }
        })

        btnBack.setOnClickListener { onBackPressed() }
        btnDelete.setOnClickListener { showDeleteConfirmDialog() }

        btnToggleBackDrop.setOnClickListener {
            if (isBackDropOpened) {
                hideBackDrop()
            } else {
                showBackDrop()
            }
        }

        val account = intent.getSerializableExtra(EXTRA_ACCOUNT) as? Account
            ?: return@with

        presenter = EditAccountPresenter(
            view = this@EditAccountActivity,
            repository = AccountRepository(this@EditAccountActivity),
            initialAccount = account
        )

        btnToggleBackDrop.isVisible = account.isOwnAccount

        groupIcon.setIconByAccountGroup(account)
        tvAccountGroupName.text = account.own_group.group_name
        ivFavorite.crossfade =
            if (account.own_group.is_favorite) 1f
            else 0f
        edtWebUrl.setText(account.own_group.web_url)

        ivFavorite.setOnClickListener {
            val isFavorite = (ivFavorite.crossfade == 0f)

            val from = if (isFavorite) 0f else 1f
            val to = if (isFavorite) 1f else 0f

            ObjectAnimator.ofFloat(ivFavorite, "crossfade", from, to).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 200
                doOnEnd { ivFavorite.crossfade = to }
            }.start()

            presenter.updateFavorite(isFavorite)
        }

        viewBinder.initView(account)

        if (account.isOwnAccount) {
            presenter.loadUserIds()
        }
    }

    private fun createBackDropOpenButtonAnimator(from: Float, to: Float): ValueAnimator =
        ValueAnimator.ofFloat(from, to).apply {
            val rotationOffset = 180f

            interpolator = OvershootInterpolator()
            duration = 400L
            doOnStart {
                binding.btnToggleBackDrop.crossfade = from
                binding.btnToggleBackDrop.rotation = from * rotationOffset
            }
            addUpdateListener {
                val crossFade = it.animatedValue as Float
                val rotation = crossFade * rotationOffset

                if (crossFade in 0f..1f) {
                    binding.btnToggleBackDrop.crossfade = crossFade
                }
                binding.btnToggleBackDrop.rotation = rotation
            }
            doOnEnd {
                binding.btnToggleBackDrop.crossfade = to
                binding.btnToggleBackDrop.rotation = to * rotationOffset
            }
        }
}