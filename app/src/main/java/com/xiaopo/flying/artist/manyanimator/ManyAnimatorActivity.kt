package com.xiaopo.flying.artist.manyanimator

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.xiaopo.flying.artist.R
import com.xiaopo.flying.artist.base.dp
import com.xiaopo.flying.artist.base.log

class ManyAnimatorActivity : AppCompatActivity() {

    private lateinit var ivAnim: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_many_animator)

        ivAnim = findViewById(R.id.iv_anim)

        ivAnim.setOnClickListener {
            startAnim()
        }

    }

    private fun startAnim() {
        val frameAnimator = ValueFrameAnimator.ofFloat(0.dp, 100.dp)
            .setDuration(2000L)

        frameAnimator.startDelay = 1000L

        frameAnimator.addUpdateListener {
            val fraction = it.animatedFraction
            val value = it.animatedValue as Float

            ivAnim.apply {
                translationX = value
                translationY = value
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
