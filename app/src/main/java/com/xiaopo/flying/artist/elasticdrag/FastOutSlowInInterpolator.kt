package com.xiaopo.flying.artist.elasticdrag

import android.view.animation.PathInterpolator

/**
 * @author wupanjie
 */
object FastOutSlowInInterpolator : PathInterpolator(0.4f, 0f, 0.2f, 1f)