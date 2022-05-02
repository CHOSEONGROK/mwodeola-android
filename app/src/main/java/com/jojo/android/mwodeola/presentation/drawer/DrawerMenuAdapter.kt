package com.jojo.android.mwodeola.presentation.drawer

import android.content.res.ColorStateList
import android.graphics.Color
import androidx.core.content.res.ResourcesCompat
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivityDrawerBinding
import com.jojo.android.mwodeola.databinding.ActivityDrawerMenuBinding

class DrawerMenuAdapter(
    private val view: DrawerContract.View,
    private val binding: ActivityDrawerBinding,
    private val menuBinding: ActivityDrawerMenuBinding
) {

    var isDarkTheme: Boolean = true
        private set

    private val minAlpha = menuBinding.cardView.alpha

    private val textColorGray = ResourcesCompat.getColor(menuBinding.root.resources, R.color.text_view_text_default_color, null)

    fun initView(): Unit = with(menuBinding) {
        btnSettings.setOnClickListener {
            binding.drawerLayout.close()
            view.startSettingsActivity()
        }
        switchDayOrNightTheme.setOnCheckedChangeListener { buttonView, isChecked ->
            buttonView.text = if (isChecked) "테마: 밤" else "테마: 낮"

            view.changeDayOrNightTheme(isChecked)
        }

        toggleAccount.isChecked = true

        toggleBankAccount.isCheckable = false
        togglePublicCertificate.isCheckable = false
        toggleSecurityCard.isCheckable = false
        toggleCreditCard.isCheckable = false
        toggleAddress.isCheckable = false
        toggleMemo.isCheckable = false

        toggleGroup.addOnButtonCheckedListener { group, checkedId, isChecked ->

        }

        btnHome.setOnClickListener {
            view.finishActivity()
        }
    }

    fun updateBackgroundAlpha(progress: Float) {
        menuBinding.cardView.alpha = minAlpha.coerceAtLeast(progress)
    }

    fun setDarkTheme(): Unit = with(menuBinding) {
        if (isDarkTheme)
            return@with

        isDarkTheme = true

        val color = Color.WHITE
        val colorTint = ColorStateList.valueOf(color)

        menuBinding.cardView.alpha = minAlpha


        categoryLabel.setTextColor(color)
        toggleAccount.setTextColor(color)
        toggleAccount.iconTint = colorTint
        toggleBankAccount.setTextColor(color)
        toggleBankAccount.iconTint = colorTint
        togglePublicCertificate.setTextColor(color)
        togglePublicCertificate.iconTint = colorTint
        toggleSecurityCard.setTextColor(color)
        toggleSecurityCard.iconTint = colorTint
        toggleCreditCard.setTextColor(color)
        toggleCreditCard.iconTint = colorTint
        toggleAddress.setTextColor(color)
        toggleAddress.iconTint = colorTint
        toggleMemo.setTextColor(color)
        toggleMemo.iconTint = colorTint

        btnSettings.imageTintList = colorTint
        switchDayOrNightTheme.setTextColor(color)
        btnHome.setTextColor(color)
        btnHome.setBackgroundColor(ResourcesCompat.getColor(menuBinding.root.resources, R.color.white_a25, null))
    }

    fun setLightTheme(): Unit = with(menuBinding) {
        if (isDarkTheme.not())
            return@with

        isDarkTheme = false

        val color = Color.BLACK
        val colorTint = ColorStateList.valueOf(color)

        menuBinding.cardView.alpha = 1f

        categoryLabel.setTextColor(color)
        toggleAccount.setTextColor(color)
        toggleAccount.iconTint = colorTint
        toggleBankAccount.setTextColor(color)
        toggleBankAccount.iconTint = colorTint
        togglePublicCertificate.setTextColor(color)
        togglePublicCertificate.iconTint = colorTint
        toggleSecurityCard.setTextColor(color)
        toggleSecurityCard.iconTint = colorTint
        toggleCreditCard.setTextColor(color)
        toggleCreditCard.iconTint = colorTint
        toggleAddress.setTextColor(color)
        toggleAddress.iconTint = colorTint
        toggleMemo.setTextColor(color)
        toggleMemo.iconTint = colorTint

        btnSettings.imageTintList = ColorStateList.valueOf(textColorGray)
        switchDayOrNightTheme.setTextColor(color)
        btnHome.setTextColor(color)
        btnHome.setBackgroundColor(ResourcesCompat.getColor(menuBinding.root.resources, R.color.gray200, null))
    }
}