package com.panruijie.exoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.AppCompatSeekBar
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.SeekBar

import com.panruijie.exoplayer.base.IPlayListener
import com.panruijie.exoplayer.source.GLDisPlay
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout

import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.RESIZE_MODE_FIT
import com.panruijie.exoplayer.R
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IPlayListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener {

    companion object {
        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE =
            arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")
    }

    private lateinit var textureView: TextureView
    private lateinit var aspectRatioFrameLayout: AspectRatioFrameLayout
    private lateinit var goExoPlayer: GoExoPlayer
    private lateinit var progressBar: AppCompatSeekBar
    private lateinit var loadingProgress: ProgressBar
    private lateinit var playButton : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aspectRatioFrameLayout = findViewById(R.id.aspectFrameLayout)
        textureView = findViewById(R.id.textureView)
        progressBar = findViewById(R.id.progressSeekBar)
        loadingProgress = findViewById(R.id.loadingProgress)
        playButton = findViewById(R.id.playButton)

        aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FIT
        aspectRatioFrameLayout.setAspectRatio(1f)

        requestPermission()
        initPlayer()
        setListener()
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_EXTERNAL_STORAGE)
            }
        }
    }

    private fun initPlayer() {
        goExoPlayer = GoExoPlayer(this)
        //https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8
        //https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd
        //goExoPlayer.setMediaInfo("/storage/emulated/0/DCIM/WonderVideo/VID_20190124_122309771.mp4");
        goExoPlayer.setMediaInfo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")
        //goExoPlayer.setMediaInfo("https://commondatastorage.googleapis.com/gtv-videos-bucket/CastVideos/hls/TearsOfSteel.m3u8")
        goExoPlayer.setDisPlay(GLDisPlay(textureView))
        goExoPlayer.addPlayListener(this)
    }

    private fun setListener() {
        progressBar.setOnSeekBarChangeListener(this)
        aspectRatioFrameLayout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v?.id) {
            R.id.aspectFrameLayout -> {
                if (goExoPlayer.isPlaying()) {
                    goExoPlayer.pausePlay()
                } else {
                    goExoPlayer.startPlay()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_EXTERNAL_STORAGE) {
            for (i in permissions.indices) {
                if (grantResults[i] == 0) {
                    goExoPlayer.initPlayer()
                }
                Log.i("MainActivity", "申请的权限为：" + permissions[i] + ",申请结果：" + grantResults[i])
            }
        }
    }

    override fun onStart() {
        super.onStart()
        goExoPlayer.onStart()
    }

    override fun onResume() {
        super.onResume()
        goExoPlayer.onResume()
    }

    override fun onStop() {
        super.onStop()
        goExoPlayer.onStop()
    }

    override fun onPause() {
        super.onPause()
        goExoPlayer.onPause()
    }

    override fun onPlayResume() {
        playButton.visibility = View.INVISIBLE
    }

    override fun onPlayPause() {
        playButton.visibility = View.VISIBLE
    }

    override fun onPlayPosition(pos: Long) {
        progressSeekBar.progress = pos.toInt()
    }

    override fun onPlayNext(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onPlayCompleted(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onBufferStart(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {
        loadingProgress.visibility = View.VISIBLE
        playButton.visibility = View.INVISIBLE
    }

    override fun onBufferEnd(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {
        loadingProgress.visibility = View.INVISIBLE
        playButton.visibility = if (playWhenReady) View.INVISIBLE else View.VISIBLE
    }

    override fun onBufferingUpdate(percent: Int?) {
        Log.w(GoExoPlayer.TAG, "percent = " + percent)
    }

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime?) {
        loadingProgress.visibility = View.VISIBLE
    }

    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onSeekComoleted(eventTime: AnalyticsListener.EventTime?, playWhenReady: Boolean) {
        loadingProgress.visibility = View.INVISIBLE
        playButton.visibility = if (playWhenReady) View.INVISIBLE else View.VISIBLE
    }

    override fun onVideoSizeChanged(
        eventTime: AnalyticsListener.EventTime?,
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        var width = width
        var height = height
        if (unappliedRotationDegrees == 90 || unappliedRotationDegrees == 270) {
            val temp = width
            width = height
            height = temp
        }
        aspectRatioFrameLayout.setAspectRatio(width * 1.0f / height)
    }

    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime?, surface: Surface?) {

    }

    override fun onLoadingSource(loadingStatue: IPlayListener.LoadingStatue) {
        if (loadingStatue == IPlayListener.LoadingStatue.START ||
            loadingStatue == IPlayListener.LoadingStatue.ERROR
        ) {
            progressBar.visibility = View.INVISIBLE
        } else {
            progressBar.visibility = View.VISIBLE
        }
    }

    override fun onPlayError(eventTime: AnalyticsListener.EventTime?, reason: String) {

    }

    override fun onLoopingEnd(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onLoopingSingleEnd(eventTime: AnalyticsListener.EventTime?) {

    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime?) {
        val timeline = eventTime?.timeline

        var cumulativePositionMs: Long = 0
        val window = Timeline.Window()

        for (index in 0 until timeline?.getWindowCount()!!) {
            timeline.getWindow(index, window)
            cumulativePositionMs += window.durationMs
        }
        //Toast.makeText(this, "time = " + cumulativePositionMs, Toast.LENGTH_SHORT).show()
        progressBar.max = cumulativePositionMs.toInt()
    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        if (fromUser) {
            progressBar.progress = progress
            goExoPlayer.seekTo(progress.toLong())
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

}
