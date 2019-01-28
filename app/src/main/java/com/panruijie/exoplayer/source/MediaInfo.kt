package com.panruijie.exoplayer.source

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaMetadataRetriever.*
import android.net.Uri
import com.panruijie.exoplayer.base.IExoMediaInfo
import com.panruijie.exoplayer.util.NumberUtils
import com.google.android.exoplayer2.util.Util
import java.io.File
import java.lang.Exception

/**
 * Created by panruijie on 2019/1/22.
 **/
class MediaInfo(val context : Context, override val path: String?, override val rawId : Int?,
                override var uri: Uri?, override var clipStart: Long, override var clipEnd: Long) : IExoMediaInfo {

    val isLocalFile : Boolean
    val contentType : Int
    var degree : Int = 0
    var duration : Int = 0
    var bitRate : Int = 0
    var originalWidth : Int = 0
    var originalHeight : Int = 0
    var width : Int = 0
    var height : Int = 0

    init {
        if (rawId != null) {
            uri = Uri.parse("android.resource://" + context.getPackageName() + "/" + rawId)
        } else if (path != null) {
            uri = Uri.parse(path)
        }
        Util.inferContentType(uri).also {
            contentType = it
        }
        isLocalFile = rawId != null || File(path).exists()
        if (isLocalFile) {
            initMetadata()
        }
    }

    constructor(context: Context, path: String?) : this(context, path, null, null, 0, 0)

    constructor(context: Context, u: Uri?) : this(context, null, null, u, 0, 0)

    constructor(context: Context, rawId : Int?) : this(context, null, rawId, null, 0, 0)

    @SuppressWarnings
    override fun initMetadata() {
        val retriever = MediaMetadataRetriever()
        try {
            if (path != null) {
                if (File(path).exists()) {
                    retriever.setDataSource(path)
                } else {
                    return
                }
            } else if (uri != null) {
                retriever.setDataSource(context, uri)
            } else {
                return
            }
            degree = NumberUtils.getInteger(retriever.extractMetadata(METADATA_KEY_VIDEO_ROTATION))
            duration = NumberUtils.getInteger(retriever.extractMetadata(METADATA_KEY_DURATION))
            bitRate = NumberUtils.getInteger(retriever.extractMetadata(METADATA_KEY_BITRATE))
            originalWidth = NumberUtils.getInteger(retriever.extractMetadata(METADATA_KEY_VIDEO_WIDTH))
            originalHeight = NumberUtils.getInteger(retriever.extractMetadata(METADATA_KEY_VIDEO_HEIGHT))
            width = if (isDisplayRotate()) originalHeight else originalWidth
            height = if (isDisplayRotate()) originalWidth else originalHeight
            clipStart = 0L
            clipEnd = duration * 1000L
            retriever.release()
        } catch (e : Exception) {
            e.printStackTrace()
        } finally {
            retriever.release()
        }
    }

    private fun isDisplayRotate(): Boolean {
        if (degree == 90 || degree == 270) {
            return true
        }
        return false
    }

}