package com.puxin.webplayer.utils

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.puxin.webplayer.MyApplication

/**
 * Activity 基类
 * 抽象所有Activity共同逻辑
 * */
open class BaseActivity: AppCompatActivity(), NetTypeBroadcastReceiver.NetEventHandler {

    private val TAG = "BaseActivity"

    private var mHomeWatcherReceiver: HomeWatcherReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtil.d(TAG, javaClass.simpleName)
        mHomeWatcherReceiver ?: registerHomeKeyReceiver()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterHomeKeyReceiver()
    }

    //广播监听Home键
    private fun registerHomeKeyReceiver() {
        LogUtil.d(TAG, "registerHomeKeyReceiver")
        mHomeWatcherReceiver = HomeWatcherReceiver()
        val intentFilter = IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)
        MyApplication.context.registerReceiver(mHomeWatcherReceiver, intentFilter)
    }

    //取消广播监听Home键
    private fun unregisterHomeKeyReceiver() {
        LogUtil.d(TAG, "unregisterHomeKeyReceiver")
        mHomeWatcherReceiver?.let { receiver ->
            MyApplication.context.unregisterReceiver(receiver)
        }
    }

    override fun onNetChange() {
        if (!NetworkChangeUtil.getNetworkState(this)) {
            "请稍后重试或联系xxxxx".toast("网络连接失败")
            NetworkChangeUtil.networkType = false
        } else {
            "网络恢复链接".toast(null)
            NetworkChangeUtil.networkType = true
        }
    }
}