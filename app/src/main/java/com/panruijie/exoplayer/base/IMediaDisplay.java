package com.panruijie.exoplayer.base;

import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

/**
 * Created by panruijie on 18-1-2.
 * 对外接口.对应的播放器必须实现这些接口
 */

public interface IMediaDisplay {

    Surface getSurface();

    SurfaceView getSurfaceView();

    SurfaceHolder getHolder();

    SurfaceTexture getSurfaceTexture();

    TextureView getTextureView();
}
