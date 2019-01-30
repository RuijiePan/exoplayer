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
            item?.check = ischeck
            if (ischeck) {
                mData.forEachIndexed { index, filterInfo ->
                    if (index != position) {
                        filterInfo.check = false
                    }
                }
                if (lastIndex != -1) {
                    notifyItemChanged(lastIndex)
                }
                lastIndex = position
                listener?.filterChoose(holder.adapterPosition)
            } else {
                listener?.filterChoose(0)
            }
        }
    }

    interface IFilterChooseListener {

        fun filterChoose(id: Int)
    }

}
