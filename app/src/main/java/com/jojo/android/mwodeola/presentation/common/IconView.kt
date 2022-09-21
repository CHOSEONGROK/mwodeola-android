package com.jojo.android.mwodeola.presentation.common

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.core.widget.TextViewCompat
import androidx.core.widget.TextViewCompat.AUTO_SIZE_TEXT_TYPE_UNIFORM
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.data.account.Account
import com.jojo.android.mwodeola.data.account.AccountGroup

class IconView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr, defStyleRes) {

    private val iconView: OvalShapeImageView
    private val textView: TextView

    private var textIconBackground: Drawable

    init {
        context.obtainStyledAttributes(attrs, R.styleable.IconView, defStyleAttr, defStyleRes).use { typed ->
            // init TextView
            textView = TextView(context, attrs, defStyleAttr).also {
                it.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
                it.gravity = Gravity.CENTER
                TextViewCompat.setAutoSizeTextTypeWithDefaults(it, AUTO_SIZE_TEXT_TYPE_UNIFORM)

                it.text = typed.getString(R.styleable.IconView_icon_text).firstOrDefault()
                it.setTextColor(typed.getColor(R.styleable.IconView_icon_text_color, Color.WHITE))
                textIconBackground = typed.getDrawable(R.styleable.IconView_icon_text_background)
                    ?: TypedValue().let { value ->
                        context.theme.resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true)
                        ColorDrawable(value.data)
                    }
            }

            val iconImage = typed.getDrawable(R.styleable.IconView_icon_image_src)
            val ovalOffset = typed.getFloat(R.styleable.IconView_icon_oval_offset, 2.7f)
            val ovalShapeEnable = typed.getBoolean(R.styleable.IconView_icon_oval_shape_enable, true)
            val ovalAutoStrokeEnable = typed.getBoolean(R.styleable.IconView_icon_oval_auto_stroke_enable, true)

            // init OvalShapeImageView
            iconView = OvalShapeImageView(
                context, ovalOffset, ovalShapeEnable, ovalAutoStrokeEnable
            ).also {
                it.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

                if (iconImage != null) {
                    it.setImageDrawable(iconImage)
                    textView.isVisible = false
                } else {
                    it.setImageDrawable(textIconBackground)
                    textView.isVisible = true
                }
            }
        }

        addView(iconView)
        addView(textView)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed) {
            textView.updateLayoutParams<MarginLayoutParams> {
                setMargins((right - left) / 6)
            }
        }
    }

    fun setText(text: String?) {
        textView.text = text.firstOrDefault()
        textView.isVisible = true
        iconView.setImageDrawable(textIconBackground)
    }

    fun setTextColor(@ColorInt color: Int) {
        textView.setTextColor(color)
        textView.isVisible = true
        iconView.setImageDrawable(textIconBackground)
    }

    fun setTextIconBackground(@ColorInt color: Int) {
        setTextIconBackground(ColorDrawable(color))
    }

    fun setTextIconBackground(drawable: Drawable?) {
        if (drawable == null)
            return

        textIconBackground = drawable
        iconView.setImageDrawable(drawable)
        textView.isVisible = true
    }

    fun setIconImage(drawable: Drawable?) {
        iconView.setImageDrawable(drawable)
        textView.isVisible = false
    }

    fun setIconImage(@DrawableRes resId: Int) {
        iconView.setImageResource(resId)
        textView.isVisible = false
    }

    fun setParentColor(@ColorInt color: Int) {
        iconView.setParentColor(color)
    }

    fun setGrayScale(enabled: Boolean) {
        if (enabled) {
            iconView.colorFilter = GRAY_SCALE_COLOR_FILTER
            iconView.alpha = 0.3f
            textView.setTextColor(GRAY_SCALE_COLOR)
            textView.alpha = 0.3f
        } else {
            iconView.clearColorFilter()
            iconView.colorFilter = null
            iconView.alpha = 1f
            textView.setTextColor(Color.WHITE)
            textView.alpha = 1f
        }
    }

    fun setIconByAccountGroup(account: Account) {
        if (account.own_group.isOwnGroup) {
            setNormalGroupIcon(account.own_group)
        } else {
            setSnsGroupIcon(account.own_group.sns)
        }
    }

    fun setSnsGroupIcon(snsId: Int) {
        when (snsId) {
            1 -> setIconImage(R.drawable.sns_naver_icon)
            2 -> setIconImage(R.drawable.sns_kakao_icon)
            3 -> setIconImage(R.drawable.sns_line_icon)
            4 -> setIconImage(R.drawable.sns_google_icon_white_320)
            5 -> setIconImage(R.drawable.sns_facebook_icon)
            6 -> setIconImage(R.drawable.sns_twitter_icon)
        }
    }

    fun setNormalGroupIcon(group: AccountGroup) {
        when (group.icon_type) {
            AccountGroup.ICON_TYPE_TEXT,
            AccountGroup.ICON_TYPE_IMAGE -> setText(group.group_name)
            AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO -> {
                val installedAppIcon = getInstalledAppIcon(group.app_package_name)
                if (installedAppIcon != null) {
                    setIconImage(installedAppIcon)
                } else {
                    setText(group.group_name)
                }
            }
        }
    }

    private fun getInstalledAppIcon(packageName: String?): Drawable? {
        if (packageName == null)
            return null

        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }
    }

    private fun String?.firstOrDefault(): String {
        return (this?.firstOrNull() ?: '?').toString()
    }

    private val Int.dp: Int get() {
        return (this * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()
    }

    companion object {
        private val GRAY_SCALE_COLOR = Color.rgb(204, 204, 204)
        private val GRAY_SCALE_COLOR_FILTER = ColorMatrixColorFilter(ColorMatrix().apply {
            setSaturation(0f)
        })
    }
}