package com.panruijie.exoplayer.source;

import android.graphics.SurfaceTexture;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.panruijie.exoplayer.base.IMediaDisplay;


/**
 * Created by panruijie on 18-1-2.
 * 用于MediaPlayer绑定的是GLSurfaceView还是surface的显示窗口
 */
public class GLDisPlay implements IMediaDisplay {

    private Surface mSurface;
    private SurfaceView mSurfaceView;
    private SurfaceTexture mSurfaceTexture;
    private TextureView mTextureView;

    public GLDisPlay(SurfaceTexture surfaceTexture) {
        this.mSurfaceTexture = surfaceTexture;
        this.mSurface = new Surface(surfaceTexture);
    }

    public GLDisPlay(GLSurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    public GLDisPlay(TextureView textureView) {
        this.mTextureView = textureView;
    }

    public GLDisPlay(SurfaceView surfaceView) {
        this.mSurfaceView = surfaceView;
    }

    @Override
    public Surface getSurface() {
        return mSurface;
    }

    @Override
    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    @Override
    public SurfaceHolder getHolder() {
        if (mSurfaceView != null) {
            return mSurfaceView.getHolder();
        }
        return null;
    }

    @Override
    public SurfaceTexture getSurfaceTexture() {
        return mSurfaceTexture;
    }

    @Override
    public TextureView getTextureView() {
        return mTextureView;
    }
}
