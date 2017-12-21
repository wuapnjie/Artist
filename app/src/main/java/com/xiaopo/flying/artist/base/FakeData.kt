package com.xiaopo.flying.artist.base

/**
 * @author wupanjie
 */

fun strings(size : Int) : ArrayList<Any>{
  val list = arrayListOf<Any>()
  for (i in 0 until size){
    list += "Item $i"
  }

  return list
}