package com.xiaopo.flying.artist.base

import android.util.Log

/**
 * @author wupanjie
 */
fun Any.logd(message: String): Unit {
  Log.d(this.javaClass.simpleName, message)
}