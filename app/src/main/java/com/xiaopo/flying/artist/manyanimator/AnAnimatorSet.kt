package com.xiaopo.flying.artist.manyanimator

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.TimeInterpolator
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * @author wupanjie
 */
internal abstract class AnAnimatorSet : Animator() {

    internal companion object {

        fun wrap(animatorSet: AnimatorSet): AnAnimatorSet {
            return object : AnAnimatorSet() {
                override fun playTogether(animator: Collection<Animator>) {
                    animatorSet.playTogether(animator)
                }

                override fun playSequentially(animator: List<Animator>) {
                    animatorSet.playSequentially(animator)
                }

                override fun getStartDelay(): Long {
                    return animatorSet.startDelay
                }

                override fun setStartDelay(startDelay: Long) {
                    animatorSet.startDelay = startDelay
                }

                override fun setDuration(duration: Long): Animator {
                    animatorSet.duration = duration
                    return this
                }

                override fun getDuration(): Long {
                    return animatorSet.duration
                }

                override fun setInterpolator(value: TimeInterpolator?) {
                    animatorSet.interpolator = value
                }

                override fun isRunning(): Boolean {
                    return animatorSet.isRunning
                }

                override fun start() {
                    animatorSet.start()
                }

                override fun cancel() {
                    animatorSet.cancel()
                }

                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun resume() {
                    animatorSet.resume()
                }

                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun pause() {
                    animatorSet.pause()
                }

                @RequiresApi(Build.VERSION_CODES.KITKAT)
                override fun isPaused(): Boolean {
                    return animatorSet.isPaused
                }

            }
        }

    }

    abstract fun playTogether(animator: Collection<Animator>)

    abstract fun playSequentially(animator: List<Animator>)

}