package com.jojo.android.mwodeola.presentation.security.screenLock

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import androidx.biometric.BiometricManager
import androidx.fragment.app.FragmentActivity
import com.jojo.android.mwodeola.R
import com.jojo.android.mwodeola.presentation.common.BottomUpDialog
import com.jojo.android.mwodeola.presentation.security.AuthType
import com.jojo.android.mwodeola.presentation.security.BiometricHelper
import com.jojo.android.mwodeola.presentation.security.SecurityManager
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.APP_CREDENTIAL
import com.jojo.android.mwodeola.presentation.security.SecurityManager.SharedPref.Companion.DEVICE_CREDENTIAL
import jp.wasabeef.blurry.Blurry
import java.util.*
import kotlin.concurrent.timer

class ScreenLockHandler(
    private val context: Context
) : Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    companion object {
        private const val TAG = "ScreenLockHandler"

        var blurSnapshot: Bitmap? = null
        var signUpFlag: Boolean = false
        var signInFlag: Boolean = false
    }

    private val securitySharedPref = SecurityManager.SharedPref(context)
    private val screenLockTimeOut: Int
        get() = securitySharedPref.screenLockTimeout()

    private val backgroundTimer = BackgroundTimer()

    // configuration changed(화면 회전, 팝업, 분할 등) -> screen lock 방지
    private var configurationChangedFlag: Boolean = false

    private var firstSubscriber: Class<*>? = null
    private var firstSubscriberCreatedFlag = false

    fun setFirstSubscriber(subscriber: Class<*>) {
        firstSubscriber = subscriber
    }

    fun onTerminate() {
        blurSnapshot = null
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityCreated()]")

        if (signUpFlag || signInFlag || configurationChangedFlag)
            return

        if (activity.javaClass == firstSubscriber && activity is ScreenLockSubscriber) {
            firstSubscriberCreatedFlag = true

            activity.binding.root.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    createBlurSnapshot(activity.binding.root.rootView)
                    executeScreenLock(activity)
                    activity.binding.root.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
        }
    }

    override fun onActivityStarted(activity: Activity) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityStarted()]")
    }

    override fun onActivityResumed(activity: Activity) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityResumed()] fromBackground=${backgroundTimer.fromBackground}, isTimeOut=${backgroundTimer.isTimeOut}")

        if (backgroundTimer.fromBackground && activity is ScreenLockSubscriber) {
            activity.onResumeFromBackground()
        }

        if (signUpFlag || signInFlag || configurationChangedFlag || firstSubscriberCreatedFlag) {
            when {
                signUpFlag -> signUpFlag = false
                signInFlag -> signInFlag = false
                configurationChangedFlag -> configurationChangedFlag = false
                firstSubscriberCreatedFlag -> firstSubscriberCreatedFlag = false
            }
            return
        }

        if (backgroundTimer.fromBackground) { // App Background -> Foreground
            // ScreenLock 발동 조건
            if (backgroundTimer.isTimeOut &&
                securitySharedPref.isScreenLockEnabled() &&
                activity is ScreenLockSubscriber &&
                activity.isScreenLockEnabled
            ) {
                // ScreenLockBlurActivity 의 Background blur image 를 위한 스냅샷
                createBlurSnapshot(activity.binding.root.rootView)
                executeScreenLock(activity)
            } else if (
                activity is ScreenLockSubscriber &&
                activity.isScreenLockEnabled
            ) {
                // Background 에서 왔지만 ScreenLock 발동 조건을 만족하지 못한 상태에서
                // 새로운 지문이 추가됐을 때, Biometric enroll 에 대해 검사 및 설정을 변경함.
                checkBiometricEnroll(activity as? FragmentActivity)
            }

            backgroundTimer.stop()
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityPaused()]")
    }

    override fun onActivityStopped(activity: Activity) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityStopped()]")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivitySaveInstanceState()]: outState=$outState")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log.i(TAG, "[${activity.javaClass.simpleName}::onActivityDestroyed()]")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.i(TAG, "onConfigurationChanged(), activity=$newConfig")
        configurationChangedFlag = true
    }

    override fun onLowMemory() {
        Log.i(TAG, "onLowMemory()")
    }

    override fun onTrimMemory(level: Int) {
        Log.i(TAG, "onTrimMemory(), level=$level")
        if (configurationChangedFlag) {
            return
        }

        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            Log.w(TAG, "onTrimMemory(), level=TRIM_MEMORY_UI_HIDDEN")
            val isScreenLockEnabled = securitySharedPref.isScreenLockEnabled()
            backgroundTimer.start(screenLockTimeOut, isScreenLockEnabled)

        } else if (level == ComponentCallbacks2.TRIM_MEMORY_BACKGROUND) {
            Log.w(TAG, "onTrimMemory(), level=TRIM_MEMORY_BACKGROUND")
        }
    }

    private fun executeScreenLock(activity: Activity) {
        activity.startActivity(Intent(activity, ScreenLockBlurActivity::class.java))
        activity.overridePendingTransition(R.anim.activity_fade_in, R.anim.activity_fade_out)
    }

    private fun createBlurSnapshot(view: View) {
        Blurry.with(view.context)
            .radius(25)
            .sampling(5)
            .capture(view)
            .getAsync { blurSnapshot = it }
    }

    private fun checkBiometricEnroll(fragmentActivity: FragmentActivity?) {
        if (securitySharedPref.isExistsPassword(AuthType.BIOMETRIC)) {

            if (BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_SUCCESS &&
                BiometricHelper.hasNewBiometricEnrolled
            ) { // 지문 정보가 새로 추가된 경우
                deleteBiometricAndShowDialog(fragmentActivity)
            } else if (
                BiometricHelper.canAuthentication == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
            ) { // 지문 정보가 모두 삭제된 경우
                deleteBiometricAndShowDialog(fragmentActivity)
                if (securitySharedPref.screenLockCredential() == DEVICE_CREDENTIAL) {
                    securitySharedPref.screenLockCredential(APP_CREDENTIAL)
                }
            } else if (
                !BiometricHelper.isAuthentication
            ) { // 기타 이유로 지문 인증이 불가능해진 경우
                deleteBiometricAndShowDialog(fragmentActivity)
                if (securitySharedPref.screenLockCredential() == DEVICE_CREDENTIAL) {
                    securitySharedPref.screenLockCredential(APP_CREDENTIAL)
                }
            }
        }
    }

    private fun deleteBiometricAndShowDialog(fragmentActivity: FragmentActivity?) {
        securitySharedPref.deletePassword(AuthType.BIOMETRIC)

        if (securitySharedPref.authType() == AuthType.BIOMETRIC) {
            securitySharedPref.authType(AuthType.PIN_5)
        }

        if (fragmentActivity != null) {
            BottomUpDialog.Builder(fragmentActivity.supportFragmentManager)
                .title("생체 인증 정보 변경됨")
                .subtitle("휴대폰의 생체 인증 정보가 변경되어 앱에 등록되었던 지문 인증은 사라집니다.")
                .confirmedButton()
                .show()
        }
    }

    class BackgroundTimer {
        val fromBackground: Boolean
            get() = backgroundFlag
        val isTimeOut: Boolean
            get() = timeOutFlag

        private var timer: Timer? = null
        private var elapsedTime: Int = 0
        private var timeOutFlag: Boolean = false
        private var backgroundFlag: Boolean = false

        fun start(timeOut: Int, isScreenLockEnabled: Boolean) {
            stop()
            timeOutFlag = false
            backgroundFlag = true

            if (isScreenLockEnabled) {
                if (timeOut > 0) {
                    this.timer = newTimerAndStart(timeOut)
                } else {
                    timeOutFlag = true
                }
            }
        }

        fun stop() {
            timer?.cancel()
            timer = null
            elapsedTime = 0

            timeOutFlag = false
            backgroundFlag = false
        }

        private fun newTimerAndStart(timeOut: Int): Timer =
            timer(initialDelay = 1000, period = 1000) {
                elapsedTime++
                Log.d(TAG, "BackgroundTimer: elapsedTime=$elapsedTime")

                if (elapsedTime >= timeOut) {
                    timeOutFlag = true
                    cancel()
                    Log.e(TAG, "BackgroundTimer: elapsedTime=$elapsedTime, timeOutFlag=$timeOutFlag!!")
                }
            }
    }
}