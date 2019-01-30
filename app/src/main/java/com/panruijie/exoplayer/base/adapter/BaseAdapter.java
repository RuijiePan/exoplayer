package com.panruijie.exoplayer.base.adapter;

import android.animation.Animator;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.IntDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.panruijie.exoplayer.base.adapter.animation.*;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by panruijie on 16/12/6.
 */

public abstract class BaseAdapter<T, K extends BaseViewHolder> extends RecyclerView.Adapter<K> {

    private int mDuration = 300;
    private int mLastPosition = -1;
    protected int mLayoutResId;
    private boolean mFirstOnlyEnable = true;
    private boolean mOpenAnimationEnable = false;
    protected Resources mResources;
    protected Context mContext;
    protected List<T> mData;
    protected OnRecyclerViewItemClickListener mListener;
    private Interpolator mInterpolator = new LinearInterpolator();
    private BaseAnimation mCustomAnimation;
    private BaseAnimation mSelectAnimation = new AlphaInAnimation();

    public static final int ALPHAIN = 0x00000001;
    public static final int SCALEIN = 0x00000002;
    public static final int SLIDEIN_BOTTOM = 0x00000003;
    public static final int SLIDEIN_LEFT = 0x00000004;
    public static final int SLIDEIN_RIGHT = 0x00000005;

    /**
     * 这样写比Enum（枚举类）省一半内存以上，可以查看以下文章
     * https://noobcoderblog.wordpress.com/2015/04/12/java-enum-and-android-intdefstringdef-annotation/
     */
    @IntDef({ALPHAIN, SCALEIN, SLIDEIN_BOTTOM, SLIDEIN_LEFT, SLIDEIN_RIGHT})
    @Retention(RetentionPolicy.SOURCE)
    public @interface AnimationType {

    }

    public BaseAdapter(Context context, int layoutResId, List<T> data) {
        mContext = context;
        this.mData = data == null ? new ArrayList<T>() : data;
        if (layoutResId != 0) {
            this.mLayoutResId = layoutResId;
        }
    }

    public BaseAdapter(Context context, int layoutResId) {
        this(context, layoutResId, new ArrayList<T>());
    }

    public BaseAdapter(Context context, List<T> data) {
        this(context, 0, data);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public K onCreateViewHolder(ViewGroup parent, int viewType) {
        mResources = mContext.getResources();
        View item = LayoutInflater.from(mContext).inflate(mLayoutResId, parent, false);
        final K holder = (K) new BaseViewHolder(item);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    mListener.onItemClick(holder.getLayoutPosition());
                }
            }
        });

        return holder;
    }

    @Override
    public void onBindViewHolder(K holder, int position) {
        convert(holder, mData.get(position % mData.size()), position);
    }

    protected abstract void convert(BaseViewHolder holder, T item, int position);

    @Override
    public void onViewAttachedToWindow(K holder) {
        super.onViewAttachedToWindow(holder);
        addAnimation(holder);
    }

    private void addAnimation(RecyclerView.ViewHolder holder) {
        if (mOpenAnimationEnable) {
            if (!mFirstOnlyEnable || holder.getLayoutPosition() > mLastPosition) {
                BaseAnimation animation = null;
                if (mCustomAnimation != null) {
                    animation = mCustomAnimation;
                } else {
                    animation = mSelectAnimation;
                }

                for (Animator animator : animation.getAnimators(holder.itemView)) {
                    animator.setDuration(mDuration).start();
                    animator.setInterpolator(mInterpolator);
                }
                mLastPosition = holder.getLayoutPosition();
            }
        }
    }

    public void openLoadAnimation(@AnimationType int animationType) {
        this.mOpenAnimationEnable = true;
        mCustomAnimation = null;
        switch (animationType) {
            case ALPHAIN:
                mSelectAnimation = new AlphaInAnimation();
                break;
            case SCALEIN:
                mSelectAnimation = new ScaleInAnimation();
                break;
            case SLIDEIN_BOTTOM:
                mSelectAnimation = new SlideInBottomAnimation();
                break;
            case SLIDEIN_LEFT:
                mSelectAnimation = new SlideInLeftAnimation();
                break;
            case SLIDEIN_RIGHT:
                mSelectAnimation = new SlideInRightAnimation();
                break;
            default:
                break;
        }
    }

    /**
     * 自己实现BaseAnimation接口
     *
     * @param animation
     */
    public void openLoadAnimation(BaseAnimation animation) {
        this.mOpenAnimationEnable = true;
        this.mCustomAnimation = animation;
    }

    /**
     * true的话动画只加载一次。false的话动画一直都会加载
     *
     * @param firstOnly
     */
    public void isFirstOnly(boolean firstOnly) {
        this.mFirstOnlyEnable = firstOnly;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
    }

    public void setData(List<T> data) {
        this.mData = data;
        notifyDataSetChanged();
    }

    public void add(List<T> data) {
        this.mData.addAll(data);
        notifyDataSetChanged();
    }

    public void add(T data, int position) {
        this.mData.add(position, data);
        notifyItemInserted(position);
    }

    public void remove(int position) {
        this.mData.remove(position);
        notifyItemRemoved(position);
    }

    public void clear() {
        this.mData.clear();
        notifyDataSetChanged();
    }

    public T getData(int position) {
        return mData.get(position);
    }

    public interface OnRecyclerViewItemClickListener {
        void onItemClick(int position);
    }

    public void setItemClickListner(OnRecyclerViewItemClickListener listner) {
        this.mListener = listner;
    }
}
