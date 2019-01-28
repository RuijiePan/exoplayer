package com.panruijie.exoplayer.util

import android.content.Context
import android.net.Uri

import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.RawResourceDataSource

/**
 * Created by panruijie on 18-9-19.
 * exo专用uri解析工具
 */
object UriParseUtil {

    /**
     * exoplayer解析uri需要的特殊处理方法
     */
    fun rawResourceIdToUri(context: Context, rawResourceId: Int): Uri? {
        val uri : Uri?
        val dataSpec = DataSpec(RawResourceDataSource.buildRawResourceUri(rawResourceId))
        val rawResourceDataSource = RawResourceDataSource(context)
        try {
            rawResourceDataSource.open(dataSpec)
            uri = rawResourceDataSource.uri
        } catch (e: RawResourceDataSource.RawResourceDataSourceException) {
            e.printStackTrace()
            return null
        } finally {
            rawResourceDataSource.close()
        }

        return uri
    }
}
