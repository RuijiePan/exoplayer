package com.panruijie.exoplayer.base

/**
 * Created by panruijie on 2019/1/22.
 * exoplayer拓展
 **/
interface IExoMediaInfo : IMediaInfo {

    val rawId : Int?

    var clipStart : Long

    var clipEnd : Long

}