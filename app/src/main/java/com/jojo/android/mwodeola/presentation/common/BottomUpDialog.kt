package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.DialogBottomUpBinding
import com.jojo.android.mwodeola.databinding.DialogBottomUpNoMaterialBinding

class BottomUpDialog private constructor(
    private val type: Type,
    private val title: String? = null,
    private val subTitle: String? = null,
    private val positiveButtonLabel: String? = null,
    private val positiveButtonListener: Runnable? = null,
    private val negativeButtonLabel: String? = null,
    private val negativeButtonListener: Runnable? = null,
    private val confirmButtonLabel: String? = null,
    private val confirmButtonListener: Runnable? = null
) : DialogFragment() {

    enum class Type {
        ONE_BUTTON, TWO_BUTTON
    }

//    private val binding by lazy { DialogBottomUpBinding.inflate(layoutInflater) }
    private val binding by lazy { DialogBottomUpNoMaterialBinding.inflate(layoutInflater) }

    private var isPositive = false
    private var isNegative = false
    private var isConfirmed = false

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?, savedInstanceState: Bundle?): View {
        dialog?.window?.run {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            requestFeature(Window.FEATURE_NO_TITLE)

            attributes.let {
                it.width = WindowManager.LayoutParams.MATCH_PARENT
                it.height = WindowManager.LayoutParams.MATCH_PARENT

                it.windowAnimations = R.style.BottomUpDialogAnimation
            }
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
    }

    fun initView(): Unit = with(binding) {
        root.clipToOutline = true
        container.clipToOutline = true

        if (title != null) {
            tvTitle.text = title
        } else {
            tvTitle.visibility = View.GONE
        }

        if (subTitle != null) {
            tvSubtitle.text = subTitle
        } else {
            tvSubtitle.visibility = View.GONE
        }


        when (type) {
            Type.ONE_BUTTON -> {
                btnPositive.text = confirmButtonLabel
                btnPositive.setOnClickListener {
                    isConfirmed = true
                    dismiss()
                }
                btnNegative.visibility = View.GONE
            }
            Type.TWO_BUTTON -> {
                btnPositive.text = positiveButtonLabel
                btnPositive.setOnClickListener {
                    isPositive = true
                    dismiss()
                }

                btnNegative.text = negativeButtonLabel
                btnNegative.setOnClickListener {
                    isNegative = true
                    dismiss()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(320.dpToPixels(requireContext()), WindowManager.LayoutParams.WRAP_CONTENT)
    }

    override fun onDetach() {
        super.onDetach()
        when (type) {
            Type.ONE_BUTTON ->
                confirmButtonListener?.run()
            Type.TWO_BUTTON ->
                if (isPositive)
                    positiveButtonListener?.run()
                else
                    negativeButtonListener?.run()
        }
    }

    private fun Int.dpToPixels(context: Context): Int =
        (this * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

    class Builder(private val fragmentManager: FragmentManager) {
        private var title: String? = null
        private var subtitle: String? = null

        private var positiveButtonLabel: String? = null
        private var positiveButtonListener: Runnable? = null

        private var negativeButtonLabel: String? = null
        private var negativeButtonListener: Runnable? = null

        private var confirmButtonLabel: String? = null
        private var confirmButtonListener: Runnable? = null

        fun title(title: String) = also { this.title = title }
        fun subtitle(subtitle: String) = also { this.subtitle = subtitle }

        fun positiveButton(label: String? = "예", listener: Runnable? = null) = also {
            this.positiveButtonLabel = label
            this.positiveButtonListener = listener
            negativeButton("아니오")
        }

        fun negativeButton(label: String? = "아니오", listener: Runnable? = null) = also {
            this.negativeButtonLabel = label
            this.negativeButtonListener = listener
        }

        fun confirmedButton(label: String? = "확인", listener: Runnable? = null) = also {
            this.confirmButtonLabel = "확인"
            this.confirmButtonListener = listener

            this.positiveButtonLabel = null
            this.positiveButtonListener = null
            this.negativeButtonLabel = null
            this.negativeButtonListener = null
        }

        fun show() {
            if (isNotValid())
                return

            val type = if (positiveButtonLabel != null) {
                Type.TWO_BUTTON
            } else {
                Type.ONE_BUTTON
            }

            BottomUpDialog(
                type,
                title, subtitle,
                positiveButtonLabel, positiveButtonListener,
                negativeButtonLabel, negativeButtonListener,
                confirmButtonLabel, confirmButtonListener
            ).show(fragmentManager, null)
        }

        private fun isNotValid(): Boolean =
            (title == null && subtitle == null) &&
                    (positiveButtonLabel == null || confirmButtonLabel == null)
    }
}