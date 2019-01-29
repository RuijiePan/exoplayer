package com.panruijie.exoplayer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.PowerManager
import android.os.PowerManager.SCREEN_BRIGHT_WAKE_LOCK
import android.util.Log
import android.view.Surface
import com.panruijie.exoplayer.base.IExoPlayer
import com.panruijie.exoplayer.base.IMediaDisplay
import com.panruijie.exoplayer.base.IPlayListener
import com.panruijie.exoplayer.source.MediaInfo
import com.panruijie.exoplayer.util.Repeater
import com.panruijie.exoplayer.util.UriParseUtil
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.Player.*
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.source.*
import com.google.android.exoplayer2.source.dash.DashMediaSource
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.util.Util
import com.panruijie.exoplayer.cache.Cache
import com.panruijie.exoplayer.cache.CacheBuilder
import com.panruijie.exoplayer.cache.DataSourceFactoryProvider
import java.util.ArrayList
import java.util.concurrent.CopyOnWriteArrayList
import com.panruijie.exoplayer.util.StateStore
import com.panruijie.exoplayer.util.TextureViewHelper
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue


/**
 * Created by panruijie on 2019/1/22.
 * 用于视频本地视频播放和网络播放器的播放器
 **/
class GoExoPlayer(private val context: Context) : IExoPlayer, AnalyticsListener {

    private val mediaInfoList: MutableList<MediaInfo> = CopyOnWriteArrayList()
    private val playListenerList = CopyOnWriteArrayList<IPlayListener>()
    private val runOnRenderFirstFrame = LinkedBlockingQueue<Runnable>()
    private val bufferRepeater = Repeater()
    private val playRepeater = Repeater()
    /**
     * 播放循环状态：顺序播放
     */
    private var repeatMode = REPEAT_MODE_OFF
    /**
     * 是否正在buffering
     */
    val isBuffering: Boolean
        get() = exoPlayer?.getPlaybackState() == Player.STATE_BUFFERING
    /**
     * 是否正在加载资源，处于第一帧画面回调之前
     */
    private var isLoadingSource = true
    /**
     * 自动播放
     */
    private var playWhenReady = true
    /**
     * 是否已经回调第一帧
     */
    private var renderFirstFrame = false
    /**
     * 需要下次seekto的位置
     */
    private var pendSeekPos = IDLE_SEEK_POS

    private var initPlayer = false
    private var volume: Float = 1F
    private var speed: Float = 1F
    private var pitch: Float = 1F

    /**
     * 是否等待刷新了画面帧之后再刷新下一帧
     */
    var isSeekableAfterFrameRenderer = true
    var buildClipSource = true
    private var exoPlayer: SimpleExoPlayer? = null
    private var playbackParameters: PlaybackParameters = PlaybackParameters.DEFAULT
    private lateinit var display: IMediaDisplay
    /**
     * 可以动态增加，删除的mediasource
     */
    private val concatenatedSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
    private val stateStore: StateStore = StateStore()
    private val wakeLock: PowerManager.WakeLock?

    companion object {
        val TAG = "GoExoPlayer"

        val WAKE_LOCK_TIMEOUT = 1000L
        val BUFFER_REPEAT_DELAY = 1000

        val PLAY_REPEATER_TIME = 1000
        val IDLE_SEEK_POS = -1L
        val ANDROID_M = 23
    }

    init {
        //Acquires the wakelock if we have permissions to
        if (context.packageManager.checkPermission(Manifest.permission.WAKE_LOCK, context.packageName) == PackageManager.PERMISSION_GRANTED) {
            val pm = context.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = pm.newWakeLock(SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ON_AFTER_RELEASE, GoExoPlayer::class.java.getName())
            wakeLock.setReferenceCounted(false)
        } else {
            wakeLock = null
            Log.w(TAG, "Unable to acquire WAKE_LOCK due to missing manifest permission")
        }
        stayAwake(false)

        bufferRepeater.repeaterDelay = BUFFER_REPEAT_DELAY
        bufferRepeater.setRepeatListener(object : Repeater.RepeatListener {
            override fun onRepeat() {
                Log.w(TAG, "buffer repeat")
                playListenerList.forEach {
                    it.onBufferingUpdate(exoPlayer?.bufferedPercentage)
                }
            }
        })

        playRepeater.repeaterDelay = PLAY_REPEATER_TIME
        playRepeater.setRepeatListener {
            Log.w(TAG, "play repeat")
            val pos = getCurrentPosition()
            playListenerList.forEach {
                it.onPlayPosition(pos)
            }
        }

        DataSourceFactoryProvider.setProviderType(DataSourceFactoryProvider.ProviderType.OKHTTP)
    }

