package com.puxin.webplayer.utils

import android.view.KeyEvent
import androidx.fragment.app.Fragment

/**
 * Fragment 基类
 * 抽象按键监听
 * */
abstract class BaseFragment: Fragment() {
    fun isFocus() = view?.hasFocus()

    abstract fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean
}