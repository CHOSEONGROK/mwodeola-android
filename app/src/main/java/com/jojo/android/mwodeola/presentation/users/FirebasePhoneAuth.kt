package com.jojo.android.mwodeola.presentation.users

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timer

class FirebasePhoneAuth constructor(private val activity: Activity) {
    companion object {
        private const val TAG = "FirebasePhoneAuth"
        private const val TIME_OUT = 120L
    }

    interface OnPhoneAuthCallback {
        fun onVerificationCompleted(smsCode: String)
        fun onVerificationFailed(e: FirebaseException)
        fun onStarted(phoneNumber: String)
        fun onReStarted(phoneNumber: String)
        fun onTimer(elapsedTimeSec: Int, remainingTimeString: String)
        fun onTimeout()
        fun onPhoneAuthSucceed()
        fun onPhoneAuthFailed()
    }

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val timer = PhoneAuthTimer(activity, TIME_OUT.toInt())

    private var storedVerificationId : String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private val verificationCallback = OnVerificationCallback()
    private var userCallback: OnPhoneAuthCallback? = null

    fun init() {
//        FirebaseApp.initializeApp(activity)
//        auth = FirebaseAuth.getInstance()
    }

    fun setOnPhoneAuthCallback(callback: OnPhoneAuthCallback) {
        userCallback = callback
    }

    fun send(phoneNumber: String) {
        PhoneAuthProvider.verifyPhoneNumber(
            phoneAuthOptionsBuilder(phoneNumber)
                .build()
        )
    }

    fun resend(phoneNumber: String) {
        if (resendingToken != null) {
            PhoneAuthProvider.verifyPhoneNumber(
                phoneAuthOptionsBuilder(phoneNumber)
                    .setForceResendingToken(resendingToken!!)
                    .build())
        } else {
            send(phoneNumber)
        }
    }

    fun authenticate(smsCode: String) {
        if (storedVerificationId == null) return

        val credential = PhoneAuthProvider.getCredential(storedVerificationId!!, smsCode)
        signInWithPhoneAuthCredential(credential)
    }

    /**
     * https://firebase.google.com/docs/auth/android/phone-auth?hl=ko
     * [앱 인증 사용 설정]
     * 1. SafetyNet
     * 2. reCAPTCHA 인증 (<= 현재 방식, 자동 구현)
     * 추후 SafetyNet 활용 방법 찾아보고 구현하기.
     * */
    private fun phoneAuthOptionsBuilder(phoneNumber: String): PhoneAuthOptions.Builder =
        PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(TIME_OUT, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(verificationCallback.apply { sendingPhoneNumber = phoneNumber })

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    val user = task.result?.user
                    val additionalUserInfo = task.result?.additionalUserInfo
                    val authCredential = task.result?.credential
                    userCallback?.onPhoneAuthSucceed()
                    timer.stop()
                } else {
                    // Sign in failed, display a message and update the UI
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    // Update UI
                    userCallback?.onPhoneAuthFailed()
                }
            }
    }

    inner class OnVerificationCallback : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        var sendingPhoneNumber: String = ""
        private val decimalFormat = DecimalFormat("00")

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            // 번호인증 혹은 기타 다른 인증(구글로그인, 이메일로그인 등) 끝난 상태
            // This callback will be invoked in two situations:
            // 1 - Instant verification. In some cases the phone number can be instantly
            //     verified without needing to send or enter a verification code.
            // 2 - Auto-retrieval. On some devices Google Play services can automatically
            //     detect the incoming verification SMS and perform verification without
            //     user action.
            Log.d(TAG, "onVerificationCompleted(): smsCode=${credential.smsCode}")
            if (credential.smsCode != null) {
                userCallback?.onVerificationCompleted(credential.smsCode!!)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.
            Log.e(TAG, "onVerificationFailed(): $e")
            userCallback?.onVerificationFailed(e)
        }

        override fun onCodeAutoRetrievalTimeOut(p0: String) {
            super.onCodeAutoRetrievalTimeOut(p0)
            Log.e(TAG, "onCodeAutoRetrievalTimeOut(): $p0")
        }

        override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
            // The SMS verification code has been sent to the provided phone number, we
            // now need to ask the user to enter the code and then construct a credential
            // by combining the code with a verification ID.
            Log.d(TAG, "onCodeSent(): $verificationId, $token")

            if (storedVerificationId == null) {
                userCallback?.onStarted(sendingPhoneNumber)
            } else {
                userCallback?.onReStarted(sendingPhoneNumber)
            }

            storedVerificationId = verificationId
            resendingToken = token

            timer.start { elapsedTimeSec, remainingTimeString, isTimeout ->
                userCallback?.onTimer(elapsedTimeSec, remainingTimeString)

                if (isTimeout) {
                    userCallback?.onTimeout()
                }
            }
        }
    }

    private class PhoneAuthTimer(val activity: Activity, val timeLimit: Int) {
        private val decimalFormat = DecimalFormat("00")

        var elapsedTimeSec = 0

        private var timer: Timer? = null

        fun start(taskOnUiThread: (elapsedTimeSec: Int, remainingTimeString: String, isTimeout: Boolean) -> Unit) {
            stop()
            timer = getTimer(taskOnUiThread)
        }

        fun stop() {
            timer?.cancel()
            timer = null
            elapsedTimeSec = 0
        }

        private fun getTimer(taskOnUiThread: (elapsedTimeSec: Int, remainingTimeString: String, isTimeout: Boolean) -> Unit): Timer =
            timer(initialDelay = 0L, period = 1000L) {
                val remainingTimeSec = timeLimit - elapsedTimeSec.toLong()

                val hour = TimeUnit.SECONDS.toHours(remainingTimeSec) - TimeUnit.SECONDS.toDays(remainingTimeSec) * 24
                val minute = TimeUnit.SECONDS.toMinutes(remainingTimeSec) - hour * 60
                val second = TimeUnit.SECONDS.toSeconds(remainingTimeSec) - minute * 60

                val remainingTimeString =
                    "${decimalFormat.format(minute)} : ${decimalFormat.format(second)}"

                activity.runOnUiThread {
                    if (elapsedTimeSec == timeLimit) {
                        taskOnUiThread.invoke(elapsedTimeSec, remainingTimeString, true)
                        stop()
                    } else {
                        taskOnUiThread.invoke(elapsedTimeSec, remainingTimeString, false)
                        elapsedTimeSec++
                    }
                }
            }
    }
}
