package com.jojo.android.mwodeola.presentation.common

import android.graphics.Color
import androidx.core.graphics.ColorUtils
import kotlin.math.*

/*
 * https://en.wikipedia.org/wiki/CIE_1931_color_space
 * https://ko.wikipedia.org/wiki/CIELAB_%EC%83%89_%EA%B3%B5%EA%B0%84
 * https://en.wikipedia.org/wiki/Color_difference#cite_note-20
 * https://m.blog.naver.com/atago59/222092830734
 * */
object ColorUtil {

    private const val RGB_THRESHOLD = 25.5 // 90 %
    private const val XYZ_THRESHOLD = 17.58 // 90 %
    private const val LAB_THRESHOLD = 14.18 // 90 %
//    private const val RGB_THRESHOLD = 12.75 // 95 %
//    private const val XYZ_THRESHOLD = 8.79 // 95 %
//    private const val LAB_THRESHOLD = 7.09 // 95 %

    private const val PI = Math.PI
    private const val DEGREES_TO_RADIANS = 0.017453292519943295
    private const val RADIANS_TO_DEGREES = 57.29577951308232
    private const val POW_25_7 = 6_103_515_625

    fun isSimilar(color1: Int, color2: Int): Boolean {
        val rgb1 = intArrayOf(Color.red(color1), Color.green(color1), Color.blue(color1))
        val rgb2 = intArrayOf(Color.red(color2), Color.green(color2), Color.blue(color2))

        if (euclideanDistanceRGB(rgb1, rgb2) < RGB_THRESHOLD)
            return true

        val xyz1 = DoubleArray(3)
        val xyz2 = DoubleArray(3)

        ColorUtils.RGBToXYZ(rgb1[0], rgb1[1], rgb1[2], xyz1)
        ColorUtils.RGBToXYZ(rgb2[0], rgb2[1], rgb2[2], xyz2)

//        if (euclideanDistanceXYZ(xyz1, xyz2) < XYZ_THRESHOLD)
//            return true

        val lab1 = DoubleArray(3)
        val lab2 = DoubleArray(3)

        ColorUtils.XYZToLAB(xyz1[0], xyz1[1], xyz1[2], lab1)
        ColorUtils.XYZToLAB(xyz2[0], xyz2[1], xyz2[2], lab2)

        return deltaE2000(lab1, lab2) < LAB_THRESHOLD
    }

    private fun euclideanDistanceRGB(rgb1: IntArray, rgb2: IntArray): Double {
        return sqrt(0.3 * (rgb1[0] - rgb2[0]).pow(2) + 0.59 * (rgb1[1] - rgb2[1]).pow(2) + 0.11 * (rgb1[2] - rgb2[2]).pow(2))
    }

    private fun euclideanDistanceXYZ(xyz1: DoubleArray, xyz2: DoubleArray): Double {
        return sqrt((xyz1[0] - xyz2[0]).pow(2) + (xyz1[1] - xyz2[1]).pow(2) + (xyz1[2] - xyz2[2]).pow(2))
    }

    private fun deltaE2000(Lab1: DoubleArray, Lab2: DoubleArray): Double {
        val (L1, a1, b1) = Lab1
        val (L2, a2, b2) = Lab2

        // ΔL'
        val dLp = L2 - L1

        // ΔC'
        val C1 = sqrt(a1.pow(2) + b1.pow(2))
        val C2 = sqrt(a2.pow(2) + b2.pow(2))
        val Cb = (C1 + C2) / 2
        val Cb_7 = Cb.pow(7)
        val G = sqrt(Cb_7 / (Cb_7 + POW_25_7))
        val ap1 = a1 + a1 / 2 * (1 - G)
        val ap2 = a2 + a2 / 2 * (1 - G)
        val Cp1 = sqrt(ap1.pow(2) + b1.pow(2))
        val Cp2 = sqrt(ap2.pow(2) + b2.pow(2))
        val dCp = Cp2 - Cp1

        // ΔH'
        val hp1 = atan2(b1, ap1).toDegrees() % 360
        val hp2 = atan2(b2, ap2).toDegrees() % 360
        val hp_diff = (hp1 - hp2).absoluteValue
        val hp_sum = hp1 + hp2
        val d_hp = if (hp_diff <= 180) {
            hp2 - hp1
        } else if (hp2 <= hp1) {
            hp2 - hp1 + 360
        } else {
            hp2 - hp1 - 360
        }
        val dHp = 2 * sqrt(Cp1 * Cp2) * sin((d_hp / 2).toRadians())

        // SL
        val Lb = (L1 + L2) / 2
        val SL = 1 + 0.015 * (Lb - 50).pow(2) / sqrt(20 + (Lb - 50).pow(2))

        // SC
        val Cpb = (Cp1 + Cp2) / 2
        val SC = 1 + 0.045 * Cpb

        // SH
        val Hpb = if (hp_diff <= 180) {
            hp_sum / 2
        } else if (hp_sum < 360) {
            (hp_sum + 360) / 2
        } else {
            (hp_sum - 360) / 2
        }
        val T = 1 - 0.17 * cos((Hpb - 30).toRadians())
                  + 0.24 * cos((2 * Hpb).toRadians())
                  + 0.32 * cos((3 * Hpb + 6).toRadians())
                  - 0.20 * cos((4 * Hpb - 63).toRadians())
        val SH = 1 + 0.015 * Cpb * T

        // RT
        val Cpb_7 = Cpb.pow(7)
        val RT = -2 * sqrt(Cpb_7 / (Cpb_7 + POW_25_7)) * sin((60 * exp(-((Hpb - 275) / 25).pow(2))).toRadians())

        return sqrt((dLp / SL).pow(2) + (dCp / SC).pow(2) + (dHp / SH).pow(2) + RT * (dCp / SC) * (dHp / SH))
    }

    private fun Double.toRadians(): Double = this * DEGREES_TO_RADIANS
    private fun Double.toDegrees(): Double = this * RADIANS_TO_DEGREES
    private fun Int.pow(n: Int): Int = this.toFloat().pow(n).toInt()
}