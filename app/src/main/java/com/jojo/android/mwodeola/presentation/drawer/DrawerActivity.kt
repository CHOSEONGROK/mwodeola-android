package com.jojo.android.mwodeola.presentation.drawer

import android.animation.ObjectAnimator
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.CheckBox
import android.widget.FrameLayout
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.databinding.ActivityDrawerBinding
import com.jojo.android.mwodeola.databinding.ActivityDrawerContentBinding
import com.jojo.android.mwodeola.presentation.BaseActivity
import com.jojo.android.mwodeola.presentation.BaseFragment
import com.jojo.android.mwodeola.presentation.account.datalist.AccountGroupListFragment
import com.jojo.android.mwodeola.presentation.common.IncubatableFloatingActionButton
import com.jojo.android.mwodeola.presentation.drawer.interactive.DrawerContent
import com.jojo.android.mwodeola.presentation.drawer.interactive.DrawerOwner
import com.jojo.android.mwodeola.presentation.drawer.motion.MotionHelper
import com.jojo.android.mwodeola.presentation.settings.SettingsActivity

class DrawerActivity : BaseActivity(), DrawerContract.View, DrawerOwner {

    companion object {
        private const val TAG = "DrawerActivity"

        const val EXTRA_CONTENT_TYPE = "extra_CONTENT"

        const val NONE = -1
        const val CONTENT_ACCOUNT = 0
        const val CONTENT_CREDIT_CARD = 1
    }

    override val isScreenLockEnabled: Boolean = true
    override val binding by lazy { ActivityDrawerBinding.inflate(layoutInflater) }

    override var drawerContent: DrawerContent? = null

    override var darkThemeColor = 0
    override var lightThemeColor = 0

    override var title: String
        get() = contentBinding.title.text.toString()
        set(value) {
            contentBinding.titleLargeSizingHelper.text = value
            contentBinding.titleSmallSizingHelper.text = value
            contentBinding.title.text = value
        }

    override var subtitle: String
        get() = contentBinding.subtitle.text.toString()
        set(value) {
            contentBinding.subtitleLargeSizingHelper.text = value
            contentBinding.subtitleSmallSizingHelper.text = value
            contentBinding.subtitle.text = value
        }

    override val swipeRefreshLayout: SwipeRefreshLayout
        get() = contentBinding.swipeRefreshLayout

    override val checkBoxSelectAllContainer: FrameLayout
        get() = contentBinding.checkBoxSelectAllContainer

    override val checkBoxSelectAll: CheckBox
        get() = contentBinding.checkBoxSelectAll

    override val incubatableFab: IncubatableFloatingActionButton
        get() = contentBinding.fab

    private val contentBinding: ActivityDrawerContentBinding
        get() = binding.layoutDrawerContent

    private val drawerMenuAdapter by lazy { DrawerMenuAdapter(this, binding, binding.layoutDrawerMenu) }

    private var motionHelper: MotionHelper? = null

    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val appThemeColor by lazy { ResourcesCompat.getColor(resources, R.color.app_theme_color, null) }

