package com.xiaopo.flying.artist.manyanimator.frame

import android.animation.TimeInterpolator
import android.animation.TypeEvaluator
import android.animation.ValueAnimator
import android.os.Build
import android.util.Log
import java.util.ArrayList

/**
 * @author wupanjie
 */

typealias OnAnimateFractionUpdatedListener = (Float) -> Unit
typealias OnAnimateStartListener = () -> Unit
typealias OnAnimateEndListener = () -> Unit
typealias OnAnimateRepeatListener = () -> Unit
typealias OnAnimateCancelListener = () -> Unit

class ValueFrameAnimator private constructor(
    private val values: FloatArray,
    private val valueConvert: ((Float) -> Any) = { it -> it }
) : ValueAnimator() {

    companion object {

        fun ofFloat(vararg values: Float): ValueFrameAnimator {
            return ValueFrameAnimator(values)
        }

        fun ofInt(vararg values: Int): ValueFrameAnimator {
            val floatValue = FloatArray(values.size) { i -> values[i].toFloat() }
            return ValueFrameAnimator(floatValue) { it.toInt() }
        }

    }

    //    private var start: Float = values.first()
    //    private var end: Float = values.last()
    private val animateFractionStages: ArrayList<AnimateFractionStage> = ArrayList(values.size)

    init {
        val stepSize = values.size - 1
        val step = 1f / stepSize

        for (i in 0 until stepSize) {
            val valueFrom = values[i]
            val valueTo = values[i + 1]
            val fractionFrom = i * step
            val fractionTo = (i + 1) * step
            val fractionStage = AnimateFractionStage(valueFrom, valueTo, fractionFrom, fractionTo)
            animateFractionStages.add(fractionStage)
        }

    }

    private var hasInit = false
    private var fractionFrameAnimator: FractionFrameAnimator? = null

    var name: String = "ValueFrameAnimator"
    private var startDelay: Long = 0L
    private var duration: Long = 1000L
    private val fraction: Float get() = fractionFrameAnimator?.animateFraction ?: 0f
    private var actualValue: Any? = null

    var repeat: Boolean = false
    var reverse: Boolean = false

    private var timeInterpolator: TimeInterpolator? = null
    private var evaluator: TypeEvaluator<Any>? = null

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
        Log.e("DDD", name, Exception())
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

    override fun isPaused(): Boolean {
        return fractionFrameAnimator?.isPaused ?: false
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

    override fun getInterpolator(): TimeInterpolator? {
        return timeInterpolator
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
        return actualValue
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

    override fun setEvaluator(value: TypeEvaluator<*>?) {
        value ?: return
        this.evaluator = value as TypeEvaluator<Any>?
    }

    private fun initAnimator() {
        fractionFrameAnimator = FractionFrameAnimator(
            name,
            duration,
            repeat,
            reverse,
            startDelay,
            onAnimateFractionUpdatedListener = { fraction ->
                val fractionStage = animateFractionStages.first { fraction in it.fractionRange }

                val calcValue = fractionStage.calc(fraction, timeInterpolator)

                this@ValueFrameAnimator.actualValue =
                    evaluator?.let { e -> fractionStage.evaluate(fraction, e) }
                        ?: valueConvert(calcValue)

                onAnimateUpdatedListener.invoke(calcValue)
            },
            onAnimateStartListener = onAnimateStartListener,
            onAnimateEndListener = onAnimateEndListener,
            onAnimateRepeatListener = onAnimateRepeatListener,
            onAnimateCancelListener = onAnimateCancelListener
        )
    }

    private class FractionFrameAnimator(
        name: String,
        duration: Long,
        repeat: Boolean = false,
        reverse: Boolean = false,
        startDelay: Long = 0L,
        private val onAnimateFractionUpdatedListener: OnAnimateFractionUpdatedListener,
        private val onAnimateStartListener: OnAnimateStartListener,
        private val onAnimateEndListener: OnAnimateEndListener,
        private val onAnimateRepeatListener: OnAnimateRepeatListener,
        private val onAnimateCancelListener: OnAnimateCancelListener
    ) : FrameAnimator(name, 60, duration, startDelay, repeat) {

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


    private inner class AnimateFractionStage(
        val valueFrom: Float,
        val valueTo: Float,
        val fractionFrom: Float,
        val fractionTo: Float
    ) {

        val fractionRange = fractionFrom..fractionTo

        fun calc(
            fraction: Float,
            interpolator: TimeInterpolator? = null
        ): Float {

            var intervalFraction = (fraction - fractionFrom) / (fractionTo - fractionFrom)

            interpolator?.let {
                intervalFraction = it.getInterpolation(intervalFraction)
            }

            return valueFrom + (valueTo - valueFrom) * ((intervalFraction - fractionFrom) / (fractionTo - fractionFrom))
        }

        fun evaluate(fraction: Float, evaluator: TypeEvaluator<Any>): Any? {
            var intervalFraction = (fraction - fractionFrom) / (fractionTo - fractionFrom)

            timeInterpolator?.let {
                intervalFraction = it.getInterpolation(intervalFraction)
            }

            return evaluator.evaluate(
                intervalFraction,
                valueConvert(valueFrom),
                valueConvert(valueTo)
            )
        }

    }

}
