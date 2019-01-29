package com.panruijie.exoplayer.base

import android.view.Surface
import com.google.android.exoplayer2.analytics.AnalyticsListener

/**
 * Created by panruijie on 2019/1/23.
 * 空实现
 **/
class IAbsPlayListener : IPlayListener {

    override fun onSeekComoleted(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {

    }

    override fun onBufferingUpdate(percent: Int?) {

    }

    override fun onBufferStart(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {

    }

    override fun onBufferEnd(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {

    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onPlayResume() {
        
    }

    override fun onPlayPause() {
        
    }

    override fun onPlayPosition(pos: Long) {
        
    }

    override fun onPlayNext(eventTime: AnalyticsListener.EventTime?) {
        
    }

    override fun onPlayCompleted(eventTime: AnalyticsListener.EventTime?) {
        
    }

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
        
    }

    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
        
    }

    override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime?, width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        
    }

    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {
        
    }

    override fun onLoadingSource(loadingStatue: IPlayListener.LoadingStatue) {
        
    }

    override fun onPlayError(eventTime: AnalyticsListener.EventTime?, reason: String) {
        
    }

    override fun onLoopingEnd(eventTime: AnalyticsListener.EventTime?) {
        
    }

    override fun onLoopingSingleEnd(eventTime: AnalyticsListener.EventTime?) {
        
    }

}