package com.xiaopo.flying.artist.emojiexplode

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import androidx.core.util.Pools
import android.util.AttributeSet
import android.view.View
import android.view.animation.DecelerateInterpolator
import com.xiaopo.flying.animatekit.quickAnimate
import com.xiaopo.flying.artist.base.dpInt
import com.xiaopo.flying.artist.base.log
import com.xiaopo.flying.artist.base.toRadian
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * @author wupanjie
 */
class EmojiExplodeView @JvmOverloads constructor(context: Context,
                                                 attr: AttributeSet? = null,
                                                 defStyle: Int = 0)
  : View(context, attr, defStyle) {

  private val bounds = Rect()
  private val random = Random()
  private var emojis = arrayListOf<Drawable>()
  private var bullets = arrayListOf<EmojiBullet>()
  private val pool = Pools.SimplePool<EmojiBullet>(100)

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    bounds.set(paddingLeft, paddingTop, w - paddingRight, h - paddingBottom)

    bullets.forEach { bullet ->
      bullet.center.set(bounds.centerX().toFloat(), bounds.centerY().toFloat())
      invalidate()
    }

  }

  fun explode() {
    val currentBullets = arrayListOf<EmojiBullet>()
    emojis.forEach {
      var bullet = pool.acquire()
      if (bullet == null) {
        bullet = EmojiBullet(it)
      } else {
        bullet.emojiDrawable = it
      }

      currentBullets.add(bullet)
    }

    bullets.addAll(currentBullets)

    currentBullets.forEach { bullet ->
      bullet.center.set(bounds.centerX().toFloat(), bounds.centerY().toFloat())

      bullet.emitSpeed = 56.dpInt + random.nextFloat() * 32.dpInt
      bullet.emitAngle = -20 + random.nextFloat() * 160
      bullet.emit()
    }
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    bullets.forEach { it.draw(canvas) }
  }

  fun fillEmoji(emoji: Drawable) {
    emojis.add(emoji)

  }

  private inner class EmojiBullet(var emojiDrawable: Drawable) {
    val paint: Paint = Paint(Paint.FILTER_BITMAP_FLAG)
    var emitSpeed: Float = 80f
    var emitAngle: Float = 45f
    var alpha = 255
    val width = emojiDrawable.intrinsicWidth.toFloat()
    val height = emojiDrawable.intrinsicHeight.toFloat()
    // 中心点
    val center = PointF(width / 2, height / 2)
    private val emitCenter = PointF()
    val emojiBounds = Rect()
      get() {
        return field.determineBounds()
      }
    val animateController = quickAnimate {
      duration = 600L
      interpolator = DecelerateInterpolator()
      play {
        targets = arrayListOf(this@EmojiExplodeView)
        floatValues(0f, 5f) { _, value ->
          // 无阻力的以一定角度初速度发射的位置变化
          "(${emitSpeed * cos(emitAngle.toRadian()) * value},${(emitSpeed * sin(emitAngle.toRadian()) * value - 9.8f * value * value / 2)})".log()
          center.x = emitCenter.x + emitSpeed * cos(emitAngle.toRadian()) * value
          // because left-top is (0,0)
          center.y = emitCenter.y - (emitSpeed * sin(emitAngle.toRadian()) * value - 9.8f * value * value / 2)
          alpha = (255f * (1 - value / 5)).toInt()
          invalidate()
        }

        onEnd = {
          bullets.remove(this@EmojiBullet)
          pool.release(this@EmojiBullet)
        }
      }
    }

    init {
      paint.isAntiAlias = true
    }

    fun emit() {
      animateController.cancel()
      emitCenter.set(center.x, center.y)
      animateController.start()
    }

    fun draw(canvas: Canvas) {
      emojiDrawable.bounds = emojiBounds
      emojiDrawable.alpha = alpha
      emojiDrawable.draw(canvas)
    }

    fun Rect.determineBounds() = apply {
      set((center.x - width / 2).toInt(), (center.y - height / 2).toInt(), (center.x + width / 2).toInt(), (center.y + height / 2).toInt())
    }
  }
}

