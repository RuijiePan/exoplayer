package com.panruijie.exoplayer.core.renderer

/**
 * Created by panruijie on 2019/2/1.
 *
 * @param rendererTrackIndexes : The exo player renderer track indexes
 * @param rendererTrackIndex : The renderer track index related to the requested <code>groupIndex</code>
 * @param rendererTrackGroupIndex :  The corrected exoplayer group index which may be used to obtain proper track group from the renderer
 **/
class ExoPlayerRendererTracksInfo(val rendererTrackIndexes : MutableList<Int>, val rendererTrackIndex : Int, val rendererTrackGroupIndex : Int)