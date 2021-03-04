package com.puxin.webplayer.utils

import com.puxin.webplayer.MyApplication

object DensityUtil {
    fun dip2px(dpValue: Float): Int {
        val scale = MyApplication.context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }

    fun px2dip(pxValue: Float): Int {
        val scale = MyApplication.context.resources.displayMetrics.density
        return (pxValue / scale + 0.5f).toInt()

    }
}