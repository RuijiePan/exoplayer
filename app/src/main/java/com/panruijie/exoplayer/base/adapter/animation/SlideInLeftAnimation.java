package com.panruijie.exoplayer.base.adapter.animation;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.view.View;


/**
 * Created by panruijie on 2017/7/12.
 * Email : zquprj@gmail.com
 */
public class SlideInLeftAnimation implements BaseAnimation {

    @Override
    public Animator[] getAnimators(View view) {
        return new Animator[]{
                ObjectAnimator.ofFloat(view, "translationX", -view.getRootView().getWidth(), 0)
        };
    }
}
