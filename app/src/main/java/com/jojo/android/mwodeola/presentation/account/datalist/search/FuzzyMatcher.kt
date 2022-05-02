package com.jojo.android.mwodeola.presentation.account.datalist.search

import android.graphics.Color
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.core.text.clearSpans


/**
 * [https://en.wikipedia.org/wiki/Korean_language_and_computers]
 * */
object FuzzyMatcher {

    interface Matchable {
        val comparator: String
        var fuzzyPoint: Long

        fun getSpannable(): SpannableString
    }

    private const val TAG = "FuzzyMatcher"

    private const val DECIMAL_BEGIN_UNICODE = 48 // 0
    private const val DECIMAL_END_UNICODE = 57 // 9

    private const val ALPHABET_UPPER_CASE_BEGIN_UNICODE = 65 // A
    private const val ALPHABET_UPPER_CASE_END_UNICODE = 90 // Z
    private const val ALPHABET_LOWER_CASE_BEGIN_UNICODE = 97 // a
    private const val ALPHABET_LOWER_CASE_END_UNICODE = 122 // z
    private const val ALPHABET_DISTANCE = 32

    private const val HANGUL_BEGIN_UNICODE = 44032 // 가
    private const val HANGUL_END_UNICODE = 55203 // 힣
    private const val HANGUL_BASE_UNIT = 588
    private const val CONSONANTS_BEGIN_UNICODE = 12593 // ㄱ
    private const val CONSONANTS_END_UNICODE = 12622 // ㅎ
    private const val VOWELS_BEGIN_UNICODE = 12623 // ㅏ
    private const val VOWELS_END_UNICODE = 12643 // ㅣ

    private val ALPHABET_UPPER_CASE = ALPHABET_UPPER_CASE_BEGIN_UNICODE..ALPHABET_UPPER_CASE_END_UNICODE // A ~ Z
    private val ALPHABET_LOWER_CASE = ALPHABET_LOWER_CASE_BEGIN_UNICODE..ALPHABET_LOWER_CASE_END_UNICODE // a ~ z

    private val HANGUL_RANGE = HANGUL_BEGIN_UNICODE..HANGUL_END_UNICODE // 가..힣: 44032..55203
    private val CONSONANTS_RANGE = CONSONANTS_BEGIN_UNICODE..CONSONANTS_END_UNICODE // ㄱ ~ ㅎ
    private val VOWELS_RANGE = VOWELS_BEGIN_UNICODE..VOWELS_END_UNICODE // ㅏ ~ ㅣ

    private val CONSONANTS = charArrayOf('ㄱ', 'ㄲ', 'ㄴ', 'ㄷ', 'ㄸ', 'ㄹ', 'ㅁ', 'ㅂ', 'ㅃ', 'ㅅ', 'ㅆ', 'ㅇ', 'ㅈ', 'ㅉ', 'ㅊ', 'ㅋ', 'ㅌ', 'ㅍ', 'ㅎ')
    private val CONSONANTS_CODE = intArrayOf(12593, 12594, 12596, 12599, 12600, 12601, 12609, 12610, 12611, 12613, 12614, 12615, 12616, 12617, 12618, 12619, 12620, 12621, 12622)

    private val VOWELS = charArrayOf('ㅏ', 'ㅐ', 'ㅑ', 'ㅒ', 'ㅓ', 'ㅔ', 'ㅕ', 'ㅖ', 'ㅗ', 'ㅘ', 'ㅙ', 'ㅚ', 'ㅛ', 'ㅜ', 'ㅝ', 'ㅞ', 'ㅟ', 'ㅠ', 'ㅡ', 'ㅢ', 'ㅣ')
    private val VOWELS_CODE = intArrayOf(12623, 12624, 12625, 12626, 12627, 12628, 12629, 12630, 12631, 12632, 12633, 12634, 12635, 12636, 12637, 12638, 12639, 12640, 12641, 12642, 12643)

    fun <T : Matchable> matchBy(data: List<T>, keyword: String): List<T> = when {
        keyword.isBlank() -> emptyList()
        else ->
            data.filter { element: Matchable ->
                // fuzzy matching index array
                val matchedIndices = matches(element.comparator, keyword)
                if (matchedIndices.isNotEmpty()) {
                    element.fuzzyPoint = 0
                    element.getSpannable().clearSpans()

                    matchedIndices.forEach { index ->
                        element.getSpannable().setSpan(
                            ForegroundColorSpan(Color.RED),
                            index,
                            (index + 1),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                        )
                        element.fuzzyPoint = getFuzzyPoint(matchedIndices)
                    }

                    true
                } else {
                    false
                }
            }.sortedBy { it.fuzzyPoint }
    }