    /**
     * Used with playback state changes to correctly acquire and
     * release the wakelock if the user has enabled it with [.setWakeMode].
     * If the [.wakeLock] is null then no action will be performed.
     *
     * @param awake True if the wakelock should be acquired
     */
    private fun stayAwake(awake: Boolean) {
        if (wakeLock == null) {
            return
        }

        if (awake && !wakeLock.isHeld) {
            wakeLock.acquire(WAKE_LOCK_TIMEOUT.toLong())
        } else if (!awake && wakeLock.isHeld) {
            wakeLock.release()
        }
    }

    /**
     * 普通的mediasource
     */
    private fun buildMediaSource(list: MutableList<MediaInfo>): MutableCollection<MediaSource>? {
        val sourceList = ArrayList<MediaSource>()
        list.forEach {
            sourceList.add(buildMediaSource(info = it))
        }
        return sourceList
    }

    /**
     * 裁剪过后的mediasource
     */
    private fun buildClipMediaSource(list: MutableList<MediaInfo>): MutableCollection<MediaSource>? {
        val clipSourceList = ArrayList<MediaSource>()
        list.forEach {
            clipSourceList.add(buildClipMediaSource(info = it))
        }
        return clipSourceList
    }

    private fun buildClipMediaSource(info: MediaInfo): MediaSource {
        if (info.contentType == C.TYPE_OTHER && info.isLocalFile) {
            return ClippingMediaSource(buildMediaSource(info), info.clipStart, info.clipEnd)
        } else {
            return buildMediaSource(info)
        }
    }

    private fun buildMediaSource(info: MediaInfo): MediaSource {
        val dataSourceFactory = DataSourceFactoryProvider.provider.create(context, Util.getUserAgent(context, context.getPackageName()), null)
        val uri: Uri?
        if (info.rawId != null) {
            uri = UriParseUtil.rawResourceIdToUri(context, info.rawId)!!
            return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
        } else {
            uri = info.uri
            when (info.contentType) {
                C.TYPE_SS -> return SsMediaSource.Factory(
                        DefaultSsChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                        .createMediaSource(uri)
                C.TYPE_DASH -> return DashMediaSource.Factory(
                        DefaultDashChunkSource.Factory(dataSourceFactory), dataSourceFactory)
                        .createMediaSource(uri)
                C.TYPE_HLS -> return HlsMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                C.TYPE_OTHER -> return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
                else -> return ExtractorMediaSource.Factory(dataSourceFactory).createMediaSource(uri)
            }
        }
    }

    @Synchronized
    override fun initPlayer() {
        if (initPlayer) {
            return
        }
        initPlayer = true
        val adaptiveTrackSelectionFactory = AdaptiveTrackSelection.Factory()
        val trackSelector = DefaultTrackSelector(adaptiveTrackSelectionFactory)
        val renderersFactory = DefaultRenderersFactory(context)
        exoPlayer = ExoPlayerFactory.newSimpleInstance(context, renderersFactory, trackSelector)
        exoPlayer?.addAnalyticsListener(this)
        if (display.getSurface() != null) {
            exoPlayer?.setVideoSurface(display.getSurface())
        } else if (display.getTextureView() != null) {
            exoPlayer?.setVideoTextureView(display.getTextureView())
        } else if (display.getSurfaceView() != null) {
            exoPlayer?.setVideoSurfaceView(display.getSurfaceView())
        }
        renderFirstFrame = false
        isLoadingSource = true
        playListenerList.forEach {
            it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.START)
        }
        exoPlayer?.volume = volume
        exoPlayer?.repeatMode = repeatMode
        playbackParameters = PlaybackParameters(speed, pitch)
        exoPlayer?.playbackParameters = playbackParameters
        exoPlayer?.playWhenReady = playWhenReady

        concatenatedSource.clear()
        if (buildClipSource) {
            concatenatedSource.addMediaSources(buildClipMediaSource(mediaInfoList))
        } else {
            concatenatedSource.addMediaSources(buildMediaSource(mediaInfoList))
        }

        exoPlayer?.prepare(concatenatedSource)
    }

