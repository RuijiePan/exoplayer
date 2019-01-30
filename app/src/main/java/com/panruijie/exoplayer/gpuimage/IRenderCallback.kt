package com.panruijie.exoplayer.gpuimage

import android.graphics.SurfaceTexture

/**
 * Created by panruijie on 2019/1/30.
 **/
interface IRenderCallback {

    /**
     * 纹理创建完毕
     */
    fun onSurfaceTextureCreated(surfaceTexture: SurfaceTexture?)

    /**
     * 图像帧刷新
     */
    fun onFrameAvailable(frameTimeNanos: Long)
}