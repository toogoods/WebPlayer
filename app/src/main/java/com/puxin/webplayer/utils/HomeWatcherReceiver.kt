package com.puxin.webplayer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import kotlin.system.exitProcess

/**
 * Home按键监听
 * */
class HomeWatcherReceiver: BroadcastReceiver() {
    private val TAG = "homeReceiver"

    private val REASON = "reason"
    private val RECENT_APPS = "recentapps"
    private val HOME = "homekey"
    private val LOCK = "lock"
    private val ASSIST = "assist"

    override fun onReceive(p0: Context, p1: Intent) {
        val action = p1.action
        LogUtil.i(TAG, "action: $action")

        if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            val reason = p1.getStringExtra(REASON)
            LogUtil.i(TAG, "reason: $reason")

            when(reason) {
                HOME -> {
                    //短按home键
                    LogUtil.i(TAG, "homekey")
                    ActivityController.finishAll()
                    exitProcess(0)
                }
                RECENT_APPS -> {
                    //长按home键 或 activity 切换键
                    LogUtil.i(TAG, "long press home key or activity switch")
                    ActivityController.finishAll()
                }
                LOCK -> {
                    LogUtil.i(TAG, "lock")
                }
                ASSIST -> {
                    LogUtil.i(TAG, "assist")
                }
            }
        }

    }
}