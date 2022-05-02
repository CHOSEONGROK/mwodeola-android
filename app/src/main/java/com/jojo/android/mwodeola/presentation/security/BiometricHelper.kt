package com.jojo.android.mwodeola.presentation.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.*
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import java.security.InvalidAlgorithmParameterException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey


/**
 * 1. 새로운 지문 정보가 추가되었을 때
 * canAuthentication == [BiometricManager.BIOMETRIC_SUCCESS]
 * isAuthentication == True
 * hasNewBiometricEnrolled == True
 *
 * 2. 기존의 지문 정보에서 삭제되었을 때(하나 이상 남음)
 * canAuthentication == [BiometricManager.BIOMETRIC_SUCCESS]
 * isAuthentication == True
 * hasNewBiometricEnrolled == False
 *
 * 3. 기존의 지문 정보에서 모두 삭제되었을 때
 * canAuthentication == [BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED]
 * isAuthentication == False
 * hasNewBiometricEnrolled == False
 * */
object BiometricHelper {

    private const val TAG = "BiometricHelper"
    private const val BIOMETRIC_KEY_NAME = "com.jojo.android.practicewidgetapp.biometric.secretkey"
    private const val VALIDITY_DURATION_SECONDS = 30

    val isAuthentication: Boolean
        get() = (canAuthentication == BiometricManager.BIOMETRIC_SUCCESS)
    val canAuthentication: Int
        get() = biometricManager.canAuthenticate(BIOMETRIC_STRONG)
    val canAuthenticationString: String
        get() = canAuthenticationToString(canAuthentication)
    val hasNewBiometricEnrolled: Boolean
        get() = (getCipherWithSecretKey() == null)

    private lateinit var biometricManager: BiometricManager

    fun init(applicationContext: Context) {
        biometricManager = BiometricManager.from(applicationContext)
    }

    fun generateSecretKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")

            val keyGenParameterSpecBuilder = KeyGenParameterSpec.Builder(
                BIOMETRIC_KEY_NAME, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .setUserAuthenticationRequired(true)

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.R) {
                // 별로 안 좋은 듯
                // keyGenParameterSpecBuilder
                //    .setUserAuthenticationParameters(VALIDITY_DURATION_SECONDS, KeyProperties.AUTH_BIOMETRIC_STRONG)
            }

