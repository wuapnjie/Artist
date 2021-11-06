package com.xiaopo.flying.artist.manyanimator.frame

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.TimeInterpolator
import android.os.Build
import androidx.annotation.RequiresApi
import com.xiaopo.flying.artist.manyanimator.AnAnimatorSet

/**
 * @author wupanjie
 */
internal class FrameAnimatorSet : AnAnimatorSet() {

    private enum class PlayMode {
        Together, Sequentially
    }

    private var duration: Long = -1L
    private var timeInterpolator: TimeInterpolator? = null

    private var playMode = PlayMode.Sequentially
    private val animators = arrayListOf<Animator>()

    override fun playTogether(animator: Collection<Animator>) {
        playMode = PlayMode.Together
        animators.clear()
        animators.addAll(animator)
    }

    override fun playSequentially(animator: List<Animator>) {
        playMode = PlayMode.Sequentially
        animators.clear()
        animators.addAll(animator)
    }

    override fun start() {
        if (duration > 0L) {
            animators.forEach { it.setDuration(duration) }
        }

        timeInterpolator?.let { interpolator ->
            animators.forEach { it.setInterpolator(interpolator) }
        }

        listeners?.forEach { it.onAnimationStart(this) }

        when (playMode) {
            PlayMode.Together -> {
                animators.maxByOrNull {
                    if (it is FrameAnimatorSet) {
                        it.totalDuration()
                    } else {
                        it.duration
                    }
                }?.addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator?) {
                        listeners?.forEach { it.onAnimationEnd(this@FrameAnimatorSet) }
                    }

                })

                animators.forEach { it.start() }
            }
            PlayMode.Sequentially -> {
                val size = animators.size

                if (size >= 2) {
                    for (i in 0 until size - 1) {
                        animators[i].addListener(object : AnimatorListenerAdapter() {
                            override fun onAnimationEnd(animation: Animator?) {
                                animators[i + 1].start()
                            }
                        })
                    }
                }

                animators.lastOrNull()?.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        listeners?.forEach { it.onAnimationEnd(this@FrameAnimatorSet) }
                    }
                })

                animators.firstOrNull()?.start()
            }
        }
    }

    override fun cancel() {
        animators.forEach { it.cancel() }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun resume() {
        when (playMode) {
            PlayMode.Together -> {
                animators.forEach { it.resume() }
            }
            PlayMode.Sequentially -> {
                animators.firstOrNull { it.isPaused }?.resume()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun pause() {
        when (playMode) {
            PlayMode.Together -> {
                animators.forEach { it.pause() }
            }
            PlayMode.Sequentially -> {
                animators.firstOrNull { it.isRunning }?.pause()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    override fun isPaused(): Boolean {
        return animators.any { it.isPaused }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun getTotalDuration(): Long {
        return totalDuration()
    }

    private fun totalDuration(): Long {
        return when (playMode) {
            PlayMode.Together -> animators.maxOf {
                if (it is FrameAnimatorSet) {
                    it.totalDuration()
                } else {
                    it.duration
                }
            }
            PlayMode.Sequentially -> animators.sumOf {
                if (it is FrameAnimatorSet) {
                    it.totalDuration()
                } else {
                    it.duration
                }
            }
        }
    }

    override fun getStartDelay(): Long {
        return 0L
    }

    override fun setStartDelay(startDelay: Long) {
        // no-ops
    }

    override fun setDuration(duration: Long): Animator {
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
        return false
    }

}