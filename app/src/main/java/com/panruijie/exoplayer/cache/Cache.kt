package com.panruijie.exoplayer.cache

/**
 * Created by panruijie on 2019/1/28.
 * 缓存管理
 **/
class Cache(builder: CacheBuilder) {

    val cachDir : String
    val cacheSize : Long
    val cacheName : String

    init {
        cachDir = builder.cacheDir
        cacheName = builder.cacheName
        cacheSize = builder.cacheSize
    }
}