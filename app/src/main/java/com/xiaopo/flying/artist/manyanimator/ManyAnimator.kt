package com.xiaopo.flying.artist.manyanimator

import android.animation.*
import android.annotation.TargetApi
import android.graphics.Path
import android.graphics.PathMeasure
import android.os.Build
import android.util.Property
import android.view.View
import com.xiaopo.flying.artist.manyanimator.frame.FrameAnimatorSet
import com.xiaopo.flying.artist.manyanimator.frame.ValueFrameAnimator
import java.util.concurrent.ConcurrentHashMap

/**
 * @author wupanjie
 */
internal typealias Listener = () -> Unit

fun quickAnimate(useFrameAnimator: Boolean, init: ManyAnimator.() -> Unit) =
    ManyAnimator.Controller(ManyAnimator(useFrameAnimator).apply(init))

fun ManyAnimator.Controller.cancelAndStart() {
    cancel()
    start()
}

class ManyAnimator(private val useFrameAnimator: Boolean = false) {

    var duration: Long = -1L
    var interpolator: TimeInterpolator? = null

    private var currentAnimatorSet: AnAnimatorSet = if (useFrameAnimator) {
        FrameAnimatorSet()
    } else {
        AnAnimatorSet.wrap(AnimatorSet())
    }
    private var plays = arrayListOf<Animator>()

    var onStart: Listener? = null
    var onEnd: Listener? = null

    private fun createTogether() = currentAnimatorSet.apply {
        if (duration >= 0) {
            duration = this@ManyAnimator.duration
        }
        if (this@ManyAnimator.interpolator != null) {
            interpolator = this@ManyAnimator.interpolator
        }

        if (onStart != null || onEnd != null) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    onStart?.invoke()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })
        }
        playTogether(plays)
    }

    private fun createSequence() = currentAnimatorSet.apply {
        if (duration >= 0) {
            duration = this@ManyAnimator.duration
        }
        if (this@ManyAnimator.interpolator != null) {
            interpolator = this@ManyAnimator.interpolator
        }

        if (onStart != null || onEnd != null) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    onStart?.invoke()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })
        }
        playSequentially(plays)
    }

    private fun start() {
        currentAnimatorSet.cancel()
        currentAnimatorSet.removeAllListeners()
        if (onStart != null || onEnd != null) {
            currentAnimatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    onStart?.invoke()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })
        }
        currentAnimatorSet.playSequentially(plays)
        currentAnimatorSet.start()
    }

    private fun cancel() {
        currentAnimatorSet.cancel()
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun pause() {
        currentAnimatorSet.pause()
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private fun resume() {
        currentAnimatorSet.resume()
    }

    private fun isRunning(): Boolean {
        return currentAnimatorSet.isRunning
    }

    fun delay(time: Long) {
        val animator = if (useFrameAnimator)
            ValueFrameAnimator.ofFloat(0f, 1f).apply {
                name = "DelayFrameAnimator"
            }
        else
            ValueAnimator.ofFloat(0f, 1f)

        animator.duration = time
        plays.add(animator)
    }

    fun play(anim: AnAnimator.() -> Unit) {
        val animator = AnAnimator(useFrameAnimator).apply(anim).createAnimator()
        if (duration >= 0) {
            animator.duration = duration
        }
        if (interpolator != null) {
            animator.interpolator = interpolator
        }
        plays.add(animator)
    }

    fun together(init: ManyAnimator.() -> Unit) {
        val animator = ManyAnimator(useFrameAnimator).apply(init).createTogether()
        plays.add(animator)
    }

    fun sequence(init: ManyAnimator.() -> Unit) {
        val animator = ManyAnimator(useFrameAnimator).apply(init).createSequence()
        plays.add(animator)
    }

    class Controller(private val animator: ManyAnimator) {
        fun start() = apply { animator.start() }
        fun cancel() = animator.cancel()

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun pause() = animator.pause()

        @TargetApi(Build.VERSION_CODES.KITKAT)
        fun resume() = animator.resume()

        fun isRunning(): Boolean = animator.isRunning()
    }

    object ControllerCenter {

        private val controllers = ConcurrentHashMap<String, ManyAnimator.Controller>()

        fun register(key: String, controller: ManyAnimator.Controller) {
            controllers[key] = controller
        }

        fun unregister(key: String) {
            controllers.remove(key)
        }

        fun take(key: String): ManyAnimator.Controller? = controllers.remove(key)

        fun release() {
            controllers.clear()
        }
    }
}

/**
 * to let play together
 */
class AnAnimator(private val useFrameAnimator: Boolean) {

    private val animatorSet: AnAnimatorSet = if (useFrameAnimator) {
        FrameAnimatorSet()
    } else {
        AnAnimatorSet.wrap(AnimatorSet())
    }
    private val animators = arrayListOf<Animator>()

    var targets: List<View> = arrayListOf()
    var repeatCount = 0
    var duration = 300L
    var onStart: Listener? = null
    var onEnd: Listener? = null
    var interpolator: TimeInterpolator? = null
    var startDelay = 0L

    fun createAnimator(): Animator = animatorSet.apply {
        if (onStart != null || onEnd != null) {
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationStart(animation: Animator?) {
                    onStart?.invoke()
                }

                override fun onAnimationEnd(animation: Animator?) {
                    onEnd?.invoke()
                }
            })
        }

        duration = this@AnAnimator.duration
        interpolator = this@AnAnimator.interpolator
        startDelay = this@AnAnimator.startDelay
        playTogether(animators)
    }

    fun alpha(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.ALPHA, *values, onUpdate = onUpdate)
    }

    fun translationX(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.TRANSLATION_X, *values, onUpdate = onUpdate)
    }

    fun translationY(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.TRANSLATION_Y, *values, onUpdate = onUpdate)
    }

    fun translation(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        translationX(*values, onUpdate = onUpdate)
        translationY(*values, onUpdate = onUpdate)
    }

    fun scaleX(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.SCALE_X, *values, onUpdate = onUpdate)
    }

    fun scaleY(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.SCALE_Y, *values, onUpdate = onUpdate)
    }

    fun scale(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        scaleX(*values, onUpdate = onUpdate)
        scaleY(*values, onUpdate = onUpdate)
    }

    fun rotationX(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.ROTATION_X, *values, onUpdate = onUpdate)
    }

    fun rotationY(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.ROTATION_Y, *values, onUpdate = onUpdate)
    }

    fun rotation(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
        property(View.ROTATION, *values, onUpdate = onUpdate)
    }

