package com.puxin.webplayer.anim

import android.animation.TypeEvaluator

class RectEvaluator: TypeEvaluator<RectData> {
    override fun evaluate(fraction: Float, p1: RectData, p2: RectData): RectData {

        val left = getCurrentMargin(p1.left, p2.left, fraction)
        val top = getCurrentMargin(p1.top, p2.top, fraction)
        val width = getCurrentSize(p1.width, p2.width, fraction)
        val height = getCurrentSize(p1.height, p2.height, fraction)
        return RectData(left, top, width, height)
    }

    private fun getCurrentMargin(startValue: Float, endValue: Float, fraction: Float): Float = startValue + fraction * (endValue - startValue)
    private fun getCurrentSize(startValue: Float, endValue: Float, fraction: Float): Float = startValue + fraction * (endValue - startValue)

}