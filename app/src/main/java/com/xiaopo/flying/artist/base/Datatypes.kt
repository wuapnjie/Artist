package com.xiaopo.flying.artist.base

import android.content.Context
import android.content.Intent

/**
 * @author wupanjie
 */

data class Work(val name: String, val clazz: Class<*>)

fun Context.intentTo(work: Work) {
  val intent = Intent(this, work.clazz)
  startActivity(intent)
}