package com.jojo.android.mwodeola.presentation.security

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Log
import java.lang.Exception
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature

object SecurityManager {

    private const val TAG = "SecuritySharedPref"
    private const val KEY_ALIAS = "jojo_mwodeola_app_lock_secret_key"

    fun hasNotSecretKey(): Boolean =
        try { getPrivateKeyEntry(); false }
        catch (e: Exception) { true }

    fun generateSecretKey() {
        val keyGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC, "AndroidKeyStore")

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .build()

        keyGenerator.initialize(keyGenParameterSpec)
        keyGenerator.generateKeyPair()
    }

    class SharedPref(private val context: Context) {

        companion object {
            private const val TAG = "SecuritySharedPref"
            private const val KEY_ALIAS = "jojo_mwodeola_app_lock_secret_key"

            private const val KEY_AUTH_TYPE = "auth_type"
            private const val KEY_SCREEN_LOCK_ENABLED = "screen_lock_enabled"
            private const val KEY_SCREEN_LOCK_CREDENTIAL = "screen_lock_credential"
            private const val KEY_SCREEN_LOCK_TIMEOUT = "screen_lock_timeout"

            const val APP_CREDENTIAL = 0
            const val DEVICE_CREDENTIAL = 1

            const val TIMEOUT_DEFAULT = 0
        }

        private val preferences: SharedPreferences
            get() = context.getSharedPreferences(context.packageName + ".security", Context.MODE_PRIVATE)

        private val editor: SharedPreferences.Editor
            get() = preferences.edit()

        fun authType(): AuthType =
            AuthType.valueOf(preferences.getInt(KEY_AUTH_TYPE, AuthType.PIN_5.ordinal))
        fun authType(type: AuthType) =
            editor.putInt(KEY_AUTH_TYPE, type.ordinal).apply()

        fun passwordPin6(value: String) =
            editor.putString(AuthType.PIN_6.name, encrypt(value)).apply()

        fun passwordPattern(value: String) =
            editor.putString(AuthType.PATTERN.name, encrypt(value)).apply()

        fun registerBiometric() =
            editor.putString(AuthType.BIOMETRIC.name, "used").apply()

        fun signaturePin6(value: String): Boolean =
            signature(AuthType.PIN_6, value)

        fun signaturePattern(value: String): Boolean =
            signature(AuthType.PATTERN, value)

        fun isExistsPassword(type: AuthType) =
            if (type == AuthType.PIN_5) true
            else preferences.getString(type.name, null) != null

        fun deletePassword(type: AuthType) =
            editor.remove(type.name).apply()

        fun isScreenLockEnabled(): Boolean =
            preferences.getBoolean(KEY_SCREEN_LOCK_ENABLED, true)
        fun isScreenLockEnabled(value: Boolean) =
            editor.putBoolean(KEY_SCREEN_LOCK_ENABLED, value).apply()

        fun screenLockCredential(): Int =
            preferences.getInt(KEY_SCREEN_LOCK_CREDENTIAL, APP_CREDENTIAL)
        fun screenLockCredential(value: Int) =
            editor.putInt(KEY_SCREEN_LOCK_CREDENTIAL, value).apply()

        fun screenLockTimeout(): Int =
            preferences.getInt(KEY_SCREEN_LOCK_TIMEOUT, TIMEOUT_DEFAULT)
        fun screenLockTimeoutString(): String =
            screenLockTimeoutToString(screenLockTimeout())
        fun screenLockTimeout(value: Int) =
            editor.putInt(KEY_SCREEN_LOCK_TIMEOUT, value).apply()

        fun clearAll() = editor.clear().apply()

        fun screenLockTimeoutToString(timeout: Int): String = when (timeout) {
            0 -> "즉시"
            in 1 until 60 -> "${timeout}초"
            60 -> "1분"
            else -> ""
        }

        private fun encrypt(value: String): String {
            val entry = getPrivateKeyEntry()

            val digest = Signature.getInstance("SHA256withECDSA").apply {
                initSign(entry.privateKey)
                update(value.encodeToByteArray())
            }.sign()

            return decode(digest)
        }

        private fun signature(type: AuthType, target: String): Boolean {
            Log.d(TAG, "signature(): $target")

            val entry = getPrivateKeyEntry()
            val signature = preferences.getString(type.name, null)
                ?: return false

            val valid = Signature.getInstance("SHA256withECDSA").apply {
                initVerify(entry.certificate)
                update(target.encodeToByteArray())
            }.verify(encode(signature))

            return valid
        }

        private fun encode(str: String): ByteArray {
            val split = str.split(',')
            val arr = ByteArray(split.size - 1)
            split.forEachIndexed { index, s ->
                if (s.isNotBlank())
                    arr[index] = s.toInt().toByte()
            }
            return arr
        }

        private fun decode(source: ByteArray): String =
            StringBuilder().also { builder ->
                source.forEach { builder.append("$it,") }
            }.toString()
    }

    private fun getPrivateKeyEntry(): KeyStore.PrivateKeyEntry =
        KeyStore.getInstance("AndroidKeyStore")
            .apply { load(null) }
            .getEntry(KEY_ALIAS, null) as KeyStore.PrivateKeyEntry
}