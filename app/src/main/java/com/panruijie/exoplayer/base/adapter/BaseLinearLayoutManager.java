package com.panruijie.exoplayer.base.adapter;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by panruijie on 2017/8/10.
 * https://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
 * <p>
 * 捕获：RecyclerView and java.lang.IndexOutOfBoundsException:
 * Inconsistency detected. Invalid view holder adapter positionViewHolder
 */

public class BaseLinearLayoutManager extends LinearLayoutManager {

    public static final String TAG = "BaseLinearLayoutManager";

    public BaseLinearLayoutManager(Context context) {
        super(context);
    }

    public BaseLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    public BaseLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
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
