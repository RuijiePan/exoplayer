package com.panruijie.exoplayer.base

import com.google.android.exoplayer2.upstream.DataSource
import com.panruijie.exoplayer.source.MediaInfo

/**
 * Created by panruijie on 2019/1/22.
 * exoplayer的拓展接口
 **/
interface IExoPlayer : IPlayer {

    /**
     * 设置多个数据源
     */
    fun setMediaInfo(infoList : MutableList<MediaInfo>)

    /**
     * 获取当前时间
     */
    fun getCurrentPosition(limitToCurrentWindow: Boolean) : Long

    /**
     * 动态remove视频源
     */
    fun removeMediaInfo(index : Int)

    /**
     * 动态增加视频源
     */
    fun addMediaInfo(index: Int, mediaInfo: MediaInfo)

    /**
     * 单段视频循环
     */
    fun setLoopingSingle(window : Int, looping : Boolean)

    /**
     * 在当前window里面seekto
     */
    fun seekTo(positionMs : Long, limitToCurrentWindow : Boolean)

    /**
     * 调速
     */
    fun setSpeed(speed: Float)

    /**
     * 音调
     */
    fun setPitch(pitch : Float)

    /**
     * 切换视频源
     */
    fun setWindow(window : Int)

    /**
     * 第一帧刷出来的时候跑的任务
     */
    fun runOnRenderFirstFrame(runnable: Runnable)

    /**
     * 生命周期回调
     */
    fun onStart()

    fun onResume()

    fun onPause()

    fun onStop()

    /**
     * 设置回调
     */
    fun addPlayListener(listener: IPlayListener)

}