package com.jojo.android.mwodeola.model

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Response


data class ErrorResponse(
    val responseCode: Int,
    val message: String?,
    val code: String?,
    val detail: String?,
    val count: Int?,
    val limit: Int?,
    val error: String?,
    val messages: String?,
    val rawErrorString: String?,
) {

    companion object {
        /** 필드 에러 */
        const val FIELD_ERROR = "field_error"
        /** 비밀번호 오류 */
        const val ERROR_CODE_AUTHENTICATION_FAILED = "authentication_failed"
        const val ERROR_CODE_AUTHENTICATION_EXCEED = "authentication_exceed"

        /** Refresh 토큰 만료 or 토큰 블랙리스트 */
        const val ERROR_CODE_TOKEN_NOT_VALID = "token_not_valid"
        const val ERROR_CODE_BAD_AUTHENTICATION_HEADER = "bad_authorization_header"
        const val ERROR_DETAIL_TOKEN_NOT_PROVIDED = "Authentication credentials were not provided."

        const val ERROR_CODE_USER_INACTIVE = "user_inactive"
        const val ERROR_CODE_USER_LOCKED = "user_locked"
        const val ERROR_CODE_USER_NOT_FOUND = "user_not_found"

        /** Account Api Error Code */
        const val ERROR_CODE_DUPLICATED_FIELD = "duplicated_field"

        fun <T> createBy(response: Response<T>): ErrorResponse {
            val errString = response.errorBody()?.string()
                ?: return empty(response.code(), null)

            return parse(response.code(), errString)
        }

        fun createBy(response: okhttp3.Response): ErrorResponse {
            // TODO: Closed 이슈 해결하기
            val responseCode = response.code
            val errString = response.peekBody(2048).string()
//            val errString = response.body?.string()

            return if (errString == null) empty(responseCode, null)
            else parse(responseCode, errString)
        }

        private fun empty(responseCode: Int, errString: String?): ErrorResponse =
            ErrorResponse(responseCode, null, null, null,
                null, null, null, null, errString)

        private fun parse(responseCode: Int, errString: String): ErrorResponse {
            var message: String? = null
            var code: String? = null
            var detail: String? = null
            var count: Int? = null
            var limit: Int? = null
            var error: String? = null
            var messages: String? = null

            val jsonObject: JSONObject

            try {
                jsonObject = JSONObject(errString)
            } catch (e: JSONException) {
                return empty(responseCode, errString)
            }

            try { message = jsonObject.getString("message") } catch (e: JSONException) {}
            try { detail = jsonObject.getString("detail") } catch (e: JSONException) {}
            try { code = jsonObject.getString("code") } catch (e: JSONException) {}
            try { count = jsonObject.getInt("count") } catch (e: JSONException) {}
            try { limit = jsonObject.getInt("limit") } catch (e: JSONException) {}
            try { error = jsonObject.getString("error") } catch (e: JSONException) {}
            try { messages = jsonObject.getJSONArray("messages").toString() } catch (e: JSONException) {}

            return ErrorResponse(responseCode, message, code, detail, count, limit, error, messages, errString)
        }
    }
}