//    fun pivotX(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
//        property("pivotX", *values, onUpdate = onUpdate)
//    }
//
//    fun pivotY(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
//        property("pivotY", *values, onUpdate = onUpdate)
//    }
//
//    fun pivot(vararg values: Float, onUpdate: ((value: Any) -> Unit)? = null) {
//        pivotX(*values, onUpdate = onUpdate)
//        pivotY(*values, onUpdate = onUpdate)
//    }

//    fun property(
//        propertyName: String,
//        vararg values: Float,
//        onUpdate: ((value: Any) -> Unit)? = null
//    ) {
//        targets.forEach {
//            val valueAnimator = ObjectAnimator.ofFloat(it, propertyName, *values)
//            valueAnimator.repeatCount = repeatCount
//            if (onUpdate != null) {
//                valueAnimator.addUpdateListener { animator ->
//                    onUpdate.invoke(animator.animatedValue)
//                }
//            }
//            animators.add(valueAnimator)
//        }
//    }

    fun property(
        property: Property<View, Float>,
        vararg values: Float,
        onUpdate: ((value: Any) -> Unit)? = null
    ) {
        targets.forEach { target ->
            val valueAnimator = if (useFrameAnimator) {
                ValueFrameAnimator.ofFloat(*values).apply {
                    addUpdateListener { animator ->
                        property.set(target, animator.animatedValue as Float)
                    }
                    name = "PropertyFrameAnimator"
                }
            } else {
                ObjectAnimator.ofFloat(target, property, *values)
            }
            valueAnimator.repeatCount = repeatCount
            if (onUpdate != null) {
                valueAnimator.addUpdateListener { animator ->
                    onUpdate.invoke(animator.animatedValue)
                }
            }
            animators.add(valueAnimator)
        }
    }

    fun pivot(x: Float, y: Float) {
        targets.forEach {
            it.pivotX = x
            it.pivotY = y
        }
    }

    fun height(vararg values: Float, onUpdate: ((value: Int) -> Unit)? = null) {
        floatValues(*values) { view, value ->
            view?.layoutParams?.height = value.toInt()
            view?.requestLayout()
            onUpdate?.invoke(value.toInt())
        }
    }

    fun width(vararg values: Float, onUpdate: ((value: Int) -> Unit)? = null) {
        floatValues(*values) { view, value ->
            view?.layoutParams?.width = value.toInt()
            view?.requestLayout()
            onUpdate?.invoke(value.toInt())
        }
    }

    fun floatValues(
        vararg values: Float, evaluator: TypeEvaluator<*>? = null, onUpdate: (
            target: View?, value:
            Float
        ) -> Unit
    ) {
        val valueAnimator = if (useFrameAnimator)
            ValueFrameAnimator.ofFloat(*values).apply {
                name = "FloatFrameAnimator"
            }
        else
            ValueAnimator.ofFloat(*values)
        if (targets.isEmpty()) {
            valueAnimator.addUpdateListener { anim ->
                onUpdate.invoke(null, anim.animatedValue as Float)
            }
            if (evaluator != null) valueAnimator.setEvaluator(evaluator)
            animators.add(valueAnimator)
        } else {
            targets.forEach { target ->
                valueAnimator.addUpdateListener { anim ->
                    onUpdate.invoke(target, anim.animatedValue as Float)
                }
                if (evaluator != null) valueAnimator.setEvaluator(evaluator)
                animators.add(valueAnimator)
            }
        }
    }

    fun intValues(
        vararg values: Int,
        evaluator: TypeEvaluator<*>? = null,
        onUpdate: (target: View?, value: Int) ->
        Unit
    ) {
        val valueAnimator = if (useFrameAnimator)
            ValueFrameAnimator.ofInt(*values).apply {
                name = "IntFrameAnimator"
            }
        else
            ValueAnimator.ofInt(*values)
        if (targets.isEmpty()) {
            valueAnimator.addUpdateListener { anim ->
                onUpdate.invoke(null, anim.animatedValue as Int)
            }
            if (evaluator != null) valueAnimator.setEvaluator(evaluator)
            animators.add(valueAnimator)
        } else {
            targets.forEach { target ->
                valueAnimator.addUpdateListener { anim ->
                    onUpdate.invoke(target, anim.animatedValue as Int)
                }
                if (evaluator != null) valueAnimator.setEvaluator(evaluator)
                animators.add(valueAnimator)
            }
        }
    }

    fun colors(vararg colors: Int, onUpdate: (target: View?, value: Int) -> Unit) {
        intValues(*colors, evaluator = ArgbEvaluator(), onUpdate = onUpdate)
    }

    fun path(path: Path) {
        val pathMeasure = PathMeasure(path, false)

        return floatValues(0f, pathMeasure.length) { target: View?, value: Float ->
            val currentPosition = FloatArray(2)
            pathMeasure.getPosTan(value, currentPosition, null)
            val x = currentPosition[0]
            val y = currentPosition[1]
            target?.x = x
            target?.y = y
        }
    }

    fun path(path: Path, onUpdate: (target: View?, valueX: Float, valueY: Float) -> Unit) {
        val pathMeasure = PathMeasure(path, false)

        return floatValues(0f, pathMeasure.length) { target: View?, value: Float ->
            val currentPosition = FloatArray(2)
            pathMeasure.getPosTan(value, currentPosition, null)
            val x = currentPosition[0]
            val y = currentPosition[1]
            onUpdate.invoke(target, x, y)
        }
    }
}