    private val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        drawerContent?.onActivityResult(it)
    }

    /**
     * [BaseActivity]'s override functions
     * */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initWindowSettings()
        initView()
        handleIntent()
    }

    override fun onBackPressed() {
        when {
            binding.drawerLayout.isOpen -> {
                binding.drawerLayout.close()
            }
            incubatableFab.isActivate -> {
                incubatableFab.close()
            }
            else -> {
                val intercepted = drawerContent?.onBackPressed() ?: false
                if (intercepted.not()) {
                    super.onBackPressed()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        motionHelper?.close()
        motionHelper = null
    }

    /**
     * [DrawerContract.View]'s override functions
     * */
    override fun setDarkTheme() = with(contentBinding) {
        updateSystemBarTheme(true)

        checkBoxSelectAll.buttonTintList = ColorStateList.valueOf(Color.WHITE)
        checkBoxSelectAllLabel.setTextColor(Color.WHITE)

        drawerMenuAdapter.setDarkTheme()
    }

    override fun setLightTheme() = with(contentBinding) {
        updateSystemBarTheme(false)

        checkBoxSelectAll.buttonTintList = ColorStateList.valueOf(darkThemeColor)
        checkBoxSelectAllLabel.setTextColor(darkThemeColor)

        drawerMenuAdapter.setLightTheme()
    }

    override fun updateBackgroundColor(progress: Float, backgroundColor: Int, textColor: Int) {
        binding.drawerLayout.setBackgroundColor(backgroundColor)

        with(contentBinding) {
            val colorTint = ColorStateList.valueOf(textColor)

            title.setTextColor(textColor)
            subtitle.setTextColor(textColor)

            drawerToggle.drawerArrowDrawable.color = textColor
            btnDelete.imageTintList = colorTint
            btnSearch.imageTintList = colorTint
            btnBackDrop.imageTintList = colorTint
        }
    }

    override fun changeDayOrNightTheme(isNightTheme: Boolean) {
        val dayThemeColor = ResourcesCompat.getColor(resources, R.color.day_theme_color, null)
        val nightThemeColor = ResourcesCompat.getColor(resources, R.color.night_theme_color, null)

        val colorFrom = if (isNightTheme) dayThemeColor else nightThemeColor
        val colorTo = if (isNightTheme) nightThemeColor else dayThemeColor

        if (drawerMenuAdapter.isDarkTheme) {
            ObjectAnimator.ofArgb(binding.drawerLayout, "backgroundColor", colorFrom, colorTo).apply {
                interpolator = AccelerateDecelerateInterpolator()
                duration = 200
            }.start()
        }

        darkThemeColor = colorTo

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            window.navigationBarColor = darkThemeColor
        }
    }

    override fun startSettingsActivity() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    override fun finishActivity() {
        finish()
    }

    /**
     * [DrawerOwner]'s override functions
     * */
    override fun startSelectionMode() = with(contentBinding) {
        coordinatorLayout.isRefreshEnabled = false
        drawerToggle.isDrawerIndicatorEnabled = false
        drawerToggle.syncState()

        checkBoxSelectAllContainer.isVisible = true

        btnDelete.isVisible = false
        btnSearch.isVisible = false
        btnBackDrop.isVisible = false
        subtitle.isVisible = false
        incubatableFab.hide()
    }

    override fun cancelSelectionMode() = with(contentBinding) {
        coordinatorLayout.isRefreshEnabled = true
        drawerToggle.isDrawerIndicatorEnabled = true
        drawerToggle.syncState()

        checkBoxSelectAllContainer.isVisible = false
        checkBoxSelectAll.isChecked = false

        btnDelete.isVisible = true
        btnSearch.isVisible = true
        btnBackDrop.isVisible = true
        subtitle.isVisible = true
        incubatableFab.show()
    }

    override fun launchActivityForResult(cls: Class<*>, intent: Intent?) {
        val intent2 = intent?.setClass(this, cls)
            ?: Intent(this, cls)

        launcher.launch(intent2)
    }

    private fun initView() {
        val activity = this@DrawerActivity

        darkThemeColor = ResourcesCompat.getColor(resources, R.color.day_theme_color, null)
        lightThemeColor = ResourcesCompat.getColor(resources, R.color.gray200, null)

        with (binding) {
            drawerLayout.setScrimColor(Color.argb(68, 0, 0, 0))
            drawerLayout.drawerElevation = 0f
        }

        with (contentBinding) {
            motionHelper = MotionHelper(activity, binding.drawerLayout, toolBarLayout)
            motionHelper?.init()

            setSupportActionBar(toolBar)
            supportActionBar?.title = null

            drawerToggle = ActionBarDrawerToggle(
                activity, binding.drawerLayout, toolBar, R.string.drawer_open, R.string.drawer_close
            ).also {
                binding.drawerLayout.addDrawerListener(it)

                it.drawerArrowDrawable.color = Color.WHITE
                it.syncState()
            }

            btnDelete.setOnClickListener {
                drawerContent?.sharedWidgetsListener?.onDeleteClicked(it)
            }
            btnSearch.setOnClickListener {
                drawerContent?.sharedWidgetsListener?.onSearchClicked(it)
            }
            btnBackDrop.setOnClickListener {
                drawerContent?.sharedWidgetsListener?.onFilterClicked(btnBackDrop)
            }
        }

        drawerMenuAdapter.initView()
    }

    private fun handleIntent() {
        val fragment: BaseFragment = when (intent.getIntExtra(EXTRA_CONTENT_TYPE, NONE)) {
            CONTENT_ACCOUNT -> {
                binding.layoutDrawerMenu.toggleAccount.isChecked = true
                AccountGroupListFragment()
            }
            CONTENT_CREDIT_CARD -> {
                binding.layoutDrawerMenu.toggleCreditCard.isChecked = true
                null
            }
            else -> null
        } ?: return

        drawerContent = fragment as DrawerContent

        supportFragmentManager.beginTransaction()
            .replace(contentBinding.fragmentContainer.id, fragment)
            .commitAllowingStateLoss()
//            .commit()
    }

    private fun initWindowSettings() {
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = Color.TRANSPARENT

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
            window.navigationBarColor = Color.TRANSPARENT
        } else {
            window.navigationBarColor = darkThemeColor
        }

        updateSystemBarTheme(true)

        ViewCompat.setOnApplyWindowInsetsListener(contentBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

//            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
//                topMargin = insets.top
//                bottomMargin = insets.bottom
//            }

            with (contentBinding) {
                topGuideline.setGuidelineBegin(insets.top)
                bottomGuideline.setGuidelineEnd(insets.bottom)
            }

            binding.layoutDrawerMenu.drawerMenuContainer.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top
                bottomMargin = insets.bottom
            }

            WindowInsetsCompat.CONSUMED
        }
    }

    private fun updateSystemBarTheme(isDark: Boolean) {
        ViewCompat.getWindowInsetsController(window.decorView)?.let {
            it.isAppearanceLightStatusBars = !isDark
            it.isAppearanceLightNavigationBars = !isDark
        }
    }
}