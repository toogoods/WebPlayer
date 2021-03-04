package com.puxin.webplayer.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * 网络连接状态监听
 * 反馈网络变化
 * */
class NetTypeBroadcastReceiver: BroadcastReceiver() {
    companion object {
        val listeners = ArrayList<NetEventHandler>()
        private val NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE"
    }

    override fun onReceive(p0: Context, p1: Intent) {
        if (p1.action == NET_CHANGE_ACTION) {
            if (listeners.size > 0) {
                for (handle in listeners) {
                    handle.onNetChange()
                }
            }
        }
    }

    interface NetEventHandler {
        fun onNetChange()
    }

}