    @Synchronized
    override fun releasePlayer() {
        if (!initPlayer) {
            return
        }
        initPlayer = false
        playRepeater.stop()
        bufferRepeater.stop()
        exoPlayer?.playWhenReady = false
        stateStore.resetState()
        renderFirstFrame = false
        concatenatedSource.releaseSourceInternal()
        concatenatedSource.clear()
        pendSeekPos = getCurrentPosition()
        synchronized(runOnRenderFirstFrame) {
            runOnRenderFirstFrame.clear()
        }
        exoPlayer?.removeAnalyticsListener(this)
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun setMediaInfo(path: String) {
        mediaInfoList.clear()
        mediaInfoList.add(MediaInfo(context, path))
    }

    override fun setMediaInfo(infoList: MutableList<MediaInfo>) {
        mediaInfoList.clear()
        mediaInfoList.addAll(infoList)
    }

    override fun removeMediaInfo(index: Int) {
        mediaInfoList.removeAt(index)
        isLoadingSource = true
        playListenerList.forEach {
            it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.START)
        }
        if (exoPlayer != null) {
            concatenatedSource.removeMediaSource(index) {
                isLoadingSource = false
                playListenerList.forEach {
                    it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.FINISH)
                }
            }
        }

    }

    override fun addMediaInfo(index: Int, mediaInfo: MediaInfo) {
        mediaInfoList.add(index, mediaInfo)
        isLoadingSource = true
        playListenerList.forEach {
            it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.START)
        }
        if (exoPlayer != null) {
            concatenatedSource.addMediaSource(index, buildMediaSource(info = mediaInfo), {
                isLoadingSource = false
                playListenerList.forEach {
                    it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.FINISH)
                }
            })
        }
    }

    override fun runOnRenderFirstFrame(runnable: Runnable) {
        synchronized(runOnRenderFirstFrame) {
            runOnRenderFirstFrame.add(runnable)
        }
    }

    override fun setWindow(window: Int) {
        exoPlayer?.seekTo(window, 0)
    }

    override fun setSpeed(speed: Float) {
        this.speed = speed
        PlaybackParameters(speed, pitch).also {
            exoPlayer?.playbackParameters = it
        }
    }

    override fun setPitch(pitch: Float) {
        this.pitch = pitch
        PlaybackParameters(speed, pitch).also {
            exoPlayer?.playbackParameters = it
        }
    }

    override fun seekTo(position: Long) {
        Log.w(TAG, "seek to = ${position}")
        seekTo(position, false)
    }

    /**
     * TODO: Expose this
     * Seeks to the specified position in the media currently loaded specified by `positionMs`.
     * If `limitToCurrentWindow` is true then a seek won't be allowed to span across windows.
     * This should only be different if the media in playback has multiple windows (e.g. in the case of using a
     * `ConcatenatingMediaSource` with more than 1 source)
     *
     * @param positionMs           The position to seek to in the media
     * @param limitToCurrentWindow `true` to only seek in the current window
     */
    override fun seekTo(positionMs: Long, limitToCurrentWindow: Boolean) {
        //无效的seekto
        if (positionMs == IDLE_SEEK_POS) {
            return
        }
        //等待刷新一帧之后再seekto
        if (isSeekableAfterFrameRenderer) {
            if (stateStore.getMostRecentState() == StateStore.STATE_SEEKING) {
                pendSeekPos = positionMs
                return
            }
        }
        //当前位置就是想要seekto的位置，不会有回调，手动回调
        if (positionMs == getCurrentPosition(limitToCurrentWindow)) {
            pendSeekPos = IDLE_SEEK_POS
            stateStore.setMostRecentState(stateStore.isLastReportedPlayWhenReady(), StateStore.FLAG_PLAY_WHEN_READY)
            playListenerList.forEach {
                it.onSeekComoleted(null, exoPlayer?.playWhenReady?: playWhenReady)
            }
            return
        }
        //在当前的window的seekto
        if (limitToCurrentWindow) {
            exoPlayer?.seekTo(positionMs)
            stateStore.setMostRecentState(stateStore.isLastReportedPlayWhenReady(), StateStore.STATE_SEEKING)
            return
        }

        // We seek to the position in the timeline (may be across windows)
        val timeline = exoPlayer?.getCurrentTimeline()
        val windowCount = timeline?.getWindowCount() ?: concatenatedSource.size

        var cumulativePositionMs: Long = 0
        val window = Timeline.Window()

        for (index in 0 until windowCount) {
            timeline!!.getWindow(index, window)

            val windowDurationMs = window.durationMs
            if (cumulativePositionMs < positionMs && positionMs <= cumulativePositionMs + windowDurationMs) {
                exoPlayer?.seekTo(index, positionMs - cumulativePositionMs)
                stateStore.setMostRecentState(stateStore.isLastReportedPlayWhenReady(), StateStore.STATE_SEEKING)
                pendSeekPos = IDLE_SEEK_POS
                return
            }

            cumulativePositionMs += windowDurationMs
        }

        //seekto失败，默认seekto
        /*Log.e(TAG, "Unable to seek across windows, falling back to in-window seeking")
        exoPlayer?.seekTo(if (positionMs <= cumulativePositionMs) positionMs else 0)
        stateStore.setMostRecentState(stateStore.isLastReportedPlayWhenReady(), StateStore.STATE_SEEKING)*/
    }

    override fun addPlayListener(listener: IPlayListener) {
        playListenerList.add(listener)
    }

    override fun onStart() {
        if (Util.SDK_INT > ANDROID_M) {
            initPlayer()
        }
    }

    override fun onResume() {
        if (Util.SDK_INT <= ANDROID_M) {
            initPlayer()
        }
    }

    override fun onPause() {
        if (Util.SDK_INT <= ANDROID_M) {
            releasePlayer()
        }
    }

    override fun onStop() {
        if (Util.SDK_INT > ANDROID_M) {
            releasePlayer()
        }
    }

    override fun setDisPlay(display: IMediaDisplay) {
        this.display = display
    }

    override fun startPlay() {
        playWhenReady = true
        exoPlayer?.playWhenReady = playWhenReady
    }

    override fun pausePlay() {
        playWhenReady = false
        exoPlayer?.playWhenReady = playWhenReady
    }

    override fun resetPlay() {
        exoPlayer?.seekTo(0)
        startPlay()
    }

    override fun getCurrentPosition(): Long {
        return getCurrentPosition(false)
    }

    override fun getCurrentPosition(limitToCurrentWindow: Boolean): Long {
        val positionInCurrentWindow = exoPlayer?.getCurrentPosition() ?: 0
        if (limitToCurrentWindow) {
            return positionInCurrentWindow
        }

        // TODO cache the total time at the start of each window (e.g. Map<WindowIndex, cumulativeStartTimeMs>)
        // Adds the preceding window durations
        val timeline = exoPlayer?.getCurrentTimeline()
        val maxWindowIndex = Math.min(timeline?.getWindowCount() ?: concatenatedSource.size
        - 1, exoPlayer?.getCurrentWindowIndex() ?: 0)

        var cumulativePositionMs: Long = 0
        val window = Timeline.Window()

        for (index in 0 until maxWindowIndex) {
            timeline?.getWindow(index, window)
            cumulativePositionMs += window.durationMs
        }

        return cumulativePositionMs + positionInCurrentWindow
    }

    override fun isPlaying(): Boolean {
        if (exoPlayer == null)
            return false
        val state = exoPlayer?.getPlaybackState()
        when (state) {
            Player.STATE_BUFFERING, Player.STATE_READY -> return exoPlayer?.getPlayWhenReady()
                    ?: false
            Player.STATE_IDLE, Player.STATE_ENDED -> return false
            else -> return false
        }
    }

    override fun setLooping(looping: Boolean) {
        if (looping) {
            repeatMode = REPEAT_MODE_ALL
        } else {
            repeatMode = REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = repeatMode
    }

    override fun setLoopingSingle(window: Int, looping: Boolean) {
        if (looping) {
            repeatMode = REPEAT_MODE_ONE
        } else {
            repeatMode = REPEAT_MODE_OFF
        }
        exoPlayer?.repeatMode = repeatMode
    }

    override fun setVolume(volume: Float) {
        this.volume = volume
        exoPlayer?.volume = volume
    }

    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {
        super.onRenderedFirstFrame(eventTime, surface)
        if (isLoadingSource) {
            isLoadingSource = false
            playListenerList.forEach {
                it.onLoadingSource(loadingStatue = IPlayListener.LoadingStatue.FINISH)
            }
        }
        if (!renderFirstFrame) {
            renderFirstFrame = true
            playListenerList.forEach {
                it.onRenderedFirstFrame(eventTime, surface)
            }
            synchronized(runOnRenderFirstFrame) {
                while (!runOnRenderFirstFrame.isEmpty()) {
                    val runnable = runOnRenderFirstFrame.poll()
                    if (runnable != null) {
                        runnable.run()
                    }
                }
            }
        }
    }

    override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime?, width: Int, height: Int, unappliedRotationDegrees: Int, pixelWidthHeightRatio: Float) {
        super.onVideoSizeChanged(eventTime, width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
        display.textureView.also {
            TextureViewHelper.applyTextureViewRotation(it, unappliedRotationDegrees)
        }

        playListenerList.forEach {
            it.onVideoSizeChanged(eventTime, width, height, unappliedRotationDegrees, pixelWidthHeightRatio)
        }
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime?, error: ExoPlaybackException?) {
        super.onPlayerError(eventTime, error)
        if (isLoadingSource) {
            isLoadingSource = false
            playListenerList.forEach {
                it.onLoadingSource(IPlayListener.LoadingStatue.ERROR)
            }
        }
        val reason = StringBuilder()
        val type = error?.type
        when (type) {
            ExoPlaybackException.TYPE_SOURCE -> {
            }
            ExoPlaybackException.TYPE_RENDERER -> {
            }
            ExoPlaybackException.TYPE_UNEXPECTED -> {
            }
        }
        reason.append(if (error?.message == null) "" else error.message)
        playListenerList.forEach {
            it.onPlayError(eventTime, reason.toString())
        }
        error?.printStackTrace()
    }

    override fun onPlayerStateChanged(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean, playbackState: Int) {
        super.onPlayerStateChanged(eventTime, playWhenReady, playbackState)

        //play state changed
        if (playWhenReady && playbackState == Player.STATE_READY) {
            if (!playRepeater.isRunning) {
                playRepeater.start()
                playListenerList.forEach {
                    it.onPlayResume()
                }
            }
        } else {
            if (playRepeater.isRunning) {
                playRepeater.stop()
                playListenerList.forEach {
                    it.onPlayPause()
                }
            }
        }

        //buffer state changed
        when (playbackState) {
            Player.STATE_IDLE, Player.STATE_READY -> {
                notifityBuffering(eventTime, false, playWhenReady)
            }
            Player.STATE_BUFFERING -> {
                notifityBuffering(eventTime, true, playWhenReady)
            }
            Player.STATE_ENDED -> {
                notifityBuffering(eventTime, false, playWhenReady)
                playListenerList.forEach {
                    it.onPlayCompleted(eventTime)
                }
            }
        }

        //seek state changed
        Log.w(TAG, "playwhenready = " + playWhenReady + ", playstate = " + playbackState)
        val newState = stateStore.getState(playWhenReady, playbackState)
        if (newState != stateStore.getMostRecentState()) {
            stateStore.setMostRecentState(playWhenReady, playbackState)

            //Makes sure the buffering notifications are sent
            if (newState == Player.STATE_READY) {
                bufferRepeater.start()
                playRepeater.start()
                playListenerList.forEach {
                    it.onPlayResume()
                }
            } else if (newState == Player.STATE_IDLE || newState == Player.STATE_ENDED) {
                bufferRepeater.stop()
            }
            //Because the playWhenReady isn't a state in itself, rather a flag to a state we will ignore informing of
            // see events when that is the only change.  Additionally, on some devices we get states ordered as
            // [seeking, ready, buffering, ready] while on others we get [seeking, buffering, ready]
            var informSeekCompletion = stateStore.matchesHistory(intArrayOf(StateStore.STATE_SEEKING, Player.STATE_BUFFERING, Player.STATE_READY), true)
            informSeekCompletion = informSeekCompletion or stateStore.matchesHistory(intArrayOf(Player.STATE_BUFFERING, StateStore.STATE_SEEKING, Player.STATE_READY), true)
            informSeekCompletion = informSeekCompletion or stateStore.matchesHistory(intArrayOf(StateStore.STATE_SEEKING, Player.STATE_READY, Player.STATE_BUFFERING, Player.STATE_READY), true)

            if (informSeekCompletion) {
                playListenerList.forEach {
                    it.onSeekComoleted(eventTime, playWhenReady)
                }
                //继续下次seekto
                if (pendSeekPos != IDLE_SEEK_POS) {
                    seekTo(pendSeekPos)
                }
            }
        }
    }

    private fun notifityBuffering(eventTime: AnalyticsListener.EventTime?, buffering: Boolean, playWhenReady: Boolean) {
        if (buffering) {
            playListenerList.forEach {
                it.onBufferStart(eventTime, playWhenReady)
            }
        } else {
            playListenerList.forEach {
                it.onBufferEnd(eventTime, playWhenReady)
            }
        }
    }

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
        super.onSeekStarted(eventTime)
        playListenerList.forEach {
            it.onSeekStarted(eventTime)
        }
    }

    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {
        super.onSeekProcessed(eventTime)
        playListenerList.forEach {
            it.onSeekProcessed(eventTime)
        }
    }

    override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime?, reason: Int) {
        super.onPositionDiscontinuity(eventTime, reason)
        when (reason) {
            //Seek adjustment due to being unable to seek to the requested position or because the seek was permitted to be
            // inexact.
            DISCONTINUITY_REASON_SEEK_ADJUSTMENT,
                //Seek within the current period or to another period.
            DISCONTINUITY_REASON_SEEK,
                //Discontinuity to or from an ad within one period in the timeline.
            DISCONTINUITY_REASON_AD_INSERTION,
                //Discontinuity introduced internally by the source.
            DISCONTINUITY_REASON_INTERNAL -> {
            }
            //Automatic playback transition from one period in the timeline to the next.
            DISCONTINUITY_REASON_PERIOD_TRANSITION -> {
                val repeatMode = exoPlayer?.repeatMode
                when (repeatMode) {
                    REPEAT_MODE_ALL -> {
                        if (eventTime?.windowIndex ?: 0 == 0) {
                            playListenerList.forEach {
                                it.onLoopingEnd(eventTime)
                            }
                        }
                    }
                    REPEAT_MODE_ONE -> {
                        playListenerList.forEach {
                            it.onLoopingSingleEnd(eventTime)
                        }
                    }
                }
                playListenerList.forEach {
                    it.onPlayNext(eventTime)
                }
            }
        }
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?, reason: Int) {
        super.onTimelineChanged(eventTime, reason)
        playListenerList.forEach {
            it.onTimelineChanged(eventTime)
        }
        Log.w(TAG, "onTimelineChanged")
        seekTo(pendSeekPos)
    }

    //这个是加载网络的时候的状态，片段加载，会start->completed->start这样轮询
    override fun onLoadStarted(eventTime: AnalyticsListener.EventTime?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        super.onLoadStarted(eventTime, loadEventInfo, mediaLoadData)
        Log.w(TAG, "onLoadStarted")
    }

    override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime?, loadEventInfo: MediaSourceEventListener.LoadEventInfo?, mediaLoadData: MediaSourceEventListener.MediaLoadData?) {
        super.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData)
        Log.w(TAG, "onLoadCompleted")
    }

    override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime?, isLoading: Boolean) {
        super.onLoadingChanged(eventTime, isLoading)
        Log.w(TAG, "onLoadingChanged = " + isLoading)
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime?,
        loadEventInfo: MediaSourceEventListener.LoadEventInfo?,
        mediaLoadData: MediaSourceEventListener.MediaLoadData?,
        error: IOException?,
        wasCanceled: Boolean
    ) {
        super.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled)
        Log.w(TAG, "onLoadError: wasCacneled = ${wasCanceled}, error = ${error?.message.toString()}" )
        error?.printStackTrace()
    }
}

