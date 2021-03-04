package com.puxin.webplayer.layout

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.widget.SeekBar

@SuppressLint("AppCompatCustomView")
class MySeekBar(context: Context, attrs: AttributeSet?, defStyleAttr: Int): SeekBar(context, attrs, defStyleAttr) {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        return true
    }
}