package com.xiaopo.flying.artist.manyanimator

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.os.Build
import java.util.ArrayList

/**
 * @author wupanjie
 */

typealias OnAnimateFractionUpdatedListener = (Float) -> Unit
typealias OnAnimateStartListener = () -> Unit
typealias OnAnimateEndListener = () -> Unit
typealias OnAnimateRepeatListener = () -> Unit
typealias OnAnimateCancelListener = () -> Unit

private class FractionFrameAnimator(
    duration: Long,
    repeat: Boolean = false,
    reverse: Boolean = false,
    startDelay: Long = 0L,
    private val onAnimateFractionUpdatedListener: OnAnimateFractionUpdatedListener,
    private val onAnimateStartListener: OnAnimateStartListener,
    private val onAnimateEndListener: OnAnimateEndListener,
    private val onAnimateRepeatListener: OnAnimateRepeatListener,
    private val onAnimateCancelListener: OnAnimateCancelListener
) : FrameAnimator("FractionFrameAnimator", 60, duration, startDelay, repeat) {

    var animateFraction: Float = 0f

    override fun onValueUpdated(frame: Int, totalFrames: Int, cycle: Long) {
        val fraction = frame.toFloat() / totalFrames
        animateFraction = fraction

        onAnimateFractionUpdatedListener.invoke(fraction)
    }

    override fun onAnimateStart() {
        onAnimateStartListener.invoke()
    }

    override fun onAnimateEnd() {
        onAnimateEndListener.invoke()
    }

    override fun onAnimateRepeat() {
        onAnimateRepeatListener.invoke()
    }

    override fun onAnimateCancel() {
        onAnimateCancelListener.invoke()
    }
}

class ValueFrameAnimator private constructor() : ValueAnimator() {

    companion object {

        // 暂时只支持两个float
        fun ofFloat(start: Float, end: Float): ValueFrameAnimator {
            return ValueFrameAnimator().apply {
                this.start = start
                this.end = end
            }
        }

    }

    private var hasInit = false
    private var fractionFrameAnimator: FractionFrameAnimator? = null

    private var start: Float = 0f
    private var end: Float = 1f

    private var startDelay: Long = 0L
    private var duration: Long = 1000L
    private val fraction: Float get() = fractionFrameAnimator?.animateFraction ?: 0f
    private var value: Any? = null

    var repeat: Boolean = false
    var reverse: Boolean = false

    private var timeInterpolator: TimeInterpolator? = null

    private val updateListeners: ArrayList<AnimatorUpdateListener> = ArrayList()

    private val onAnimateUpdatedListener: OnAnimateFractionUpdatedListener = { value ->
        updateListeners.forEach { it.onAnimationUpdate(this) }
    }
    private val onAnimateStartListener: OnAnimateStartListener = {
        listeners?.forEach {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.onAnimationStart(this@ValueFrameAnimator, reverse)
            } else {
                it.onAnimationStart(this@ValueFrameAnimator)
            }
        }
    }
    private val onAnimateEndListener: OnAnimateEndListener = {
        listeners?.forEach {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                it.onAnimationEnd(this@ValueFrameAnimator, reverse)
            } else {
                it.onAnimationEnd(this@ValueFrameAnimator)
            }
        }
    }

    private val onAnimateRepeatListener: OnAnimateRepeatListener = {
        listeners?.forEach { it.onAnimationRepeat(this@ValueFrameAnimator) }
    }

    private val onAnimateCancelListener: OnAnimateCancelListener = {
        listeners?.forEach { it.onAnimationCancel(this@ValueFrameAnimator) }
    }

    override fun start() {
        if (!hasInit) {
            initAnimator()
        }

        fractionFrameAnimator?.start()
    }

    override fun pause() {
        if (!hasInit) {
            return
        }

        fractionFrameAnimator?.pause()
    }

    override fun resume() {
        if (!hasInit) {
            return
        }

        fractionFrameAnimator?.resume()
    }

    override fun getStartDelay(): Long {
        return startDelay
    }

    override fun setStartDelay(startDelay: Long) {
        this.startDelay = startDelay
    }

    override fun setDuration(duration: Long): ValueAnimator {
        this.duration = duration
        return this
    }

    override fun getDuration(): Long {
        return duration
    }

    override fun setInterpolator(value: TimeInterpolator?) {
        this.timeInterpolator = value
    }

    override fun isRunning(): Boolean {
        return fractionFrameAnimator?.isRunning ?: false
    }

    override fun cancel() {
        fractionFrameAnimator?.cancel()
    }

    override fun addUpdateListener(listener: AnimatorUpdateListener?) {
        listener ?: return
        updateListeners.add(listener)
    }

    override fun removeAllUpdateListeners() {
        updateListeners.clear()
    }

    override fun removeUpdateListener(listener: AnimatorUpdateListener?) {
        listener ?: return
        updateListeners.remove(listener)
    }

    override fun getAnimatedFraction(): Float {
        return fraction
    }

    override fun getAnimatedValue(): Any? {
        return value
    }

    override fun getAnimatedValue(propertyName: String?): Any {
        // TODO 待实现
        return super.getAnimatedValue(propertyName)
    }

    override fun setRepeatMode(value: Int) {
        // TODO 待实现
    }

    override fun setRepeatCount(value: Int) {
        // TODO 待实现
    }

    override fun reverse() {
        // TODO 待实现
    }

    private fun initAnimator() {
        fractionFrameAnimator = FractionFrameAnimator(
            duration,
            repeat,
            reverse,
            startDelay,
            onAnimateFractionUpdatedListener = { fraction ->
                val calc = timeInterpolator?.getInterpolation(fraction) ?: fraction
                val actual = start + calc * (end - start)

                this@ValueFrameAnimator.value = actual

                onAnimateUpdatedListener.invoke(actual)
            },
            onAnimateStartListener = onAnimateStartListener,
            onAnimateEndListener = onAnimateEndListener,
            onAnimateRepeatListener = onAnimateRepeatListener,
            onAnimateCancelListener = onAnimateCancelListener
        )
    }

}
