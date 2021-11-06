package com.xiaopo.flying.artist.elasticdrag

import android.content.Context
import androidx.core.view.NestedScrollingParent2
import androidx.core.view.ViewCompat
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.xiaopo.flying.artist.base.dp
import com.xiaopo.flying.artist.base.logd
import kotlin.math.abs

/**
 * @author wupanjie
 */
internal typealias ElasticDragListener = (elasticOffset: Float,
                                          elasticOffsetPixels: Float,
                                          rawOffset: Float,
                                          rawOffsetPixels: Float) -> Unit

internal typealias DragDismissListener = () -> Unit

class ElasticDragLayout @JvmOverloads constructor(context: Context,
                                                  attr: AttributeSet? = null,
                                                  defStyle: Int = 0)
  : FrameLayout(context, attr, defStyle), NestedScrollingParent2 {

  private var dragDismissDistance = 112.dp
  private var dragDismissFraction = -1f
  private var dragDismissScale = 0.95f
  private var shouldScale = true
  private var dragElasticity = 0.8f

  private var totalDrag = 0f
  private var draggingDown = false
  private var draggingUp = false

  private val dragListeners = arrayListOf<ElasticDragListener>()
  private val dismissListeners = arrayListOf<DragDismissListener>()

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    if (dragDismissFraction > 0f) {
      dragDismissDistance = h * dragDismissFraction.toInt()
    }
  }

  override fun onStartNestedScroll(child: View, target: View, axes: Int, type: Int): Boolean {
    return axes and ViewCompat.SCROLL_AXIS_VERTICAL != 0
  }

  override fun onNestedScrollAccepted(child: View, target: View, axes: Int, type: Int) {

  }

  override fun onNestedPreScroll(target: View, dx: Int, dy: Int, consumed: IntArray, type: Int) {
    if (draggingDown && dy > 0 || draggingUp && dy < 0) {
      dragScale(dy)
      consumed.set(1, dy)
    }
  }

  override fun onNestedScroll(target: View, dxConsumed: Int, dyConsumed: Int, dxUnconsumed: Int, dyUnconsumed: Int, type: Int) {
    dragScale(dyUnconsumed)
  }

  override fun onStopNestedScroll(target: View, type: Int) {
    logd("total drag : ${totalDrag}")
    if (abs(totalDrag) >= dragDismissDistance) {
      dismissListeners.forEach { it.invoke() }
    } else {
      animate()
          .translationY(0f)
          .scaleX(1f)
          .scaleY(1f)
          .setDuration(200L)
          .setInterpolator(FastOutSlowInInterpolator)
          .setListener(null)
          .start()
      totalDrag = 0f
      draggingUp = false
      draggingDown = draggingUp
      dragListeners.forEach { it.invoke(0f, 0f, 0f, 0f) }
    }
  }

  private fun dragScale(scroll: Int) {
    if (scroll == 0) return

    totalDrag += scroll

    // track the direction & set the pivot point for scaling
    // don't double track i.e. if start dragging down and then reverse, keep tracking as
    // dragging down until they reach the 'natural' position
    if (scroll < 0 && !draggingUp && !draggingDown) {
      draggingDown = true
      if (shouldScale) pivotY = height.toFloat()
    } else if (scroll > 0 && !draggingDown && !draggingUp) {
      draggingUp = true
      if (shouldScale) pivotY = 0f
    }
    // how far have we dragged relative to the distance to perform a dismiss
    // (0–1 where 1 = dismiss distance). Decreasing logarithmically as we approach the limit
    var dragFraction = Math.log10((1 + Math.abs(totalDrag) / dragDismissDistance).toDouble()).toFloat()

    // calculate the desired translation given the drag fraction
    var dragTo = dragFraction * dragDismissDistance * dragElasticity

    if (draggingUp) {
      // as we use the absolute magnitude when calculating the drag fraction, need to
      // re-apply the drag direction
      dragTo *= -1f
    }
    translationY = dragTo

    if (shouldScale) {
      val scale = 1 - (1 - dragDismissScale) * dragFraction
      scaleX = scale
      scaleY = scale
    }

    // if we've reversed direction and gone past the settle point then clear the flags to
    // allow the list to get the scroll events & reset any transforms
    if (draggingDown && totalDrag >= 0 || draggingUp && totalDrag <= 0) {
      dragFraction = 0f
      dragTo = dragFraction
      totalDrag = dragTo
      draggingUp = false
      draggingDown = draggingUp
      translationY = 0f
      scaleX = 1f
      scaleY = 1f
    }
    dragListeners.forEach {
      it.invoke(dragFraction,
          dragTo,
          Math.min(1f, Math.abs(totalDrag) / dragDismissDistance),
          totalDrag)
    }
  }

  fun addDragListener(listener: ElasticDragListener) {
    dragListeners += listener
  }

  fun addDismissListener(listener: DragDismissListener) {
    dismissListeners += listener
  }

  fun removeDragListener(listener: ElasticDragListener) {
    dragListeners -= listener
  }

  fun removeDismissListener(listener: DragDismissListener) {
    dismissListeners -= listener
  }
}