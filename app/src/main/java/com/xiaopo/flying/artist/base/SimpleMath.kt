package com.xiaopo.flying.artist.base

import androidx.annotation.IntRange

/**
 * @author wupanjie
 */

fun clamp(min: Float, value: Float, max: Float): Float {
  return Math.min(Math.max(min, value), max)
}

fun Int.modifyAlpha(@IntRange(from = 0, to = 255) alpha: Int) = (this and 0x00ffffff) or (alpha shl 24)

fun Float.toRadian() = Math.toRadians(this.toDouble()).toFloat()