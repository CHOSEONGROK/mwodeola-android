package com.jojo.android.mwodeola.util

import android.util.Log

object HangulUtils {

    private const val TAG = "HangulUtils"

    private const val DECIMAL_BEGIN_UNICODE = 48 // 0
    private const val DECIMAL_END_UNICODE = 57 // 9

    private const val ALPHABET_UPPER_CASE_BEGIN_UNICODE = 65 // A
    private const val ALPHABET_UPPER_CASE_END_UNICODE = 90 // Z
    private const val ALPHABET_LOWER_CASE_BEGIN_UNICODE = 97 // a
    private const val ALPHABET_LOWER_CASE_END_UNICODE = 122 // z

    private const val HANGUL_BEGIN_UNICODE = 44032 // 가
    private const val HANGUL_END_UNICODE = 55203 // 힣
    private const val HANGUL_BASE_UNIT = 588

    private val INITIAL_SOUND_UNICODE = intArrayOf(
        12593, 12594, 12596,
        12599, 12600, 12601, 12609, 12610, 12611, 12613, 12614, 12615,
        12616, 12617, 12618, 12619, 12620, 12621, 12622
    )
    private val INITIAL_SOUND = charArrayOf(
        'ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ',
        'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ'
    )

    fun getHangulInitialSound(value: String): String {
        val result = StringBuffer()
        val unicodeList = value.toCharArray().map { it.code }
        for (unicode in unicodeList) {
            if (unicode in HANGUL_BEGIN_UNICODE..HANGUL_END_UNICODE) {
                val tmp = unicode - HANGUL_BEGIN_UNICODE
                val index = tmp / HANGUL_BASE_UNIT
                result.append(INITIAL_SOUND[index])
            } else {
                result.append(unicode.toChar())
            }
        }
        return result.toString()
    }

    fun getFirstCharOfInitialSound(value: String): Char =
        if (value.isEmpty()) {
            ' '
        } else {
            value.toCharArray()[0].code.let { firstCharUnicode ->
                if (firstCharUnicode in HANGUL_BEGIN_UNICODE..HANGUL_END_UNICODE) {
                    INITIAL_SOUND[(firstCharUnicode - HANGUL_BEGIN_UNICODE) / HANGUL_BASE_UNIT]
                } else {
                    value[0]
                }
            }
        }

    fun getFirstCharOfInitialSound2(value: String): String =
        if (value.isEmpty()) {
            ""
        } else {
            value.toCharArray()[0].code.let { firstCharUnicode ->
                when (firstCharUnicode) {
                    // 한글 유니코드(ㄱ..ㅎ)
                    in HANGUL_BEGIN_UNICODE..HANGUL_END_UNICODE ->
                        INITIAL_SOUND[(firstCharUnicode - HANGUL_BEGIN_UNICODE) / HANGUL_BASE_UNIT].toString()
                    // 숫자 유니코드(0..9)
                    in DECIMAL_BEGIN_UNICODE..DECIMAL_END_UNICODE ->
                        value.substring(0..0)
                    // 알파벳 유니코드(대문자, A..Z)
                    in ALPHABET_UPPER_CASE_BEGIN_UNICODE..ALPHABET_UPPER_CASE_END_UNICODE ->
                        value.substring(0..0)
                    // 알파벳 유니코드(소문자, a..z)
                    in ALPHABET_LOWER_CASE_BEGIN_UNICODE..ALPHABET_LOWER_CASE_END_UNICODE ->
                        value.substring(0..0).uppercase()
                    else -> "#"
                }
            }
        }




}