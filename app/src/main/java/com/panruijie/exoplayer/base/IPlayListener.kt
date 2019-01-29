package com.panruijie.exoplayer.base

import android.support.annotation.Size
import android.view.Surface
import com.google.android.exoplayer2.analytics.AnalyticsListener

/**
 * Created by panruijie on 2019/1/22.
 **/
interface IPlayListener {

    fun onPlayResume()

    fun onPlayPause()

    fun onPlayPosition(pos : Long)

    fun onPlayNext(eventTime : AnalyticsListener.EventTime?)

    fun onPlayCompleted(eventTime : AnalyticsListener.EventTime?)

    fun onBufferStart(eventTime : AnalyticsListener.EventTime?, playWhenReady : Boolean)

    fun onBufferEnd(eventTime : AnalyticsListener.EventTime?, playWhenReady : Boolean)

    fun onBufferingUpdate(@Size(min = 0, max = 100) percent: Int?)

    fun onTimelineChanged(eventTime : AnalyticsListener.EventTime?)

    /**
     * exoplayer回调seekto开始
     */
    fun onSeekStarted(eventTime : AnalyticsListener.EventTime?)

    /**
     * exoplayer回调seekto结束，在刷新画面之前
     */
    fun onSeekProcessed(eventTime : AnalyticsListener.EventTime?)

    /**
     * exoplayer seekto完成，画面已经刷新的时候，
     */
    fun onSeekComoleted(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean)

    fun onVideoSizeChanged(eventTime : AnalyticsListener.EventTime?, width : Int, height : Int,
                           unappliedRotationDegrees : Int, pixelWidthHeightRatio : Float)

    fun onRenderedFirstFrame(eventTime : AnalyticsListener.EventTime?, surface : Surface?)

    fun onLoadingSource(loadingStatue: LoadingStatue)

    fun onPlayError(eventTime : AnalyticsListener.EventTime?, reason : String)

    fun onLoopingEnd(eventTime: AnalyticsListener.EventTime?)

    fun onLoopingSingleEnd(eventTime: AnalyticsListener.EventTime?)

    enum class LoadingStatue {
        START, FINISH, ERROR;

        /**
         * @return 返回对应状态
         */
        fun asString(): String {
            when (this) {
                START -> return "start"
                FINISH -> return "finish"
                ERROR -> return "error"
                else -> throw IllegalStateException("Unknown statue!")
            }
        }
    }
}