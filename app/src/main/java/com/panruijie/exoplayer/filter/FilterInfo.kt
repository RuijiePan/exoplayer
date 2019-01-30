package com.panruijie.exoplayer.filter

/**
 * Created by panruijie on 2019/1/30.
 **/
enum class FilterInfo(name : String, var check : Boolean = false) {

    FILTER_ORIGINAL("original", true),
    FILTER_SOLARIZE("solarize"),
    FILTER_VIGNETTE("vignette"),
    FILTER_WEAK_PIXEL( "weakPixel"),
    FILTER_CONVOLUTION("convolution"),
    FILTER_ADD_BLEND("addBlend"),
    FILTER_ALPHA_BLEND( "alphaBlend"),
    FILTER_BOX_BLUR("boxBlur"),
    FILTER_BRIGHTNESS( "brightness"),
    FILTER_FALSE_COLOR("falseColor"),
}