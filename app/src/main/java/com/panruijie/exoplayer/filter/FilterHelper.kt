package com.panruijie.exoplayer.filter

import jp.co.cyberagent.android.gpuimage.filter.*
import java.lang.IllegalArgumentException

/**
 * Created by panruijie on 2019/1/30.
 **/
object FilterHelper {

    fun createFilterFromId(id : Int) : GPUImageFilter? {
        when(id) {
            FilterInfo.FILTER_ORIGINAL.ordinal -> {
                return null
            }
            FilterInfo.FILTER_CONVOLUTION.ordinal -> {
                return GPUImage3x3ConvolutionFilter()
            }
            FilterInfo.FILTER_ADD_BLEND.ordinal -> {
                return GPUImageAddBlendFilter()
            }
            FilterInfo.FILTER_ALPHA_BLEND.ordinal -> {
                return GPUImageAlphaBlendFilter()
            }
            FilterInfo.FILTER_BOX_BLUR.ordinal -> {
                return GPUImageBoxBlurFilter()
            }
            FilterInfo.FILTER_BRIGHTNESS.ordinal -> {
                return GPUImageBrightnessFilter()
            }
            FilterInfo.FILTER_FALSE_COLOR.ordinal -> {
                return GPUImageFalseColorFilter()
            }
            FilterInfo.FILTER_SOLARIZE.ordinal -> {
                return GPUImageSolarizeFilter()
            }
            FilterInfo.FILTER_VIGNETTE.ordinal -> {
                return GPUImageVignetteFilter()
            }
            FilterInfo.FILTER_WEAK_PIXEL.ordinal -> {
                return GPUImageWeakPixelInclusionFilter()
            }
            else -> {
                throw IllegalArgumentException("Not filter found!")
            }
        }
    }
}