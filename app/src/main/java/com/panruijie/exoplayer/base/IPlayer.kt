package com.panruijie.exoplayer.base

/**
 * Created by panruijie on 2019/1/22.
 * 播放器基础功能接口（Mediaplayer基础功能接口）
 **/
interface IPlayer {

    /**
     * 初始化播放器
     */
    fun initPlayer()

    /**
     * 回收播放器资源
     */
    fun releasePlayer()

    /**
     * 设置信息源
     */
    fun setMediaInfo(path : String)

    /**
     * 设置承载容器。surfaceView / GLSurfaceView / Surface / TextureView
     */
    fun setDisPlay(display : IMediaDisplay)

    /**
     * 开始播放
     */
    fun startPlay()

    /**
     * 暂停播放
     */
    fun pausePlay()

    /**
     * 重置播放器进度到0
     */
    fun resetPlay()

    /**
     * seekto
     */
    fun seekTo(position : Long)

    /**
     * 获取当前播放进度
     */
    fun getCurrentPosition() : Long

    /**
     * 是否在播放
     */
    fun isPlaying() : Boolean

    /**
     * 设置循环
     */
    fun setLooping(looping : Boolean)

    /**
     * 音量设置
     */
    fun setVolume(volume : Float)

}