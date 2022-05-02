package com.jojo.android.mwodeola.presentation.account.create.bottomSheet

import android.app.Dialog
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.CycleInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jojo.android.mwodeola.data.account.AccountGroup
import com.jojo.android.mwodeola.databinding.BottomSheetAppNameSelectBinding
import com.jojo.android.mwodeola.presentation.account.create.AppInfo
import com.jojo.android.mwodeola.util.KeyboardHeightProvider
import com.jojo.android.mwodeola.util.dpToPixels

class AppNameSelectBottomSheet : BottomSheetDialogFragment() {
    companion object {
        private const val TAG = "AppNameSelectBottomSheet"
    }

    interface OnItemSelectedListener {
        fun onSelected(appInfo: AppInfo)
    }

    private val window: Window
        get() = requireActivity().window
    private val behavior: BottomSheetBehavior<FrameLayout>?
        get() = (dialog as? BottomSheetDialog)?.behavior

    private val shakeErrorAnim = TranslateAnimation(0f, 20f, 0f, 0f).apply {
        duration = 300
        interpolator = CycleInterpolator(2f)
    }

    lateinit var binding: BottomSheetAppNameSelectBinding

    private val adapter = AppNameSelectBottomSheetRvAdapter(this)
    private val unavailableList = arrayListOf<AccountGroup>()

    private var softInputProvider: KeyboardHeightProvider? = null
    private var listener: OnItemSelectedListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP &&
                binding.edtGroupName.text?.isNotBlank() == true) {
                binding.edtGroupName.text?.clear()
                true
            } else {
                false
            }
        }
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetAppNameSelectBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
    }

    override fun onResume() {
        super.onResume()
        softInputProvider?.onResume()
    }

    override fun onPause() {
        super.onPause()
        softInputProvider?.onPause()
    }

    fun setUnavailableList(unavailableList: List<AccountGroup>) = apply {
        this.unavailableList.clear()
        this.unavailableList.addAll(unavailableList)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) = apply {
        this.listener = listener
    }

    fun removeOnItemSelectedListener() {
        listener = null
    }

    fun setInstalledUserApps(apps: List<AppInfo>) {
        stopShimmer()
        adapter.setData(apps)
        if (::binding.isInitialized) {
            adapter.filter(binding.edtGroupName.text.toString())
        }
    }

    fun setGroupName(appInfo: AppInfo) {
        dismiss()
        listener?.onSelected(appInfo)
    }

    fun showExistsError() {
        binding.edtLayoutGroupName.error = "이미 존재하는 그룹명입니다"
        binding.edtGroupName.setTextColor(Color.RED)
        binding.edtLayoutGroupName.startAnimation(shakeErrorAnim)
    }

    fun hideExistsError() {
        if (binding.edtLayoutGroupName.error != null) {
            binding.edtLayoutGroupName.error = null
            binding.edtGroupName.setTextColor(Color.BLACK)
        }
    }

    private fun initView() {
        initDialogHeight()

        val watcher = EditTextWatcher()
        binding.edtGroupName.setText("")
        binding.edtGroupName.addTextChangedListener(watcher)
        binding.edtGroupName.setOnEditorActionListener(watcher)
        binding.btnComplete.setOnClickListener {
            onClickCompleted()
        }

        binding.recyclerView.let {
            val layoutManager = GridLayoutManager(requireContext(), 2)

            it.layoutManager = layoutManager
            it.adapter = adapter
            it.isNestedScrollingEnabled = false
        }

        softInputProvider = KeyboardHeightProvider(requireActivity())
            .setOnKeyboardListener(SoftInputObserver())

        if (adapter.items.isEmpty()) {
            startShimmer()
        } else {
            stopShimmer()
            adapter.filter(binding.edtGroupName.text.toString())
        }
    }

    private fun onClickCompleted() {
        val editTextGroupName = binding.edtGroupName.text.toString()
        if (editTextGroupName.isBlank())
            return

        val isExists = unavailableList.any { it.group_name.equals(editTextGroupName, false) }
        if (isExists) {
            showExistsError()
            return
        }

        if (adapter.itemsForFiltered.size == 1) {
            listener?.onSelected(adapter.itemsForFiltered[0])
        } else {
            val appInfo = AppInfo(
                icon = null,
                label = editTextGroupName,
                packageName = null
            )
            listener?.onSelected(appInfo)
        }
        dismiss()
    }

    private fun startShimmer() {
        if (::binding.isInitialized) {
            binding.shimmer.startShimmer()
            binding.shimmer.visibility = View.VISIBLE
            binding.recyclerView.visibility = View.INVISIBLE
        }
    }

    private fun stopShimmer() {
        if (::binding.isInitialized) {
            binding.shimmer.stopShimmer()
            binding.shimmer.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
    }

    private fun initDialogHeight() {
        val windowHeight = getWindowHeight(0.85f)
        if (windowHeight > 0) {
            binding.root.layoutParams = binding.root.layoutParams.apply {
                height = windowHeight
            }
        }
        behavior?.peekHeight = getWindowHeight(0.85f)
        behavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {}
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == STATE_EXPANDED) {
                    behavior?.state = STATE_COLLAPSED
                }
            }
        })
//        behavior?.disableShapeAnimations()
    }


    private fun getWindowHeight(ratio: Float): Int {
        val displayMetrics = DisplayMetrics()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            requireActivity().window.windowManager?.defaultDisplay?.getRealMetrics(displayMetrics)
        } else {
            requireActivity().display?.getRealMetrics(displayMetrics)
        }
        return (displayMetrics.heightPixels * ratio).toInt()
    }

    inner class EditTextWatcher : TextWatcher, TextView.OnEditorActionListener {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
        override fun afterTextChanged(p0: Editable?) {}
        override fun onTextChanged(text: CharSequence?, p1: Int, p2: Int, p3: Int) {
            adapter.filter(text.toString())

            binding.btnComplete.isEnabled = text?.isNotBlank() == true
//                if (text.isNullOrBlank()) View.GONE
//                else View.VISIBLE

            hideExistsError()
        }

        override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                when (adapter.itemsForFiltered.size) {
                    0, 1 -> onClickCompleted()
                    else -> {}
                }
            }
            return false
        }
    }

    inner class SoftInputObserver : KeyboardHeightProvider.OnKeyboardListener {
        private val defaultBottomMargin = 56.dpToPixels(requireContext())

        override fun onHeightChanged(height: Int, isShowing: Boolean) {
            if (height == 0) {
                binding.bottomGuideLine.setGuidelineEnd(defaultBottomMargin)
            } else {
                binding.bottomGuideLine.setGuidelineEnd(height + defaultBottomMargin)
            }
        }
    }
}