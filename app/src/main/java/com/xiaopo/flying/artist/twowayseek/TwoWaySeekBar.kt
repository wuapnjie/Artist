package com.xiaopo.flying.artist.twowayseek

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.clamp
import com.xiaopo.flying.artist.base.dp
import com.xiaopo.flying.artist.base.modifyAlpha
import kotlin.math.sqrt


/**
 * @author wupanjie
 */
internal typealias OnIntSeekChangeListener = (Int) -> Unit
internal typealias OnFloatSeekChangeListener = (Float) -> Unit

class TwoWaySeekBar @JvmOverloads constructor(context: Context,
                                              attr: AttributeSet? = null,
                                              defStyle: Int = 0)
  : View(context, attr, defStyle) {

  companion object {
    val MODE_INT = 0
    val MODE_FLOAT = 1
  }

  private var minValue = 0f
  private var maxValue = 50f
  private var initialValue = (minValue + maxValue) / 2
  private var currentValue = initialValue
  private var lastValue = currentValue

  private val bounds = Rect()
  private val line = Line()
  private val handle = Handle()
  private val shadowLine = Line()

  private var downValue = currentValue
  private var downX = 0f
  private var downY = 0f

  private var canSeeking = false

  var onIntSeekChangeListener: OnIntSeekChangeListener? = null
  var onFloatSeekChangeListener: OnFloatSeekChangeListener? = null

  // some attrs
  var initialHandleColor = Color.WHITE
    set(value) {
      field = value
      invalidate()
    }
  var handleColor: Int = Color.rgb(78, 134, 236)
    set(value) {
      field = value
      invalidate()
    }
  var lineColor: Int = Color.rgb(102, 102, 102)
    set(value) {
      field = value
      invalidate()
    }
  var mode = MODE_FLOAT

  init {
    attr?.let {
      val typedArray = context.obtainStyledAttributes(attr, R.styleable.TwoWaySeekBar)

      val lineWidth = typedArray.getDimensionPixelSize(R.styleable.TwoWaySeekBar_lineWidth, 3.dp).toFloat()
      val handleRadius = typedArray.getDimensionPixelSize(R.styleable.TwoWaySeekBar_handleRadius, 8.dp).toFloat()
      minValue = typedArray.getFloat(R.styleable.TwoWaySeekBar_maxValue, -10f)
      maxValue = typedArray.getFloat(R.styleable.TwoWaySeekBar_minValue, 10f)
      initialValue = typedArray.getFloat(R.styleable.TwoWaySeekBar_initialValue, (minValue + maxValue) / 2)
      lineColor = typedArray.getColor(R.styleable.TwoWaySeekBar_lineColor, Color.rgb(102, 102, 102))
      handleColor = typedArray.getColor(R.styleable.TwoWaySeekBar_handleColor, Color.rgb(78, 134, 236))
      initialHandleColor = typedArray.getColor(R.styleable.TwoWaySeekBar_initialHandleColor, Color.WHITE)

      if (minValue > maxValue) {
        throw IllegalArgumentException("min value greater than max value")
      }

      initialValue = clamp(minValue, initialValue, maxValue)

      line.paint.strokeWidth = lineWidth
      line.paint.color = lineColor
      shadowLine.paint.strokeWidth = lineWidth
      shadowLine.paint.color = handleColor
      handle.radius = handleRadius
      handle.extraRadius = 1.5f * handleRadius
      handle.paint.color = handleColor

      typedArray.recycle()

      currentValue = initialValue
    }
    shadowLine.paint.strokeCap = Paint.Cap.SQUARE
  }

  override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    // support wrap_content
    val widthSize = View.MeasureSpec.getSize(widthMeasureSpec)

    val heightSize = View.MeasureSpec.getSize(heightMeasureSpec)

    val suggestWidth = 300.dp + paddingLeft + paddingRight
    val suggestHeight = ((handle.radius + handle.extraRadius) * 4).toInt() + paddingTop + paddingBottom

    if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT && layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
      setMeasuredDimension(suggestWidth, suggestHeight)
    } else if (layoutParams.width == ViewGroup.LayoutParams.WRAP_CONTENT) {
      setMeasuredDimension(suggestWidth, heightSize)
    } else if (layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT) {
      setMeasuredDimension(widthSize, suggestHeight)
    }
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    bounds.set(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom)

    val centerY = h.toFloat() / 2

    line.start.set(paddingLeft.toFloat(), centerY)
    line.end.set((w - paddingRight).toFloat(), centerY)

    val initialX = calculatePosition(initialValue)
    shadowLine.start.set(initialX, centerY)

    val currentX = calculatePosition(currentValue)
    handle.center.set(currentX, centerY)
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    line.draw(canvas)
    determineHandle()
    handle.draw(canvas)
    determineShadowLine()
    shadowLine.draw(canvas)
  }

  private fun determineShadowLine() {
    shadowLine.end.set(handle.center)
  }

  private fun determineHandle() {
    handle.center.x =
        if (mode == MODE_INT)
          calculatePosition(currentValue.toInt())
        else
          calculatePosition(currentValue)

    handle.needDrawShadow = canSeeking
    if (currentValue.toInt() == initialValue.toInt()) {
      handle.center.x = calculatePosition(initialValue)
      handle.paint.color = initialHandleColor
      shadowLine.paint.color = initialHandleColor
    } else {
      handle.paint.color = handleColor
      shadowLine.paint.color = handleColor
    }
  }

  private fun calculatePosition(value: Float) = bounds.left + (value - minValue) / (maxValue - minValue) * bounds.width()

  private fun calculatePosition(value: Int) = bounds.left + (value - minValue) / (maxValue - minValue) * bounds.width()

  private fun calculateValue(position: Float) = minValue + (position - bounds.left) / bounds.width() * (maxValue - minValue)

  override fun onTouchEvent(event: MotionEvent): Boolean {
    val eventX = event.x
    val eventY = event.y

    when (event.actionMasked) {
      MotionEvent.ACTION_DOWN -> {
        downX = eventX
        downY = eventY
        canSeeking = handle.contains(downX, downY)

        if (!canSeeking && line.contains(downX, downY)) {
          currentValue = calculateValue(downX)
          canSeeking = true
        }

        downValue = currentValue
        invalidate()
        return canSeeking
      }

      MotionEvent.ACTION_MOVE -> {
        if (canSeeking) {
          val delta = (eventX - downX) / bounds.width() * (maxValue - minValue)
          currentValue = clamp(minValue, downValue + delta, maxValue)
          if (currentValue != lastValue) {
            dispatchSeekChangeEvent()
            lastValue = currentValue
            invalidate()
          }
        }
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        canSeeking = false
        invalidate()
      }

    }
    return super.onTouchEvent(event)
  }

  private fun dispatchSeekChangeEvent() {
    onIntSeekChangeListener?.invoke(currentValue.toInt())
    onFloatSeekChangeListener?.invoke(currentValue)
  }

  fun setValue(value: Float) {
    currentValue = clamp(minValue, value, maxValue)
    invalidate()
  }

  fun config(minValue: Float, maxValue: Float, initialValue: Float) {
    if (minValue > maxValue) {
      throw IllegalArgumentException("min value greater than max value")
    }
    this.minValue = minValue
    this.maxValue = maxValue
    this.initialValue = clamp(minValue, initialValue, maxValue)
    this.currentValue = clamp(minValue, currentValue, maxValue)
    invalidate()
  }

  private class Line(
      val start: PointF = PointF(0f, 0f),
      val end: PointF = PointF(0f, 0f)
  ) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val strokeWidth = 3.dp.toFloat()

    init {
      paint.strokeCap = Paint.Cap.ROUND
      paint.strokeWidth = strokeWidth
    }

    fun draw(canvas: Canvas) {
      canvas.drawLine(start.x, start.y, end.x, end.y, paint)
    }

    fun contains(x: Float, y: Float) = x >= start.x && x < end.x && y >= start.y - strokeWidth * 5 && y < start.y + strokeWidth * 5
  }

  private class Handle(
      var radius: Float = 7.dp.toFloat(),
      var extraRadius: Float = radius,
      val center: PointF = PointF(0f, 0f)
  ) {
    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    var needDrawShadow = false

    fun draw(canvas: Canvas) {
      if (needDrawShadow) {
        shadowPaint.color = paint.color.modifyAlpha(160)
        canvas.drawCircle(center.x, center.y, radius + extraRadius, shadowPaint)
      }
      canvas.drawCircle(center.x, center.y, radius, paint)
    }

    fun contains(x: Float, y: Float): Boolean
        = sqrt((x - center.x) * (x - center.x) + (y - center.y) * (y - center.y)) <= radius + extraRadius

  }
}