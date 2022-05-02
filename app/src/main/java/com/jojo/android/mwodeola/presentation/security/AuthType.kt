package com.jojo.android.mwodeola.presentation.security

enum class AuthType {
    PIN_5, PIN_6, PATTERN, BIOMETRIC;

    companion object {
        fun contains(value: AuthType): Boolean = AuthType.values().any { it == value }
        fun valueOf(ordinal: Int): AuthType = AuthType.values()[ordinal]
        fun valueOrNull(name: String): AuthType? = AuthType.values().firstOrNull { it.name == name }
        fun valueOrNull(ordinal: Int): AuthType? = AuthType.values().getOrNull(ordinal)
    }
}