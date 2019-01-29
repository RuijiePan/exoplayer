package com.panruijie.exoplayer.cache

import android.content.Context
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.upstream.HttpDataSource
import com.google.android.exoplayer2.upstream.TransferListener
import com.google.android.exoplayer2.upstream.cache.CacheDataSource
import com.google.android.exoplayer2.upstream.cache.CacheDataSourceFactory
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor
import com.google.android.exoplayer2.upstream.cache.SimpleCache
import com.google.android.exoplayer2.util.Util
import okhttp3.OkHttpClient
import java.io.File

/**
 * Created by panruijie on 2019/1/28.
 * 网络缓存管理器
 **/
object DataSourceFactoryProvider {

    val CACHE_DIR = null
    val CACHE_NAME = "exoplayer"
    val CACHE_SIZE = 100 * 1024 * 1024L

    interface DataSourceFactoryProvider {
        fun create(context: Context, userAgent: String, listener: TransferListener?): DataSource.Factory
    }

    enum class ProviderType {
        APACHE , OKHTTP
    }

    lateinit var provider : DataSourceFactoryProvider
    var cache : Cache = Cache(CacheBuilder(CACHE_DIR, CACHE_NAME, CACHE_SIZE))

    fun setProviderType(type : ProviderType) {
        when(type) {
            ProviderType.APACHE -> {
                provider = apacheProvider
            }
            ProviderType.OKHTTP -> {
                provider = okHttpProvider
            }
        }
    }

    private val apacheProvider = object : DataSourceFactoryProvider {

        override fun create(context: Context, userAgent: String, listener: TransferListener?): DataSource.Factory {

            return DefaultDataSourceFactory(context, Util.getUserAgent(context, context.getPackageName()))
        }
    }

    private val okHttpProvider = object : DataSourceFactoryProvider {

        private var instance: CacheDataSourceFactory? = null

        override fun create(context: Context, userAgent: String, listener: TransferListener?): DataSource.Factory {
            if (instance == null) {
                synchronized(DataSourceFactoryProvider::class.java) {
                    if (instance == null) {
                        // Updates the network data source to use the OKHttp implementation
                        val upstreamFactory = OkHttpDataSourceFactory(OkHttpClient(), userAgent, listener)

                        //cache dir is null use app cahce path, otherwise use customer
                        val cacheDir = cache.cachDir?: context.cacheDir.absolutePath
                        // Adds a cache around the upstreamFactory
                        val cache = SimpleCache(
                            File(cacheDir, cache.cacheName),
                            LeastRecentlyUsedCacheEvictor(cache.cacheSize)
                        )
                        instance =
                            CacheDataSourceFactory(cache, upstreamFactory, CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
                    }
                }
            }

            return instance!!
        }
    }
}