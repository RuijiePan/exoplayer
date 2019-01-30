package com.panruijie.exoplayer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Build
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.support.v7.widget.AppCompatSeekBar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.*

import com.panruijie.exoplayer.base.IPlayListener
import com.panruijie.exoplayer.source.GLDisPlay
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.analytics.AnalyticsListener
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.*

import com.panruijie.exoplayer.R
import com.panruijie.exoplayer.base.adapter.BaseLinearLayoutManager
import com.panruijie.exoplayer.cache.DataSourceFactoryProvider
import com.panruijie.exoplayer.filter.FilterAdapter
import com.panruijie.exoplayer.filter.FilterHelper
import com.panruijie.exoplayer.filter.FilterInfo
import com.panruijie.exoplayer.gpuimage.GoGpuImage
import com.panruijie.exoplayer.gpuimage.IRenderCallback
import com.panruijie.exoplayer.gpuimage.filter.GPUImageOESFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageBoxBlurFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilter
import jp.co.cyberagent.android.gpuimage.filter.GPUImageFilterGroup
import jp.co.cyberagent.android.gpuimage.util.Rotation
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), IPlayListener, SeekBar.OnSeekBarChangeListener, View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    companion object {
        private val REQUEST_EXTERNAL_STORAGE = 1
        private val PERMISSIONS_STORAGE =
            arrayOf("android.permission.READ_EXTERNAL_STORAGE", "android.permission.WRITE_EXTERNAL_STORAGE")

        private val SS = ""
        //https://storage.googleapis.com/wvmedia/clear/h264/tears/tears.mpd
        private val DASH = "https://content.uplynk.com/channel/3c367669a83b4cdab20cceefac253684.mpd?ad=cleardashnew"
        //直播
        private val HLS = "http://devimages.apple.com/iphone/samples/bipbop/bipbopall.m3u8"
        //网络MP4
        private val OTHER = "http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4"
    }

    private lateinit var aspectRatioFrameLayout: AspectRatioFrameLayout
    private lateinit var goExoPlayer: GoExoPlayer
    private lateinit var progressBar: AppCompatSeekBar
    private lateinit var loadingProgress: ProgressBar
    private lateinit var playButton : ImageView
    private lateinit var gpuImage : GoGpuImage
    private val filterGroup = GPUImageFilterGroup()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        aspectRatioFrameLayout = findViewById(R.id.aspectFrameLayout)
        progressBar = findViewById(R.id.progressSeekBar)
        loadingProgress = findViewById(R.id.loadingProgress)
        playButton = findViewById(R.id.playButton)

        aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FIT
        aspectRatioFrameLayout.setAspectRatio(1f)

        requestPermission()
        initData()
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

    private fun initData() {
        typeOTHER.isChecked = true
        fit.isChecked = true
        okhttpProvider.isChecked = true
        speedSeekbar.max = 400
        speedSeekbar.progress = 100
        pitchSeekbar.max = 400
        pitchSeekbar.progress = 100
        renderAfterSeek.isChecked = true
        mode_off.isChecked = true
        path.text = OTHER

        val filterAdapter = FilterAdapter(this, FilterInfo.values().toMutableList())
        recyclerview.setItemAnimator(DefaultItemAnimator())
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerview.setLayoutManager(layoutManager)
        recyclerview.setHasFixedSize(true)
        recyclerview.setAdapter(filterAdapter)
        filterAdapter.listener = object : FilterAdapter.IFilterChooseListener {
            override fun filterChoose(id: Int) {
                filterGroup.filters.clear()
                filterGroup.addFilter(GPUImageOESFilter())
                FilterHelper.createFilterFromId(id).also {
                    if (it != null) {
                        filterGroup.addFilter(it)
                    }
                }
                gpuImage.setFilter(filterGroup)
            }
        }
    }

    private fun initPlayer() {
        goExoPlayer = GoExoPlayer(this)
        goExoPlayer.setMediaInfo("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4")

        filterGroup.addFilter(GPUImageOESFilter())
        goExoPlayer.addPlayListener(this)

        gpuImage = GoGpuImage(this, object : IRenderCallback {
            override fun onSurfaceTextureCreated(surfaceTexture: SurfaceTexture?) {
                goExoPlayer.setDisPlay(GLDisPlay(surfaceTexture))
                goExoPlayer.initPlayer()
            }

            override fun onFrameAvailable(frameTimeNanos: Long) {
                gpuImage.requestRender()
            }

        })
        gpuImage.setScaleType(GoGpuImage.ScaleType.CENTER_CROP)
        gpuImage.setFilter(filterGroup)
        gpuImage.setGLSurfaceView(glSurfaceView)
        //gpuImage.setRotation(Rotation.ROTATION_180)
    }

    private fun setListener() {
        progressBar.setOnSeekBarChangeListener(this)
        aspectRatioFrameLayout.setOnClickListener(this)

        typeSS.setOnCheckedChangeListener(this)
        typeDASH.setOnCheckedChangeListener(this)
        typeHLS.setOnCheckedChangeListener(this)
        typeOTHER.setOnCheckedChangeListener(this)

        fit.setOnCheckedChangeListener(this)
        fit_width.setOnCheckedChangeListener(this)
        fit_height.setOnCheckedChangeListener(this)
        fill.setOnCheckedChangeListener(this)
        zoom.setOnCheckedChangeListener(this)

        apacheProvider.setOnCheckedChangeListener(this)
        okhttpProvider.setOnCheckedChangeListener(this)

        speedSeekbar.setOnSeekBarChangeListener(this)
        pitchSeekbar.setOnSeekBarChangeListener(this)

        mode_one.setOnCheckedChangeListener(this)
        mode_all.setOnCheckedChangeListener(this)
        mode_off.setOnCheckedChangeListener(this)

        rotation0.setOnCheckedChangeListener(this)
        rotation90.setOnCheckedChangeListener(this)
        rotation180.setOnCheckedChangeListener(this)
        rotation270.setOnCheckedChangeListener(this)
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
                /*if (grantResults[i] == 0) {
                    goExoPlayer.initPlayer()
                }*/
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
        //glSurfaceView.onResume()
    }

    override fun onStop() {
        super.onStop()
        goExoPlayer.onStop()
    }

    override fun onPause() {
        super.onPause()
        goExoPlayer.onPause()
        //glSurfaceView.onPause()
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
        gpuImage.setInputInfo(width, height)
        gpuImage.setRotation(Rotation.fromInt(unappliedRotationDegrees))
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
            when(seekBar.id) {
                R.id.progressSeekBar -> {
                    progressBar.progress = progress
                    goExoPlayer.seekTo(progress.toLong())
                }
                R.id.speedSeekbar -> {
                    if (progress < 50) {
                        seekBar.progress = 50
                    } else {
                        goExoPlayer.setSpeed(progress / 100f)
                    }
                }
                R.id.pitchSeekbar -> {
                    if (progress < 50) {
                        seekBar.progress = 50
                    } else {
                        goExoPlayer.setPitch(progress / 100f)
                    }
                }
            }
        }
    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {

    }

    override fun onStopTrackingTouch(seekBar: SeekBar) {

    }

    override fun onCheckedChanged(buttonView: CompoundButton?, isChecked: Boolean) {
        if (isChecked) {
            when (buttonView?.id) {
                R.id.typeSS -> {
                    Toast.makeText(this, "not support now!", Toast.LENGTH_SHORT).show()
                    /*unCheck(mutableListOf(typeDASH, typeHLS, typeOTHER))
                    goExoPlayer.releasePlayer()
                    goExoPlayer.setMediaInfo(SS)
                    goExoPlayer.initPlayer()
                    path.text = SS*/
                }
                R.id.typeDASH -> {
                    unCheck(mutableListOf(typeSS, typeHLS, typeOTHER))
                    goExoPlayer.releasePlayer()
                    goExoPlayer.setMediaInfo(DASH)
                    goExoPlayer.initPlayer()
                    path.text = DASH
                }
                R.id.typeHLS -> {
                    unCheck(mutableListOf(typeDASH, typeSS, typeOTHER))
                    goExoPlayer.releasePlayer()
                    goExoPlayer.setMediaInfo(HLS)
                    goExoPlayer.initPlayer()
                    path.text = HLS
                }
                R.id.typeOTHER -> {
                    unCheck(mutableListOf(typeDASH, typeHLS, typeSS))
                    goExoPlayer.releasePlayer()
                    goExoPlayer.setMediaInfo(OTHER)
                    goExoPlayer.initPlayer()
                    path.text = OTHER
                }

                R.id.fit -> {
                    aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FIT
                    unCheck(mutableListOf(fit_width, fit_height, fill, zoom))
                }
                R.id.fit_width -> {
                    aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FIXED_WIDTH
                    unCheck(mutableListOf(fit, fit_height, fill, zoom))
                }
                R.id.fit_height -> {
                    aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FIXED_HEIGHT
                    unCheck(mutableListOf(fit_width, fit, fill, zoom))
                }
                R.id.fill -> {
                    aspectRatioFrameLayout.resizeMode = RESIZE_MODE_FILL
                    unCheck(mutableListOf(fit_width, fit_height, fit, zoom))
                }
                R.id.zoom -> {
                    aspectRatioFrameLayout.resizeMode = RESIZE_MODE_ZOOM
                    unCheck(mutableListOf(fit_width, fit_height, fill, fit))
                }

                R.id.apacheProvider -> {
                    DataSourceFactoryProvider.setProviderType(DataSourceFactoryProvider.ProviderType.APACHE)
                    okhttpProvider.isChecked = false
                    goExoPlayer.releasePlayer()
                    goExoPlayer.initPlayer()
                }
                R.id.okhttpProvider -> {
                    DataSourceFactoryProvider.setProviderType(DataSourceFactoryProvider.ProviderType.OKHTTP)
                    apacheProvider.isChecked = false
                    goExoPlayer.releasePlayer()
                    goExoPlayer.initPlayer()
                }

                R.id.mode_all -> {
                    goExoPlayer.setLooping(true)
                    unCheck(mutableListOf(mode_off, mode_one))
                }
                R.id.mode_off -> {
                    goExoPlayer.setLooping(false)
                    goExoPlayer.setLoopingSingle(false)
                    unCheck(mutableListOf(mode_all, mode_one))
                }
                R.id.mode_one -> {
                    goExoPlayer.setLooping(false)
                    goExoPlayer.setLoopingSingle(true)
                    unCheck(mutableListOf(mode_off, mode_all))
                }

                R.id.rotation0 -> {
                    gpuImage.setRotation(Rotation.NORMAL)
                    unCheck(mutableListOf(rotation90, rotation180, rotation270))
                }
                R.id.rotation90 -> {
                    gpuImage.setRotation(Rotation.ROTATION_90)
                    unCheck(mutableListOf(rotation0, rotation180, rotation270))
                }
                R.id.rotation180 -> {
                    gpuImage.setRotation(Rotation.ROTATION_180)
                    unCheck(mutableListOf(rotation90, rotation0, rotation270))
                }
                R.id.rotation270 -> {
                    gpuImage.setRotation(Rotation.ROTATION_270)
                    unCheck(mutableListOf(rotation90, rotation180, rotation0))
                }

                R.id.renderAfterSeek -> {
                    goExoPlayer.isSeekableAfterFrameRenderer = true
                }
            }
        } else {
            if (buttonView?.id == R.id.renderAfterSeek) {
                goExoPlayer.isSeekableAfterFrameRenderer = false
            }
        }
    }

    private fun unCheck(view : MutableList<CheckBox>) {
        view.forEach {
            it.isChecked = false
        }
    }
}
