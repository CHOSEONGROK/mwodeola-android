package com.jojo.android.mwodeola.presentation.users

import android.app.Activity
import android.content.*
import android.os.Build
import android.util.Log
import com.google.android.gms.auth.api.credentials.Credential
import com.google.android.gms.auth.api.credentials.Credentials
import com.google.android.gms.auth.api.credentials.HintRequest
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.auth.api.phone.SmsRetrieverClient
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

class SmsRetrieveHelper(private val activity: Activity) {
    companion object {
        private const val TAG = "SmsRetrieveHelper"

        const val CREDENTIAL_PICKER_PHONE_REQUEST = 1
        const val CREDENTIAL_PICKER_EMAIL_REQUEST = 2
        const val SMS_CONSENT_REQUEST = 3
    }

    interface OnSmsRetrieveCallback {
        fun onPickedPhone(phoneNumber: String)
        fun onPickedEmail(email: String)
        fun onSmsRetrieved(message: String, smsCode: String?)
    }

    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        // Get consent intent
                        val consentIntent = extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        Log.d(TAG, "onReceive(): CommonStatusCodes.SUCCESS, consentIntent=$consentIntent")
                        try {
                            // Start activity to show consent dialog to user, activity must be started in
                            // 5 minutes, otherwise you'll receive another TIMEOUT intent
                            if (consentIntent != null) {
                                activity.startActivityForResult(consentIntent, SMS_CONSENT_REQUEST)
                            }
                        } catch (e: ActivityNotFoundException) {
                            Log.w(TAG, "onReceive(): e=$e")
                        } catch (e: NullPointerException) {
                            Log.w(TAG, "onReceive(): e=$e")
                        } catch (e: Exception) {
                            Log.w(TAG, "onReceive(): e=$e")
                        }
                    }
                    CommonStatusCodes.TIMEOUT -> {
                        // Time out occurred, handle the error.
                        Log.w(TAG, "onReceive(): CommonStatusCodes.TIMEOUT")
                    }
                }
            }
        }
    }

    private lateinit var smsRetrieverClient: SmsRetrieverClient
    private var callback: OnSmsRetrieveCallback? = null

    /**
     * onCreate() 에서 호출
     * */
    fun init(): SmsRetrieveHelper = apply {
        // SMS Retriever 브로드캐스트 등록
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
            activity.registerReceiver(
                smsVerificationReceiver,
                intentFilter,
                SmsRetriever.SEND_PERMISSION,
                null
            )
        }
        smsRetrieverClient = SmsRetriever.getClient(activity)
    }

    fun setOnSmsRetrieveCallback(callback: OnSmsRetrieveCallback) {
        this.callback = callback
    }

    fun removeOnSmsRetrieveCallback() {
        callback = null
    }

    /**
     * 구글 계정과 연동된 휴대폰 번호 picker
     * */
    fun requestHintPhone() {
        requestHint(forEmail=false, forPhone=true)
    }

    /**
     * 구글 계정과 연동된 이메일 picker
     * */
    fun requestHintEmail() {
        requestHint(forEmail=true, forPhone=false)
    }

    /**
     * 인증 문자 요청하기 전에, 문자 auto retrieve 시작
     * sender: 문자 발신자 번호
     * */
    fun startSmsUserConsent(sender: String) {
        smsRetrieverClient.startSmsUserConsent(sender)
    }

    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CREDENTIAL_PICKER_EMAIL_REQUEST ->
                // Obtain the email from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
//                    logCredentials(credential)
                    val email = credential?.id
                    if (email != null) {
                        callback?.onPickedEmail(email)
                    }
                }
            CREDENTIAL_PICKER_PHONE_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    val credential = data.getParcelableExtra<Credential>(Credential.EXTRA_KEY)
                    val phoneNumber = credential?.id
                    if (phoneNumber != null) {
                        callback?.onPickedPhone(phoneNumber)
                    }
                }
            SMS_CONSENT_REQUEST ->
                // Obtain the phone number from the result
                if (resultCode == Activity.RESULT_OK && data != null) {
                    // Get SMS message content
                    val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                    // Extract one-time code from the message and complete verification
                    // `message` contains the entire text of the SMS message, so you will need
                    // to parse the string.
                    Log.d(TAG, "handleActivityResult(): message=$message")
                    if (message != null) {
                        callback?.onSmsRetrieved(message, parseOneTimeCode(message))
                    }

                    // send one time code to the server
                } else {
                    // Consent denied. User can type OTC manually.
                }
        }
    }

    private fun parseOneTimeCode(message: String): String? {
        return null
    }

    private fun requestHint(forEmail: Boolean, forPhone: Boolean) {
        // Construct a request for email or phone numbers and show the picker
        val hintRequest = HintRequest.Builder()
            .setEmailAddressIdentifierSupported(forEmail)
            .setPhoneNumberIdentifierSupported(forPhone)
            .build()

        val credentialsClient = Credentials.getClient(activity)
        val intent = credentialsClient.getHintPickerIntent(hintRequest)
        val requestCode =
            if (forEmail) CREDENTIAL_PICKER_EMAIL_REQUEST
            else CREDENTIAL_PICKER_PHONE_REQUEST

        activity.startIntentSenderForResult(intent.intentSender, requestCode,
            null, 0, 0, 0)
    }

    private fun logCredentials(credential: Credential?) {
        Log.d(TAG, "credential.id=${credential?.id}")
        Log.d(TAG, "credential.idTokens=${credential?.idTokens}")
        Log.d(TAG, "credential.name=${credential?.name}")
        Log.d(TAG, "credential.familyName=${credential?.familyName}")
        Log.d(TAG, "credential.givenName=${credential?.givenName}")
        Log.d(TAG, "credential.password=${credential?.password}")
        Log.d(TAG, "credential.profilePictureUri=${credential?.profilePictureUri}")
        Log.d(TAG, "credential.accountType=${credential?.accountType}")
    }
}