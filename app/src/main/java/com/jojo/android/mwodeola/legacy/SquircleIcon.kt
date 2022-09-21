//package com.jojo.android.mwodeola.legacy
//
//import android.content.Context
//import android.content.pm.PackageManager
//import android.graphics.Color
//import android.graphics.ColorMatrix
//import android.graphics.ColorMatrixColorFilter
//import android.graphics.drawable.ColorDrawable
//import android.graphics.drawable.Drawable
//import android.graphics.drawable.GradientDrawable
//import android.util.AttributeSet
//import android.util.TypedValue
//import android.view.Gravity
//import android.view.View
//import android.view.ViewGroup.LayoutParams.MATCH_PARENT
//import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
//import android.widget.FrameLayout
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.annotation.ColorInt
//import androidx.annotation.DrawableRes
//import androidx.core.content.res.ResourcesCompat
//import androidx.core.view.setPadding
//import com.jojo.android.mwodeola.R
//import com.jojo.android.mwodeola.data.account.Account
//import com.jojo.android.mwodeola.data.account.AccountGroup
//
//
//class SquircleIcon @JvmOverloads constructor(
//    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0, defStyleRes: Int = 0
//) : FrameLayout(context, attrs, defStyleAttr, defStyleRes) {
//
//    companion object {
//        const val TAG = "SquircleIcon"
//
//        const val NO_ID = 0
//
//        const val ICON_TEXT_DEFAULT = "?"
//        const val ICON_TEXT_SIZE_DEFAULT = 36f
//        const val ICON_TEXT_COLOR_DEFAULT = Color.WHITE
//        const val ICON_TEXT_BACKGROUND_COLOR_DEFAULT = Color.GREEN
//
//        const val BADGE_SIZE_DEFAULT = 26 // DP
//        const val BADGE_PADDING_DEFAULT = 6 // DP
//
//        val COLOR_ORANGE = Color.rgb(255, 167, 38)
//
//        val BADGE_BACKGROUND = GradientDrawable().apply {
//            shape = GradientDrawable.OVAL
//            cornerRadius = 15f
//            setColor(Color.WHITE)
//            setStroke(1, Color.LTGRAY)
//        }
//
//        private val GRAY_SCALE_COLOR = Color.rgb(204, 204, 204)
//        private val GRAY_SCALE_COLOR_FILTER =
//            ColorMatrixColorFilter(ColorMatrix().apply {
//                setSaturation(0f)
//            })
//    }
//
//    private val imageIconView: SquircleImageView
//    private val textIconView: TextView
//    private var maskView: SquircleImageView? = null
//    private var badgeView: ImageView? = null
//
//    var iconText: String
//        get() = textIconView.text.toString()
//        set(value) { setIconText(value) }
//
//    var iconImageDrawable: Drawable?
//        get() = imageIconView.drawable
//        set(value) { setIconImageDrawable(value) }
//
//    val isVisibleMaskView: Boolean
//        get() = maskView?.visibility == View.VISIBLE
//    val maskViewVisibility: Int
//        get() = maskView?.visibility ?: View.GONE
//
//    val isVisibleBadge: Boolean
//        get() = badgeView?.visibility == View.VISIBLE
//
//    init {
//        context.obtainStyledAttributes(attrs, R.styleable.SquircleIcon, defStyleAttr, defStyleRes).run {
//            val text = getString(R.styleable.SquircleIcon_icon_text)
//            val textSize = getDimension(R.styleable.SquircleIcon_icon_text_size, ICON_TEXT_SIZE_DEFAULT)
//            val textColor = getColor(R.styleable.SquircleIcon_icon_text_color, ICON_TEXT_COLOR_DEFAULT)
//            val textIconBgColor =
//                getColor(R.styleable.SquircleIcon_icon_text_backgroundColor, COLOR_ORANGE)
//
//            val iconImgDrawableRes = getResourceId(R.styleable.SquircleIcon_icon_imgSrc, NO_ID)
//            val maskViewDrawableRes = getResourceId(R.styleable.SquircleIcon_maskView, NO_ID)
//            val maskViewBackgroundColor =
//                getColor(R.styleable.SquircleIcon_maskView_backgroundColor, Color.TRANSPARENT)
//            val badgeDrawableRes = getResourceId(R.styleable.SquircleIcon_badge, NO_ID)
//
//            imageIconView = createSquircleImageView(iconImgDrawableRes, textIconBgColor)
//            textIconView = createTextIconView(text, textSize, textColor)
//
//            if (maskViewDrawableRes != NO_ID) {
//                maskView = createSquircleImageView(maskViewDrawableRes, Color.TRANSPARENT)
//                maskView!!.squircleBackgroundColor = maskViewBackgroundColor
//                maskView!!.visibility = View.GONE
//            }
//
//            if (badgeDrawableRes != NO_ID) {
//                badgeView = createBadgeView(badgeDrawableRes)
//            }
//
//            recycle()
//        }
//
//        addView(imageIconView)
//        addView(textIconView)
//        maskView?.let { addView(it) }
//        badgeView?.let { addView(it) }
//    }
//
//    @JvmName("setIconText1")
//    fun setIconText(title: String?) {
//        textIconView.text = title.firstOrDefault(ICON_TEXT_DEFAULT)
//        textIconView.visibility = View.VISIBLE
//        setIconBackgroundColor(COLOR_ORANGE)
//    }
//
//    fun setIconBackgroundColor(@ColorInt color: Int) {
//        imageIconView.setImageDrawable(ColorDrawable(color))
//        textIconView.visibility = View.VISIBLE
//    }
//
//    fun setIconImageSource(@DrawableRes resId: Int) {
//        imageIconView.setImageResource(resId)
//        textIconView.visibility = View.GONE
//    }
//
//    @JvmName("setIconImageDrawable1")
//    fun setIconImageDrawable(drawable: Drawable?) {
//        imageIconView.setImageDrawable(drawable)
//        textIconView.visibility = View.GONE
//    }
//
//    fun setGrayScale(enabled: Boolean) {
//        if (enabled) {
//            imageIconView.colorFilter = GRAY_SCALE_COLOR_FILTER
//            imageIconView.alpha = 0.3f
//            textIconView.setTextColor(GRAY_SCALE_COLOR)
//            textIconView.alpha = 0.3f
//        } else {
//            imageIconView.clearColorFilter()
//            imageIconView.colorFilter = null
//            imageIconView.alpha = 1f
//            textIconView.setTextColor(Color.WHITE)
//            textIconView.alpha = 1f
//        }
//    }
//
//    fun setIconBy(account: Account) {
//        if (account.isSnsAccount) {
//            setSnsGroupIcon(account.sns_group!!.sns)
//        } else {
//            if (account.own_group.isSnsGroup) {
//                setSnsGroupIcon(account.own_group.sns)
//            } else {
//                setNormalGroupIcon(account.own_group)
//            }
//        }
//    }
//
//    fun setIconByAccountGroup(account: Account) {
//        if (account.own_group.isOwnGroup) {
//            setNormalGroupIcon(account.own_group)
//        } else {
//            setSnsGroupIcon(account.own_group.sns)
//        }
//    }
//
//    fun setSnsGroupIcon(snsId: Int) {
//        val resId = when (snsId) {
//            1 -> R.drawable.sns_naver_icon
//            2 -> R.drawable.sns_kakao_icon
//            3 -> R.drawable.sns_line_icon
//            4 -> R.drawable.sns_google_icon_white_320
//            5 -> R.drawable.sns_facebook_icon
//            6 -> R.drawable.sns_twitter_icon
//            else -> NO_ID
//        }
//        if (resId != NO_ID) {
//            setIconImageSource(resId)
////            if (snsId == 4) {
////                imageIconView.borderWidth = 3
////                imageIconView.borderColor = Color.BLACK
////            } else {
////                imageIconView.borderWidth = 0
////            }
//        }
//    }
//
//    fun setNormalGroupIcon(group: AccountGroup) {
//        when (group.icon_type) {
//            AccountGroup.ICON_TYPE_TEXT ->
//                setIconText(group.group_name)
//            AccountGroup.ICON_TYPE_IMAGE ->
//                setIconText(group.group_name)
//            AccountGroup.ICON_TYPE_INSTALLED_APP_LOGO -> {
//                val installedAppIcon = getInstalledAppIcon(group.app_package_name)
//                if (installedAppIcon != null) {
//                    setIconImageDrawable(installedAppIcon)
//                } else {
//                    setIconText(group.group_name)
//                }
//            }
//        }
//    }
//
//    fun setMaskView(@DrawableRes resId: Int) {
//        setMaskView(ResourcesCompat.getDrawable(resources, resId, null))
//    }
//
//    fun setMaskView(drawable: Drawable?) {
//        if (maskView == null) {
//            maskView = createSquircleImageView(drawable, null)
//            addView(maskView, 2)
//        }
//        maskView!!.setImageDrawable(drawable)
//        showMaskView()
//    }
//
//    fun setMaskView(drawable: Drawable? ,@ColorInt bgColor: Int) {
//        setMaskView(drawable)
//        maskView!!.squircleBackgroundColor = bgColor
//    }
//
//    fun removeMaskView() {
//        removeView(maskView)
//        maskView = null
//    }
//
//    fun showMaskView() { maskView?.visibility = View.VISIBLE }
//    fun hideMaskView() { maskView?.visibility = View.GONE }
//
//    fun setBadgeView(@DrawableRes resId: Int) {
//        if (badgeView == null) {
//            badgeView = createBadgeView(resId)
//            addView(badgeView)
//        }
//        badgeView!!.setImageResource(resId)
//    }
//
//    fun removeBadgeView() {
//        removeView(badgeView)
//        badgeView = null
//    }
//
//    fun showBadge() { badgeView?.visibility = View.VISIBLE }
//    fun hideBadge() { badgeView?.visibility = View.GONE }
//
//    private fun createSquircleImageView(@DrawableRes imgResId: Int,
//                                        @ColorInt bgColor: Int) : SquircleImageView {
//        val imgDrawable =
//            if (imgResId == NO_ID) null
//            else ResourcesCompat.getDrawable(resources, imgResId, null)
//        val bgColorDrawable = ColorDrawable(bgColor)
//
//        return createSquircleImageView(imgDrawable, bgColorDrawable)
//    }
//
//    private fun createSquircleImageView(imgDrawable: Drawable?,
//                                        bgColorDrawable: Drawable?): SquircleImageView =
//        SquircleImageView(context).also {
//            it.layoutParams = LayoutParams(MATCH_PARENT, MATCH_PARENT)
//            it.setPadding(1.dpToPixels(context))
//            it.setImageDrawable(imgDrawable ?: bgColorDrawable)
////            it.setSquircleBackgroundColorResource(R.color.orange400) 안됨
////            it.squircleBackgroundColor = Color.BLUE 안됨
//        }
//
//    private fun createTextIconView(title: String?, textSize: Float, @ColorInt textColor: Int): TextView =
//        TextView(context).also {
//            it.layoutParams = LayoutParams(WRAP_CONTENT, WRAP_CONTENT).apply {
//                gravity = Gravity.CENTER
//            }
//            it.text = title.firstOrDefault(ICON_TEXT_DEFAULT)
//            it.textSize = textSize
//            it.setTextColor(textColor)
//            it.visibility =
//                if (title != null) View.VISIBLE
//                else View.GONE
//        }
//
//    private fun createBadgeView(@DrawableRes resId: Int): ImageView =
//        ImageView(context).also {
//            val badgeSize = BADGE_SIZE_DEFAULT.dpToPixels(context)
//
//            it.layoutParams = LayoutParams(badgeSize, badgeSize).also { params ->
//                params.gravity = Gravity.BOTTOM or Gravity.END
//            }
//
//            it.setPadding(BADGE_PADDING_DEFAULT.dpToPixels(context))
//            it.setImageDrawable(ResourcesCompat.getDrawable(resources, resId, null))
//            it.background = BADGE_BACKGROUND
//        }
//
//    private fun getInstalledAppIcon(packageName: String?): Drawable? {
//        if (packageName == null)
//            return null
//
//        return try {
//            context.packageManager.getApplicationIcon(packageName)
//        } catch (e: PackageManager.NameNotFoundException) {
//            null
//        }
//    }
//
//    private fun Int.dpToPixels(context: Context): Int =
//        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this.toFloat(), context.resources.displayMetrics).toInt()
//
//    private fun Float.dpToPixels(context: Context): Float =
//        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this, context.resources.displayMetrics)
//
//    private fun String?.firstOrDefault(defaultValue: String): String =
//        if (this.isNullOrBlank()) defaultValue
//        else first().toString()
//}