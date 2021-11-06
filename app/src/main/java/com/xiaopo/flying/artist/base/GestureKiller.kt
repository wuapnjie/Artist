package com.xiaopo.flying.artist.base

import android.content.Context
import android.graphics.PointF
import android.graphics.RectF
import android.os.Handler
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.abs

/**
 * @author wupanjie
 */

internal typealias OnNotTouchedListener = (touchedX: Float, touchedY: Float) -> Unit
internal typealias OnSingleTapListener = (tapX: Float, tapY: Float) -> Unit
internal typealias OnDoubleTabListener = (tapX: Float, tapY: Float) -> Unit
internal typealias OnZoomAndRotateListener = (midPointX: Float, midPointY: Float, deltaZoom: Float, deltaAngle: Float) -> Unit
internal typealias OnDragListener = (dx: Float, dy: Float) -> Unit
internal typealias OnLongPressListener = (pressX: Float, pressY: Float) -> Unit
internal typealias OnReleaseListener = (releaseX: Float, releaseY: Float, gesture: GestureType) -> Unit

enum class GestureType {
  NONE,
  NOT_TOUCHED, // 没有触碰到
  SINGLE_TAP,  // 单击
  DOUBLE_TAP,  // 双击
  ZOOM_ROTATE, // 双指缩放及旋转
  DRAG,        // 拖动
  LONG_PRESS,  // 长按
  RELEASE      // 释放
}

class GestureKiller(val context: Context) {

  companion object {
    val LONG_PRESS_TIME = ViewConfiguration.getLongPressTimeout().toLong()
  }

  private val TOUCH_SLOP = ViewConfiguration.get(context).scaledTouchSlop
  private var gesture = GestureType.NONE

  private var downX = 0f;
  private var downY = 0f;

  var onNotTouchedListener: OnNotTouchedListener? = null
  var onSingleTapListener: OnSingleTapListener? = null
  var onDoubleTapListener: OnDoubleTabListener? = null
  var onZoomAndRotateListener: OnZoomAndRotateListener? = null
  var onDragListener: OnDragListener? = null
  var onLongPressListener: OnLongPressListener? = null
  var onReleaseListener: OnReleaseListener? = null

  val detectBounds = RectF()

  private val midPoint = PointF()
  private val handler = Handler()

  private var lastX = 0f
  private var lastY = 0f

  private var lastDistance = 0f
  private var lastAngle = 0f

  private val gestureDetector = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
    override fun onSingleTapConfirmed(e: MotionEvent): Boolean {
      gesture = GestureType.SINGLE_TAP
      onSingleTapListener?.invoke(e.x, e.y)
      onReleaseListener?.invoke(e.x, e.y, gesture)
      return true
    }

    override fun onDoubleTap(e: MotionEvent): Boolean {
      gesture = GestureType.DOUBLE_TAP
      onDoubleTapListener?.invoke(e.x, e.y)
      onReleaseListener?.invoke(e.x, e.y, gesture)
      return true
    }
  })

  private val longPressRunnable = Runnable {
    gesture = GestureType.LONG_PRESS
    onLongPressListener?.invoke(downX, downY)
  }

  fun fuckTouchEvent(event: MotionEvent): Boolean {
    val consumed = gestureDetector.onTouchEvent(event)

    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        downX = event.x
        downY = event.y

        lastX = downX
        lastY = downY

        if (!detectBounds.contains(downX, downY)) {
          onNotTouchedListener?.invoke(downX, downY)
          "Not touched,touched ($downX,$downY),the bounds is $detectBounds".log()
          return false
        }

        handler.postDelayed(longPressRunnable, LONG_PRESS_TIME)
      }

      MotionEvent.ACTION_POINTER_DOWN -> {
        if ((event.pointerCount > 1) && (gesture == GestureType.DRAG || gesture == GestureType.LONG_PRESS) || gesture == GestureType.NONE) {
          lastAngle = calculateRotation(event)
          lastDistance = calculateDistance(event)
          calculateMidPoint(event, midPoint)
          gesture = GestureType.ZOOM_ROTATE
        }
      }

      MotionEvent.ACTION_MOVE -> {

        val currentX = event.x
        val currentY = event.y

        if (abs(currentX - downX) > TOUCH_SLOP || abs(currentY - downY) > TOUCH_SLOP) {
          if (gesture == GestureType.NONE) {
            gesture = GestureType.DRAG
          }
          handler.removeCallbacks(longPressRunnable)
        }

        if (gesture == GestureType.DRAG) {
          onDragListener?.invoke(currentX - lastX, currentY - lastY)
        } else if (gesture == GestureType.ZOOM_ROTATE) {
          if (event.pointerCount >= 2) {
            val currentDistance = calculateDistance(event)
            val currentRotation = calculateRotation(event)

            calculateMidPoint(event, midPoint)
            onZoomAndRotateListener?.invoke(midPoint.x, midPoint.y, currentDistance / lastDistance, currentRotation - lastAngle)

            lastDistance = currentDistance
            lastAngle = currentRotation
          }
        }

        lastX = currentX
        lastY = currentY
      }

      MotionEvent.ACTION_UP -> {
        handler.removeCallbacks(longPressRunnable)

        if (gesture == GestureType.DRAG ||
            gesture == GestureType.LONG_PRESS ||
            gesture == GestureType.ZOOM_ROTATE
            ) {
          onReleaseListener?.invoke(event.x, event.y, gesture)
        }
        gesture = GestureType.NONE
      }

      MotionEvent.ACTION_CANCEL -> {
        gesture = GestureType.NONE
      }

      MotionEvent.ACTION_POINTER_UP -> {
        if (event.pointerCount == 1) {
          gesture = GestureType.DRAG
        }
      }
    }

    return true
  }

  private fun calculateDistance(event: MotionEvent): Float {
    val x = event.getX(0) - event.getX(1)
    val y = event.getY(0) - event.getY(1)

    return Math.sqrt((x * x + y * y).toDouble()).toFloat()
  }

  private fun calculateMidPoint(event: MotionEvent, point: PointF) {
    point.x = (event.getX(0) + event.getX(1)) / 2
    point.y = (event.getY(0) + event.getY(1)) / 2
  }

  private fun calculateRotation(event: MotionEvent?): Float {
    return if (event == null || event.pointerCount < 2) {
      0f
    } else calculateRotation(event.getX(0), event.getY(0), event.getX(1), event.getY(1))
  }

  private fun calculateRotation(x1: Float, y1: Float, x2: Float, y2: Float): Float {
    val x = (x1 - x2).toDouble()
    val y = (y1 - y2).toDouble()
    val radians = Math.atan2(y, x)
    return Math.toDegrees(radians).toFloat()
  }
}

