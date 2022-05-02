package com.jojo.android.mwodeola.model.sign

interface SignUpDTO {
    data class SignUpVerifyEmail(val email: String)
    data class SignUpVerifyPhone(val phone_number: String)
    data class SignUp(val user_name: String, val email: String, val phone_number: String, val password: String)
    data class SignIn(val phone_number: String, val password: String)
    data class PasswordAuth(val password: String)
    data class PasswordChange(val old_password: String, val new_password: String)
}