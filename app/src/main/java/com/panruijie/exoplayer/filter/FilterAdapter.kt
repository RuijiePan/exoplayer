package com.panruijie.exoplayer.filter

import android.content.Context
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
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

    var listener: IFilterChooseListener? = null
    private val mainHanlder = Handler(Looper.getMainLooper())

    override fun convert(holder: BaseViewHolder?, item: FilterInfo?, position: Int) {
        holder?.setText(R.id.filterName, item?.filterName)
        val text = holder?.getView<TextView>(R.id.filterName)
        if(item?.check?: false) {
            text?.setTextColor(Color.RED)
        } else {
            text?.setTextColor(Color.BLACK)
        }

        text?.setOnClickListener {
            item?.check = true
            mData.forEachIndexed { index, filterInfo ->
                if (index != position) {
                    filterInfo.check = false
                }
            }
            listener?.filterChoose(position)
            mainHanlder.post {
                notifyDataSetChanged()
            }
        }
    }

    interface IFilterChooseListener {

        fun filterChoose(id: Int)

    }

}
