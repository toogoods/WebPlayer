package com.puxin.webplayer.anim

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.widget.FrameLayout

class AnimatorFrameLayout(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    //默认小窗大小
    private var rectData = RectData(150f, 90f, 404f, 228f)
    fun setRectData(rect: RectData) {
        rectData = rect
        invalidate()
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        val newLayoutParams = layoutParams as LayoutParams
        newLayoutParams.setMargins(rectData.left.toInt(), rectData.top.toInt(), 0, 0)
        newLayoutParams.width = rectData.width.toInt()
        newLayoutParams.height = rectData.height.toInt()
        this.layoutParams = newLayoutParams
        super.onDraw(canvas)
    }

}