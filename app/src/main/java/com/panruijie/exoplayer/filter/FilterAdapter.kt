package com.panruijie.exoplayer.filter

import android.content.Context
import android.widget.CheckBox
import android.widget.CompoundButton
import com.panruijie.exoplayer.R
import com.panruijie.exoplayer.base.adapter.BaseAdapter
import com.panruijie.exoplayer.base.adapter.BaseViewHolder
import jp.co.cyberagent.android.gpuimage.filter.*
import java.lang.IllegalArgumentException

/**
 * Created by panruijie on 2019/1/30.
 **/
class FilterAdapter(context: Context?, data: MutableList<FilterInfo>?) :
    BaseAdapter<FilterInfo, BaseViewHolder>(context, R.layout.filter_item, data) {

    var lastIndex = -1
    var listener : IFilterChooseListener? = null

    override fun convert(holder: BaseViewHolder?, item: FilterInfo?, position: Int) {
        val checkBox = holder?.getView<CheckBox>(R.id.checkBox)
        checkBox?.isChecked = item?.check?: false
        holder?.setText(R.id.filterName, item?.name)
        checkBox?.setOnCheckedChangeListener {
            _, ischeck ->
            if (ischeck) {
                item?.check = true
                mData.forEachIndexed { index, filterInfo ->
                    if (index != position) {
                        filterInfo.check = false
                    }
                }
                listener?.filterChoose(createFilterFromId(position))
                notifyItemChanged(position)
                if (lastIndex != -1) {
                    notifyItemChanged(lastIndex)
                }
                lastIndex = position
            }
        }
    }

    interface IFilterChooseListener {

        fun filterChoose(filter : GPUImageFilter)
    }

    private fun createFilterFromId(id : Int) : GPUImageFilter {
        when(id) {
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
