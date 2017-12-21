package com.xiaopo.flying.artist.base

import android.content.res.Resources

/**
 * @author wupanjie
 */

val Int.dp: Int
  get() = this * Resources.getSystem().displayMetrics.density.toInt()

val Int.px: Int
  get() = this