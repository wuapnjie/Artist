package com.xiaopo.flying.artist.interactionimage

import android.content.Context
import android.graphics.Matrix
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.ImageView
import com.xiaopo.flying.artist.base.GestureKiller
import com.xiaopo.flying.artist.base.log

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
    "scale type : $assignedScaleType".log()
    // 自己控制Matrix
    scaleType = ScaleType.MATRIX

    gestureKiller.onNotTouchedListener = { x, y ->
      "Not Touched".log()
    }

    gestureKiller.onLongPressListener = { x, y ->
      "Long Touched -> ($x,$y)".log()
    }

    gestureKiller.onSingleTapListener = { x, y ->
      "Single Tap -> ($x,$y)".log()
    }

    gestureKiller.onDoubleTapListener = { x, y ->
      "Double Tap -> ($x,$y)".log()
    }

    gestureKiller.onDragListener = { x, y ->
      "Drag -> ($x,$y)".log()
      controlMatrix.postTranslate(x, y)
      changeMatrix()
    }

    gestureKiller.onReleaseListener = { x, y, gesture ->
      "Release -> ($x,$y), gesture -> $gesture".log()
    }

    gestureKiller.onZoomAndRotateListener = { midX, midY, deltaZoom, deltaAngle ->
      "Zoom And Rotation -> ($midX,$midY), deltaZoom -> $deltaZoom, delta Angle -> $deltaAngle".log()
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