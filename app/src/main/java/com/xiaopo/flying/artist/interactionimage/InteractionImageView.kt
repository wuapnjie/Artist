package com.xiaopo.flying.artist.interactionimage

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.xiaopo.flying.artist.base.GestureKiller
import com.xiaopo.flying.artist.base.logd

/**
 * @author wupanjie
 */
class InteractionImageView @JvmOverloads constructor(context: Context,
                                                     attr: AttributeSet? = null,
                                                     defStyle: Int = 0)
  : ImageView(context, attr, defStyle) {


  private val assignedScaleType: ScaleType = scaleType
  private val controlMatrix = Matrix()
  private var hasFrame = false

  private val drawableBounds = RectF()
  private val currentBounds = RectF()
  private val gestureKiller = GestureKiller(context)

  init {
    logd("scale type : $assignedScaleType")
    // 自己控制Matrix
    scaleType = ScaleType.MATRIX

    gestureKiller.onNotTouchedListener = { x, y ->
      logd("Not Touched")
    }

    gestureKiller.onLongPressListener = { x, y ->
      logd("Long Touched -> ($x,$y)")
    }

    gestureKiller.onSingleTapListener = { x, y ->
      logd("Single Tap -> ($x,$y)")
    }

    gestureKiller.onDoubleTapListener = { x, y ->
      logd("Double Tap -> ($x,$y)")
    }

    gestureKiller.onDragListener = { x, y ->
      logd("Drag -> ($x,$y)")
      controlMatrix.postTranslate(x, y)
      changeMatrix()
    }

    gestureKiller.onReleaseListener = { x, y, gesture ->
      logd("Release -> ($x,$y), gesture -> $gesture")

    }

    gestureKiller.onZoomAndRotateListener = { midX, midY, deltaZoom, deltaAngle ->
      logd("Zoom And Rotation -> ($midX,$midY), deltaZoom -> $deltaZoom, delta Angle -> $deltaAngle")
      controlMatrix.postScale(deltaZoom, deltaZoom, currentBounds.centerX(), currentBounds.centerY())
      controlMatrix.postRotate(deltaAngle, currentBounds.centerX(), currentBounds.centerY())
      changeMatrix()
    }
  }

  private fun changeMatrix() {
    imageMatrix = controlMatrix
    controlMatrix.mapRect(currentBounds, drawableBounds)
  }

  override fun setFrame(l: Int, t: Int, r: Int, b: Int): Boolean {
    val changed = super.setFrame(l, t, r, b)
    hasFrame = true
    return changed
  }


  override fun onTouchEvent(event: MotionEvent): Boolean {
    drawable?.let {
      drawableBounds.set(0f, 0f, it.intrinsicWidth.toFloat(), it.intrinsicHeight.toFloat())
      controlMatrix.mapRect(gestureKiller.detectBounds, drawableBounds)
    }
    return gestureKiller.fuckTouchEvent(event)
  }


}