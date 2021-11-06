package com.xiaopo.flying.artist.manyanimator

import android.animation.TimeInterpolator

/**
 * @author wupanjie
 */

typealias OnAnimateFractionUpdatedListener = (Float) -> Unit
typealias OnAnimateStartListener = () -> Unit
typealias OnAnimateEndListener = () -> Unit

class FractionFrameAnimator(
    duration: Long,
    repeat: Boolean = false,
    var onAnimateFractionUpdatedListener: OnAnimateFractionUpdatedListener? = null,
    var onAnimateStartListener: OnAnimateStartListener? = null,
    var onAnimateEndListener: OnAnimateEndListener? = null
) : FrameAnimator("FractionFrameAnimator", 60, duration, repeat) {

    override fun onValueUpdated(frame: Int, totalFrames: Int, cycle: Long) {
        val fraction = frame.toFloat() / totalFrames

        onAnimateFractionUpdatedListener?.invoke(fraction)
    }

    override fun onAnimateStart() {
        onAnimateStartListener?.invoke()
    }

    override fun onAnimateEnd() {
        onAnimateEndListener?.invoke()
    }

}

class ValueFrameAnimator private constructor() {

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

    var duration: Long = 1000L
    var repeat: Boolean = false
    var interpolator: TimeInterpolator? = null
    var onAnimateUpdatedListener: OnAnimateFractionUpdatedListener? = null
    var onAnimateStartListener: OnAnimateStartListener? = null
    var onAnimateEndListener: OnAnimateEndListener? = null

    fun start() {
        if (!hasInit) {
            initAnimator()
        }

        fractionFrameAnimator?.start()
    }

    fun pause() {
        if (hasInit) {
            return
        }

        fractionFrameAnimator?.pause()
    }

    fun resume() {
        if (hasInit) {
            return
        }

        fractionFrameAnimator?.resume()
    }

    fun cancel() {
        fractionFrameAnimator?.cancel()
    }

    private fun initAnimator() {
        fractionFrameAnimator = FractionFrameAnimator(duration, repeat, onAnimateFractionUpdatedListener = { fraction ->
            val calc = interpolator?.getInterpolation(fraction) ?: fraction
            val actual = start + calc * (end - start)

            onAnimateUpdatedListener?.invoke(actual)
        }, onAnimateStartListener = onAnimateStartListener, onAnimateEndListener = onAnimateEndListener)
    }

}
