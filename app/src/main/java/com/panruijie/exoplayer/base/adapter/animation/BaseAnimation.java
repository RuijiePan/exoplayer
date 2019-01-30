package com.panruijie.exoplayer.base.adapter.animation;

import android.animation.Animator;
import android.view.View;

/**
 * Created by panruijie on 2017/7/12.
 * 策略模式：实现该方法实现自定义动画
 */

public interface BaseAnimation {

    Animator[] getAnimators(View view);
}
