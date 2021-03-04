package com.puxin.webplayer.utils

import android.net.TrafficStats
import android.os.Handler
import com.puxin.webplayer.MyApplication
import java.util.*

/**
 * 计算网速工具
 * */
object NetWorkSpeedUtil {
    private var mHandler: Handler? = null

    private var lastTotalRxBytes: Long = 0
    private var lastTimeStamp: Long = 0

    const val SHOW_NET_SPEED = 100

    private lateinit var timer: Timer

    private val task = object: TimerTask() {
        override fun run() {
            showNetSpeed()
        }
    }

    fun startShowNetSpeed() {
        lastTotalRxBytes = getTotalRxBytes()
        lastTimeStamp = System.currentTimeMillis()
        timer = Timer()
        timer.schedule(task, 1000, 1000)
    }

    fun stopShowNetSpeed() {
        timer.cancel()
    }

    private fun showNetSpeed() {
        var unit = "Kb/s"
        val nowTotalRxBytes = getTotalRxBytes()
        val nowTimeStamp = System.currentTimeMillis()
        var speed = (nowTotalRxBytes - lastTotalRxBytes) * 1000 / (nowTimeStamp - lastTimeStamp)
        var speed2 = (nowTotalRxBytes - lastTotalRxBytes) * 1000 % (nowTimeStamp - lastTimeStamp) / 100

        if (speed >= 1024) {
            speed2 = speed % 1024 / 100
            speed /= 1024
            unit = "Mb/s"
        }

        lastTimeStamp = nowTimeStamp
        lastTotalRxBytes = nowTotalRxBytes

        val message = mHandler?.obtainMessage()
        message?.what = SHOW_NET_SPEED
        message?.obj = "$speed.$speed2$unit"
        mHandler?.sendMessage(message!!)
    }

    private fun getTotalRxBytes() = TrafficStats.getUidRxBytes(
        if (MyApplication.context.applicationInfo.uid == TrafficStats.UNSUPPORTED) {
            0
        } else {
            (TrafficStats.getTotalRxBytes() / 1024).toInt()
        }
    )
}