            keyGenerator.init(keyGenParameterSpecBuilder.build())
            keyGenerator.generateKey()
        } catch (e: InvalidAlgorithmParameterException) {
            // At least one biometric must be enrolled to create keys requiring user authentication for every use
            Log.w(TAG, e)
        }
    }

    private fun getCipherWithSecretKey(): Cipher? =
        try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
            ).apply { init(Cipher.ENCRYPT_MODE, getSecretKey()) }
        }
        catch (e: KeyPermanentlyInvalidatedException) { null }
        catch (e: Exception) { null }

    private fun getSecretKey(): SecretKey =
        KeyStore.getInstance("AndroidKeyStore")
            // Before the keystore can be accessed, it must be loaded.
            .apply { load(null) }
            .getKey(BIOMETRIC_KEY_NAME, null) as SecretKey

    private fun canAuthenticationToString(code: Int): String =
        when (code) {
            BiometricManager.BIOMETRIC_SUCCESS ->
                "App can authenticate using biometrics."
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE ->
                "No biometric features available on this device."
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE ->
                "Biometric features are currently unavailable."
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED ->
                "The user can't authenticate because no biometric or device credential is enrolled."
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED ->
                "The user can't authenticate because a security vulnerability has been discovered with one or more hardware sensors. " +
                        "The affected sensor(s) are unavailable until a security update has addressed the issue."
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED ->
                "The user can't authenticate because the specified options are incompatible with the current Android version."
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN ->
                "Unable to determine whether the user can authenticate.\n" +
                        "This status code may be returned on older Android versions due to partial incompatibility with a newer SignUpService. \n" +
                        "Applications that wish to enable biometric authentication on affected devices may still call."
            else -> "canAuthentication() = Unknown"
        }

    class Authenticator constructor(
        private val activity: FragmentActivity,
        private val callback: Authenticators.AuthenticationCallback
    ) {
        private val executor = ContextCompat.getMainExecutor(activity)
        private var allowedAuthenticators = BIOMETRIC_STRONG

        private var title = "생체 정보 인증하기"
        private var subtitle = "디바이스에 저장된 지문으로 인증"

        fun title(title: String) = apply {
            this.title = title
        }

        fun subtitle(subtitle: String) = apply {
            this.subtitle = subtitle
        }

        fun appCredential() = apply {
            allowedAuthenticators = BIOMETRIC_STRONG
        }

        fun deviceCredential() = apply {
            allowedAuthenticators = BIOMETRIC_WEAK or DEVICE_CREDENTIAL
        }

        fun execute() {
            if (allowedAuthenticators == BIOMETRIC_STRONG) {
                if (hasNewBiometricEnrolled) {
                    generateSecretKey()
                }

                val cipher = getCipherWithSecretKey()!!
                val cryptoObject = BiometricPrompt.CryptoObject(cipher)

                newBiometricPrompt()
                    .authenticate(newPromptInfo(allowedAuthenticators), cryptoObject)
            } else {
                newBiometricPrompt()
                    .authenticate(newPromptInfo(allowedAuthenticators))
            }
        }

        private fun newBiometricPrompt(): BiometricPrompt =
            BiometricPrompt(activity, executor, AuthenticationCallback(callback))

        private fun newPromptInfo(allowedAuthenticators: Int): BiometricPrompt.PromptInfo =
            BiometricPrompt.PromptInfo.Builder().apply {
                setTitle(title)
                setSubtitle(subtitle)
                setAllowedAuthenticators(allowedAuthenticators)

                if (allowedAuthenticators == BIOMETRIC_STRONG) {
                    setNegativeButtonText("취소")
                } else {
                    setConfirmationRequired(false)
                }
            }.build()
    }

    class AuthenticationCallback(
        private val callback: Authenticators.AuthenticationCallback
    ) : BiometricPrompt.AuthenticationCallback() {

        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            Log.w(TAG, "onAuthenticationError(errorCode=$errorCode): $errString")
            callback.onFailure()
        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            Log.d(TAG, "onAuthenticationSucceeded(): authenticationType=${result.authenticationType}")
            callback.onSucceed()
        }

        override fun onAuthenticationFailed() {
            Log.w(TAG, "onAuthenticationFailed()")
        }

        private fun errorCodeToString(errorCode: Int): String = when (errorCode) {
            BiometricPrompt.ERROR_CANCELED ->
                "The operation was canceled because the biometric sensor is unavailable. " +
                        "This may happen when the user is switched, the device is locked, or another pending operation prevents it.(code=5)"
            BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                "The device does not have the required authentication hardware.(code=12)"
            BiometricPrompt.ERROR_HW_UNAVAILABLE ->
                "The hardware is unavailable. Try again later.(code=1)"
            BiometricPrompt.ERROR_LOCKOUT ->
                "The operation was canceled because the API is locked out due to too many attempts. " +
                        "This occurs after 5 failed attempts, and lasts for 30 seconds.(code=7)"
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT ->
                "The operation was canceled because ERROR_LOCKOUT occurred too many times. " +
                        "Biometric authentication is disabled until the user unlocks with their device credential " +
                        "(i.e. PIN, pattern, or password)(code=9)"
            BiometricPrompt.ERROR_NEGATIVE_BUTTON ->
                "The user pressed the negative button.(code=13)"
            BiometricPrompt.ERROR_NO_BIOMETRICS ->
                "The user does not have any biometrics enrolled.(code=11)"
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL ->
                "The device does not have pin, pattern, or password set up.(code=14)"
            BiometricPrompt.ERROR_NO_SPACE ->
                "The operation can't be completed because there is not enough device storage remaining.(code=4)"
            BiometricPrompt.ERROR_TIMEOUT ->
                "The current operation has been running too long and has timed out.\n" +
                        "This is intended to prevent programs from waiting for the biometric sensor indefinitely. " +
                        "The timeout is platform and sensor-specific, but is generally on the order of ~30 seconds.(code=3)"
            BiometricPrompt.ERROR_UNABLE_TO_PROCESS ->
                "The sensor was unable to process the current image.(code=2)"
            BiometricPrompt.ERROR_USER_CANCELED ->
                "The user canceled the operation.\n" +
                        "Upon receiving this, applications should use alternate authentication, such as a password. " +
                        "The application should also provide the user a way of returning to biometric authentication, such as a button.(code=10)"
            BiometricPrompt.ERROR_VENDOR ->
                "The operation failed due to a vendor-specific error.\n" +
                        "This error code may be used by hardware vendors to extend this list to cover errors that don't fall under one of the other predefined categories. " +
                        "Vendors are responsible for providing the strings for these errors.\n" +
                        "These messages are typically reserved for internal operations such as enrollment but may be used to express any error that is not otherwise covered. " +
                        "In this case, applications are expected to show the error message, " +
                        "but they are advised not to rely on the message ID, since this may vary by vendor and device.(code=8)"
            else -> "Unknown Error(code=$errorCode)"
        }
    }

    private fun getCipherWithSecretKey2(): Cipher? =
        try {
            Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/" +
                    KeyProperties.BLOCK_MODE_CBC + "/" +
                    KeyProperties.ENCRYPTION_PADDING_PKCS7
            ).apply { init(Cipher.ENCRYPT_MODE, getSecretKey()) }
        }
        catch (e: KeyPermanentlyInvalidatedException) { Log.w(TAG, e); null }
        catch (e: Exception) { Log.w(TAG, e); null }
}