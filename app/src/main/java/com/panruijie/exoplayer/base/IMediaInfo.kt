package com.panruijie.exoplayer.base

import android.net.Uri

/**
 * Created by panruijie on 2019/1/22.
 * 基本信息
 **/
interface IMediaInfo {

    val path : String?

    var uri : Uri?

    fun initMetadata()
}