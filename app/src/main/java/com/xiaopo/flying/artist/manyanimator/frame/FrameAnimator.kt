package com.xiaopo.flying.artist.manyanimator.frame

import android.os.Build
import androidx.annotation.MainThread
import androidx.annotation.RequiresApi
import android.view.Choreographer

/**
 * @author wupanjie
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
@MainThread
abstract class FrameAnimator @JvmOverloads constructor(
        private val name: String,
        private val totalFrames: Int,
        private val cycleDuration: Long,
        private val startDelay: Long,
        private val repeatable: Boolean,
        private val isForward: Boolean = true
) {

    private var ticker: Choreographer.FrameCallback? = null

    private var currentFrame: Int = 0
    private var startTime: Long = 0
    private var startDeltaTime: Long = 0
    private var initialStep: Boolean = false

    @Volatile
    var isDisposed: Boolean = false
        private set

    val isRunning: Boolean
        get() = ticker != null

    @Volatile
    var isPaused: Boolean = false
        private set

    init {
        reset()

        if (skipAnimation()) {
            animationDone()
        }
    }

    private fun onTick() {
        if (isDisposed) return

        if (initialStep) {
            initialStep = false
            startTime = System.currentTimeMillis() - startDeltaTime // keep animation state on suspend

            if (!isDisposed) {
                this.onAnimateStart()
            }

            paint()
            return
        }

        val cycleTime = (System.currentTimeMillis() - startTime).toDouble()
        if (cycleTime < 0) return  // currentTimeMillis() is not monotonic - let's pretend that animation didn't changed

        var newFrame = (cycleTime * totalFrames / cycleDuration).toLong()

        if (repeatable) {
            if (newFrame != currentFrame.toLong() && newFrame >= totalFrames) {
                onAnimateRepeat()
            }

            newFrame %= totalFrames.toLong()
        }

        if (newFrame == currentFrame.toLong()) return

        if (!repeatable && newFrame >= totalFrames) {
            animationDone()
            return
        }

        currentFrame = newFrame.toInt()

        paint()
    }

    private fun nextTick() {
        ticker?.let { Choreographer.getInstance().postFrameCallback(it) }
    }

    private fun paint() {
        onValueUpdated(
                if (isForward) currentFrame else totalFrames - currentFrame - 1,
                totalFrames,
                cycleDuration
        )
    }

    private fun animationDone() {
        stopTicker()

        if (!isDisposed) {
            this.onAnimateEnd()
        }
    }

    private fun stopTicker() {
        if (ticker != null) {
            ticker?.let { Choreographer.getInstance().removeFrameCallback(it) }
            ticker = null
        }
    }

    protected open fun onAnimateStart() {

    }

    protected open fun onAnimateEnd() {

    }

    protected open fun onAnimateRepeat() {

    }

    protected open fun onAnimateCancel() {

    }

    fun start() {
        startInternal()
    }

    fun pause() {
        if (!isRunning) {
            return
        }
        startDeltaTime = System.currentTimeMillis() - startTime
        initialStep = true
        isPaused = true
        stopTicker()
    }

    fun resume() {
        if (!isPaused) {
            return
        }

        isPaused = false

        startInternal()
    }

    private fun startInternal() {

        if (isDisposed) {
            stopTicker()
            return
        }
        if (skipAnimation()) {
            animationDone()
            return
        }

        if (cycleDuration == 0L) {
            currentFrame = totalFrames - 1
            paint()
            animationDone()
        } else if (ticker == null) {
            ticker = object : Choreographer.FrameCallback {
                override fun doFrame(frameTimeNanos: Long) {
                    onTick()
                    nextTick()
                }

                override fun toString(): String {
                    return "Scheduled " + this@FrameAnimator
                }
            }

            val delay = if (initialStep) startDelay else 0L
            Choreographer.getInstance().postFrameCallbackDelayed(ticker, delay)
        }
    }

    private fun skipAnimation(): Boolean {
        return false
    }

    abstract fun onValueUpdated(frame: Int, totalFrames: Int, cycle: Long)

    fun dispose() {
        stopTicker()
        isDisposed = true
    }

    fun reset() {
        currentFrame = 0
        startDeltaTime = 0
        initialStep = true
        isPaused = false
    }

    fun cancel() {
        val actualCancel = isRunning
        pause()
        reset()
        if (actualCancel) {
            onAnimateCancel()
            onAnimateEnd()
        }
    }

    override fun toString(): String {
        val future = ticker
        return "FrameAnimator '" + name + "' @" + System.identityHashCode(this) +
                if (future == null) " (stopped)" else " (running $currentFrame/$totalFrames frame)"
    }
}
