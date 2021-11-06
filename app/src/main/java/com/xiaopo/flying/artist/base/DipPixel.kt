package com.xiaopo.flying.artist.base

import android.content.res.Resources

/**
 * @author wupanjie
 */

val Int.dp: Float
  get() = this * Resources.getSystem().displayMetrics.density

val Int.px: Float
  get() = this.toFloat()

val Int.dpInt: Int
  get() = this * Resources.getSystem().displayMetrics.density.toInt()

val Int.pxInt: Int
  get() = this