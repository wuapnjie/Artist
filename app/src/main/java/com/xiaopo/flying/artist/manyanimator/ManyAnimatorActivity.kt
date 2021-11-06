package com.xiaopo.flying.artist.manyanimator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.dpInt
import com.xiaopo.flying.artist.base.log
import com.xiaopo.flying.artist.manyanimator.frame.ValueFrameAnimator
import kotlinx.android.synthetic.main.activity_many_animator.*

class ManyAnimatorActivity : AppCompatActivity() {

    private lateinit var ivColor: View
    private lateinit var ivAnim: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_many_animator)

        ivColor = iv_color
        ivAnim = iv_anim

        ivAnim.setOnClickListener {
            startAnim()
        }

        ivColor.setOnClickListener {
            startColorAnim()
        }
    }

    private fun startColorAnim() {
        quickAnimate(true) {
            sequence {
                play {
                    targets = listOf(ivColor)
                    duration = 3000L
                    interpolator = AccelerateDecelerateInterpolator()
                    colors(Color.RED, Color.GREEN, Color.BLUE, Color.RED) { view, value ->
                        view?.setBackgroundColor(value)
                        "on color update (r, g, b): " +
                            "(${Color.red(value)}, ${Color.green(value)}, ${Color.blue(value)})".log()
                    }
                }

                play {
                    targets = listOf(ivColor)
                    duration = 500L
                    alpha(1f, 0f, 1f)
                }
            }
        }.start()
    }

    private fun startAnim() {
        val frameAnimator = ValueFrameAnimator.ofInt(0.dpInt, 100.dpInt)
            .setDuration(2000L)

        frameAnimator.startDelay = 1000L

        frameAnimator.addUpdateListener {
            val fraction = it.animatedFraction
            val value = it.animatedValue as Int

            ivAnim.apply {
                translationX = value.toFloat()
                translationY = value.toFloat()
                alpha = fraction
            }
            "on animate update fraction: $fraction, value: $value".log()
        }

        frameAnimator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationStart(animation: Animator?) {
                ivAnim.alpha = 0f

                "on animate start".log()
            }

            override fun onAnimationEnd(animation: Animator?) {
                ivAnim.alpha = 1f

                "on animate end".log()
            }

            override fun onAnimationCancel(animation: Animator?) {
                "on animate cancel".log()
            }

            override fun onAnimationRepeat(animation: Animator?) {
                "on animate repeat".log()
            }
        })

        frameAnimator.start()
    }
}
