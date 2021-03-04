package com.puxin.webplayer.layout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import com.puxin.webplayer.R
import com.puxin.webplayer.utils.LogUtil
import java.util.Collections.sort
import kotlin.collections.ArrayList

class ZOrderFrameLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    init {
        isChildrenDrawingOrderEnabled = true
    }

    private val list = ArrayList<Pair<View,Int>>()

    override fun generateLayoutParams(attrs: AttributeSet?): FrameLayout.LayoutParams {
        return LayoutParams(context, attrs)
    }

    override fun getChildDrawingOrder(childCount: Int, drawingPosition: Int): Int {
        return indexOfChild(list[drawingPosition].first)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        initialZOrder()
    }

    private fun initialZOrder() {
        var view: View
        var params: LayoutParams
        for (i in 0 until childCount) {
            view = getChildAt(i)
            params = view.layoutParams as LayoutParams

            val pair = Pair(view, params.zOrder)
            list.add(pair)
        }

        sort(list) { p0, p1 ->
            LogUtil.d("FrameLayout", "result: ${p0.second - p1.second}")
            p0.second - p1.second
        }
    }

    fun refreshLayout() {
        list.clear()
        initialZOrder()
        LogUtil.d("FrameLayout", "refreshLayout")
        requestLayout()
    }

    companion object {
        @SuppressLint("Recycle", "CustomViewStyleable")
        class LayoutParams(context: Context, attrs: AttributeSet?): FrameLayout.LayoutParams(context, attrs) {
            private val DEFAULT_ZORDER = 1
            var zOrder: Int

            init {
                val a = context.obtainStyledAttributes(attrs, R.styleable.custom)
                zOrder = a.getInt(R.styleable.custom_layout_zorder, DEFAULT_ZORDER)
                a.recycle()
            }
        }
    }

}