    /**
     * source: Fuzzy Matching 의 대상 문자열
     * keyword: 검색어
     * return: source 문자열에서 keyword 문자들의 fuzzy matching 이 true 인 index array
     * */
    fun matches(source: String, keyword: String): IntArray {
        val trimKeyword = keyword.replace(" ", "")

        if (source.length < trimKeyword.length || trimKeyword.isEmpty())
            return IntArray(0)

        val matchedIndices = mutableListOf<Int>()

        var index = 0
        val sourceLength = source.length

        val keywordCodePointStream = trimKeyword.codePoints()
        val keywordCodePoints = keywordCodePointStream.toArray()

        // Keyword 문자열의 유니코드 Array 로 변환
        keywordCodePoints.forEachIndexed { j, keywordCode ->

            while (index < sourceLength) {
                // source 문자열을 유니코드 단위로 가져옴.
                val sourceCode = source.codePointAt(index)

                // source 의 index 번째 유니코드와 keyword 의 j 번째 유니코드 비교
                if (matchFuzzy(sourceCode, keywordCode)) {
                    matchedIndices.add(index)
                    index += Character.charCount(sourceCode)
                    break
                }

                index += Character.charCount(sourceCode)
            }

            // source 가 keyword 의 모든 문자를 포함하지 못하면 fuzzy matching 실패 판정
            if (j == keywordCodePoints.size - 1 &&
                matchedIndices.size != keywordCodePoints.size
            ) {
                matchedIndices.clear()
            }
        }

        keywordCodePointStream.close()

        return matchedIndices.toIntArray()
    }

    fun getFuzzyPoint(arr: IntArray): Long {
        var fuzzyPoint = 0L
        arr.forEach { fuzzyPoint += 1.shl(it) }
        return fuzzyPoint
    }

    /**
     * Hangul Code: [(initial) × 588 + (medial) × 28 + (final)] + 44032 = code
     * initial(초성, 18개): ㄱ(0) ~ ㅎ(18)
     * medial(중모음, 20개): ㅏ(0) ~ ㅣ(20)
     * final(종성, 28개): none(0) ~ ㅎ(27)
     * https://en.wikipedia.org/wiki/Korean_language_and_computers
     * */
    private fun matchFuzzy(sourceCode: Int, targetCode: Int): Boolean {
        if (sourceCode == targetCode)
            return true

        return when (targetCode) {
            // 알파벳 대문자
            in ALPHABET_UPPER_CASE ->
                sourceCode == targetCode + ALPHABET_DISTANCE
            // 알파벳 소문자
            in ALPHABET_LOWER_CASE ->
                sourceCode == targetCode - ALPHABET_DISTANCE
            // targetCode: 자음
            // 588i + 28 ≤ sourceCode ≤ 588i + 20*8 + 27 + 44032
            in CONSONANTS_RANGE -> {
                val initialConsonantsIndex = CONSONANTS_CODE.indexOf(targetCode)

                val begin = initialConsonantsIndex * 588 + HANGUL_BEGIN_UNICODE
                val end = (initialConsonantsIndex * 588) + (20 * 28) + 27 + HANGUL_BEGIN_UNICODE

                sourceCode in begin..end
            }

            // targetCode: (초성 + 중모음) or (초성 + 중모음 + 종성)
            // (1) 초성 + 중모음: targetCode - {(targetCode - 44032) mod 28} ≤ sourceCode ≤ targetCode + [27 - {(targetCode - 44032) mod 28}]
            // (2) 초성 + 중모음 + 종성: sourceCode = targetCode
            in HANGUL_RANGE -> {
                val finalConsonantsIndex = (targetCode - HANGUL_BEGIN_UNICODE) % 28

                return if (finalConsonantsIndex == 0) {
                    // (1) 초성 + 중모음
                    val begin = targetCode - finalConsonantsIndex
                    val end = targetCode + (27 - finalConsonantsIndex)

                    sourceCode in begin..end
                } else {
                    // (2) 초성 + 중모음 + 종성
                    sourceCode == targetCode
                }
            }
            else -> {
                sourceCode == targetCode
            }
        }
    }

    private fun test(source: String) {
        val sourceCharArray = source.toCharArray()

        var codePoint: Int
        var codePointCharCount: Int
        var codePointString: String
        var i = 0
        val len = source.length
        while (i < len) {
            codePoint = Character.codePointAt(source, i)
            codePointCharCount = Character.charCount(codePoint)
            codePointString = String(sourceCharArray, i, codePointCharCount)

            Log.i(TAG, "i=$i, codePoint=$codePoint, codePointString=$codePointString")

            i += codePointCharCount
        }
    }

    // https://en.wikipedia.org/wiki/File:Hangul_Compatibility_Jamo_block_in_Unicode.svg
    private fun testHangulJamoBlockInUnicode() {
        for (i in 12593 until 12593 + 94) {
            Log.i(TAG, "code=$i, value=${Char(i)}")
        }
    }
}