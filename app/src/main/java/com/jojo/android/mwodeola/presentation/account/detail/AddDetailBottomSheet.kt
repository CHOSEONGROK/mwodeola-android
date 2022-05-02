package com.jojo.android.mwodeola.presentation.account.detail

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.FragmentActivity
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountDetail
import com.jojo.android.mwodeola.databinding.BottomSheetAddDetailBinding
import com.jojo.android.mwodeola.model.account.AccountRepository
import com.jojo.android.mwodeola.model.account.AccountSource
import com.jojo.android.mwodeola.presentation.common.BaseBottomSheetDialog
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.bottmSheet.PatternPasswordView
import com.jojo.android.mwodeola.util.KeyboardHeightProvider

class AddDetailBottomSheet(
    private val activity: FragmentActivity,
    private val accountGroupId: String,
    private val listener: (newAccount: Account) -> Unit
) : BaseBottomSheetDialog(activity), View.OnClickListener,
    TextView.OnEditorActionListener, KeyboardHeightProvider.OnKeyboardListener {

    companion object {
        private const val TAG = "AddDetailBottomSheet"
    }

    private val binding = BottomSheetAddDetailBinding.inflate(layoutInflater)
    private val repository = AccountRepository(context)
    private val softInputObserver = KeyboardHeightProvider(activity)
    private var isShowingSoftInput = false

    private val userId: String
        get() = binding.edtUserId.text.toString()
    private val userPassword: String
        get() = binding.edtUserPassword.text.toString()
    private val userPasswordPin4: String
        get() = binding.edtUserPasswordPin4.text.toString()
    private val userPasswordPin6: String
        get() = binding.edtUserPasswordPin6.text.toString()
    private var userPasswordPattern: String = ""
    private val memo: String
        get() = binding.edtMemo.text.toString()

    private val isValidUserId: Boolean
        get() = binding.edtUserId.text.toString().isNotBlank()
    private val isValidUserPassword: Boolean
        get() = binding.edtUserPassword.text.toString().isNotBlank()
    private val isValidUserPasswordPin4: Boolean
        get() = binding.edtUserPasswordPin4.text.toString().length == 4
    private val isValidUserPasswordPin6: Boolean
        get() = binding.edtUserPasswordPin6.text.toString().length == 6
    private val isValidUserPasswordPattern: Boolean
        get() = userPasswordPattern.isNotBlank()
    private val isValidMemo: Boolean
        get() = binding.edtMemo.text.toString().isNotBlank()

    private val isAllBlank: Boolean
        get() = userId.isBlank() && userPassword.isBlank() && userPasswordPin4.isBlank() && userPasswordPin6.isBlank() && userPasswordPattern.isBlank() && memo.isBlank()

    private val appThemeColor = ResourcesCompat.getColor(context.resources, R.color.app_theme_color, null)
    private val grayColor = ResourcesCompat.getColor(context.resources, R.color.gray500, null)
    private val blueColorStateList = ColorStateList.valueOf(appThemeColor)
    private val grayColorStateList = ColorStateList.valueOf(grayColor)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        disableAutofill(binding.root)

        softInputObserver.onResume()

        val windowHeight = getWindowHeight(1f)

        behavior.peekHeight = (windowHeight * 0.95f).toInt()
        behavior.isDraggable = false

        with (binding) {
            scrollView.layoutParams = (scrollView.layoutParams as ConstraintLayout.LayoutParams).also {
                it.constrainedHeight = true
                it.matchConstraintMaxHeight = (windowHeight * 0.8f).toInt()
            }

            btnClose.setOnClickListener { onBackPressed() }

            val listener = this@AddDetailBottomSheet
            edtUserId.setOnEditorActionListener(listener)
            edtUserPassword.setOnEditorActionListener(listener)
            edtUserPasswordPin4.setOnEditorActionListener(listener)
            edtUserPasswordPin6.setOnEditorActionListener(listener)
            edtMemo.setOnEditorActionListener(listener)

            btnExpandUserId.setOnClickListener(listener)
            btnExpandUserPassword.setOnClickListener(listener)
            btnExpandUserPasswordPin4.setOnClickListener(listener)
            btnExpandUserPasswordPin6.setOnClickListener(listener)
            btnExpandUserPasswordPattern.setOnClickListener(listener)
            btnExpandMemo.setOnClickListener(listener)

            val textWatcher = MyTextWatcher()
            edtUserId.addTextChangedListener(textWatcher)
            edtUserPassword.addTextChangedListener(textWatcher)
            edtUserPasswordPin4.addTextChangedListener(textWatcher)
            edtUserPasswordPin6.addTextChangedListener(textWatcher)
            edtMemo.addTextChangedListener(textWatcher)

            val patternWatcher = PattenPasswordWatcher()
            userPasswordPatternView.setPatternWatcher(patternWatcher)
            btnUserPasswordPatternReset.setOnClickListener(patternWatcher)

            btnComplete.setOnClickListener { showSaveConfirmDialog() }
        }

        repository.getAllUserIds(LoadUserIdCallback())
    }

    override fun onBackPressed() {
        if (isShowingSoftInput) {
            hideSoftInput(binding.root)
        } else {
            if (isAllBlank) {
                dismiss()
            } else {
                showCancelConfirmDialog()
            }
        }
    }

    override fun dismiss() {
        Log.d(TAG, "dismiss()")
        softInputObserver.onPause()
        super.dismiss()
    }

    override fun onHeightChanged(height: Int, isShowing: Boolean) {
        isShowingSoftInput = isShowing
    }

    override fun onEditorAction(view: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId != EditorInfo.IME_ACTION_NEXT)
            return false

        when (view) {
            binding.edtUserId -> {
                if (binding.edtLayoutUserPassword.visibility == View.GONE) {
                    onClick(binding.btnExpandUserPassword)
                }

                binding.edtUserPassword.requestFocus()
            }
            binding.edtUserPassword -> {
                if (binding.edtLayoutUserPasswordPin4.visibility == View.GONE) {
                    onClick(binding.btnExpandUserPasswordPin4)
                }

                binding.edtUserPasswordPin4.requestFocus()
            }
            binding.edtUserPasswordPin4 -> {
                if (binding.edtLayoutUserPasswordPin6.visibility == View.GONE) {
                    onClick(binding.btnExpandUserPasswordPin6)
                }

                binding.edtUserPasswordPin6.requestFocus()
            }
            binding.edtUserPasswordPin6 -> {
                if (binding.edtLayoutMemo.visibility == View.GONE) {
                    onClick(binding.btnExpandMemo)
                }

                binding.edtMemo.requestFocus()
            }
        }

        return false
    }

    override fun onClick(view: View?): Unit = with(binding) {
        val contentView = when (view) {
            btnExpandUserId -> edtLayoutUserId
            btnExpandUserPassword -> edtLayoutUserPassword
            btnExpandUserPasswordPin4 -> edtLayoutUserPasswordPin4
            btnExpandUserPasswordPin6 -> edtLayoutUserPasswordPin6
            btnExpandUserPasswordPattern -> userPasswordPatternContent
            btnExpandMemo -> edtLayoutMemo
            else -> return@with
        }
        val editText = when (view) {
            btnExpandUserId -> edtUserId
            btnExpandUserPassword -> edtUserPassword
            btnExpandUserPasswordPin4 -> edtUserPasswordPin4
            btnExpandUserPasswordPin6 -> edtUserPasswordPin6
            btnExpandMemo -> edtMemo
            else -> null
        }

//        TransitionManager.beginDelayedTransition(contentsContainer)
//        TransitionManager.beginDelayedTransition(userIdContainer)

        val rotation: Float
        if (contentView.visibility == View.GONE) {
            contentView.visibility = View.VISIBLE
            rotation = 180f

            if (editText?.text?.isBlank() == true) {
                showSoftKeyboard(editText)
            }
        } else {
            contentView.visibility = View.GONE
            rotation = 0f
            hideSoftInput(view)
        }

        view.animate().setInterpolator(AccelerateDecelerateInterpolator())
            .setDuration(150L)
            .rotation(rotation)
            .start()
    }

    private fun showSaveConfirmDialog() {
        BottomUpDialog.Builder(activity.supportFragmentManager)
            .title("계정을 생성하시겠습니까?")
            .positiveButton {
                save()
            }
            .show()
    }

    private fun showCancelConfirmDialog() {
        BottomUpDialog.Builder(activity.supportFragmentManager)
            .title("계정 만들기를 그만하시겠습니까?")
            .positiveButton {
                dismiss()
            }
            .show()
    }

    private fun update(isValid: Boolean, icon: ImageView, label: TextView, expandButton: ImageView) {
        icon.imageTintList =
            if (isValid) blueColorStateList
            else grayColorStateList
        label.setTextColor(
            if (isValid) appThemeColor
            else grayColor
        )
        expandButton.imageTintList =
            if (isValid) blueColorStateList
            else grayColorStateList
    }

    private fun updateCompleteButton() {
        binding.btnComplete.isEnabled = isValidUserId ||
                isValidUserPassword ||
                isValidUserPasswordPin4 ||
                isValidUserPasswordPin6 ||
                isValidUserPasswordPattern ||
                isValidMemo
    }

    private fun save() {
        val userId = if (isValidUserId) userId else null
        val password = if (isValidUserPassword) userPassword else null
        val pin4 = if (isValidUserPasswordPin4) userPasswordPin4 else null
        val pin6 = if (isValidUserPasswordPin6) userPasswordPin6 else null
        val pattern = if (isValidUserPasswordPattern) userPasswordPattern else null
        val memo = if (isValidMemo) memo else null

        if (userId == null &&
            password == null &&
            pin4 == null &&
            pin6 == null &&
            pattern == null &&
            memo == null) {
            return
        }

        val newDetail = AccountDetail(
            accountGroupId,"",
            userId, password, pin4, pin6, pattern, memo,
            "", "", 0
        )

        repository.addNewDetail(newDetail, DetailSavedCallback())
    }

    inner class PattenPasswordWatcher : PatternPasswordView.PatternWatcher, View.OnClickListener {
        override fun onPatternUpdated(pattern: String, added: Char) {}
        override fun onCompleted(pattern: String) {
            if (pattern.isNotBlank()) {
                userPasswordPattern = pattern

                with (binding) {
                    update(true, userPasswordPatternIcon, userPasswordPatternLabel, btnExpandUserPasswordPattern)
                }

                binding.btnUserPasswordPatternReset.isEnabled = true
                updateCompleteButton()
            }
        }

        override fun onClick(v: View?) {
            // 다시 입력하기 클릭
            binding.userPasswordPatternView.reset()
            binding.btnUserPasswordPatternReset.isEnabled = false

            userPasswordPattern = ""

            with (binding) {
                update(false, userPasswordPatternIcon, userPasswordPatternLabel, btnExpandUserPasswordPattern)
            }

            updateCompleteButton()
        }
    }

    inner class LoadUserIdCallback : AccountSource.LoadDataCallback<List<String>>() {
        override fun onSucceed(data: List<String>) {

            val arrayAdapter = ArrayAdapter(
                context,
                R.layout.auto_complete_text_view_list_item_simple,
                data
            )

            binding.edtUserId.setAdapter(arrayAdapter)
        }
    }

    inner class DetailSavedCallback : AccountSource.LoadDataCallback<Account>() {
        override fun onSucceed(data: Account) {
            listener.invoke(data)
            dismiss()
        }

        override fun onUnknownError(errString: String?) {
            super.onUnknownError(errString)
            Toast.makeText(context, "서버와의 연결이 원활하지 않습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    inner class MyTextWatcher : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun afterTextChanged(s: Editable?) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            with (binding) {
                update(isValidUserId, userIdIcon, userIdLabel, btnExpandUserId)
                update(isValidUserPassword, userPasswordIcon, userPasswordLabel, btnExpandUserPassword)
                update(isValidUserPasswordPin4, userPasswordPin4Icon, userPasswordPin4Label, btnExpandUserPasswordPin4)
                update(isValidUserPasswordPin6, userPasswordPin6Icon, userPasswordPin6Label, btnExpandUserPasswordPin6)
                update(isValidMemo, memoIcon, memoLabel, btnExpandMemo)
            }

            updateCompleteButton()
        }
    }

    class Builder(private val activity: FragmentActivity) {
        private var accountGroupId: String? = null
        private var listener: ((newAccount: Account) -> Unit)? = null

        fun groupId(id: String) = apply {
            accountGroupId = id
        }

        fun listener(listener: (newAccount: Account) -> Unit) = apply {
            this.listener = listener
        }

        fun show() {
            if (accountGroupId == null || listener == null)
                return

            AddDetailBottomSheet(activity, accountGroupId!!, listener!!).show()
        }
    }


}