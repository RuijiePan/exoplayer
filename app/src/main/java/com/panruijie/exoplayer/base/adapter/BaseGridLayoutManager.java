package com.panruijie.exoplayer.base.adapter;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by panruijie on 2017/8/10.
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 * <p>
 * 捕获：RecyclerView and java.lang.IndexOutOfBoundsException:
 * Inconsistency detected. Invalid view holder adapter positionViewHolder
 */

public class BaseGridLayoutManager extends GridLayoutManager {

    public static final String TAG = "BasetGridLayoutManager";

    public BaseGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public BaseGridLayoutManager(Context context, int spanCount) {
        super(context, spanCount);
    }

    public BaseGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
        super(context, spanCount, orientation, reverseLayout);
    }

    @Override
    public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
        try {
            super.onLayoutChildren(recycler, state);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public boolean supportsPredictiveItemAnimations() {
        return false;
    }

}
