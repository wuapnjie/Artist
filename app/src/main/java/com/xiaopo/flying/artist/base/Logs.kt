package com.xiaopo.flying.artist.base

import android.util.Log

/**
 * @author wupanjie
 */
fun Any.log(tag: String = "Artist") {
    Log.d(tag, toString())
}