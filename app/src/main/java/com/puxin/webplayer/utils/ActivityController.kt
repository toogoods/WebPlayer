package com.puxin.webplayer.utils

import android.app.Activity

/**
 * Activity控制器
 * 全局控制所有Activity
 * */
object ActivityController {
    private val activities = ArrayList<Activity>();

    fun addActivity(activity: Activity) {
        activities.add(activity)
    }

    fun removeActivity(activity: Activity) {
        activities.remove(activity)
    }

    fun finishAll() {
        for (activity in activities) {
            if (!activity.isFinishing) {
                activity.finish()
            }
        }
        activities.clear()
        android.os.Process.killProcess(android.os.Process.myPid())  //强行杀死当前进程
    }
}