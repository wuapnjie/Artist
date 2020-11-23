package com.xiaopo.flying.artist.base;

import android.content.Context;

/**
 * @author wupanjie
 */
public class UIUtils {

    private static final float FLOAT_BIAS = 0.5f;

    public static float dip2Px(Context context, float dipValue) {
        if (context != null) {
            final float scale = context.getResources().getDisplayMetrics().density;
            return dipValue * scale + FLOAT_BIAS;
        }
        return 0;
    }

}
