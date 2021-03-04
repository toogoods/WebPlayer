package com.puxin.webplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.widget.Toast
import com.puxin.webplayer.MyApplication
import com.puxin.webplayer.R
import kotlinx.android.synthetic.main.custom_toast_two.view.*

/**
 * String扩展函数
 * 一行 或 两行 用户提示
 */
fun String.toast(title: String? = null, length: Int = Toast.LENGTH_SHORT) {
    CustomToast.makeText(this, length, title).show()
}

fun String.toast(length: Int) {
    CustomToast.makeText(this, length, null).show()
}

/**
 * Int 扩展函数
 * 图片资源类型 用户提示
 * */
fun Int.toast(length: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(MyApplication.context, this, length).show()
}

/**
 * 自定义Toast 单例类
 * */
object CustomToast {
    private val customToast = CustomToast
    private lateinit var toast: Toast

    @SuppressLint("InflateParams")
    fun makeText(text: CharSequence, duration: Int, title: CharSequence?): CustomToast {
        val inflater = MyApplication.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = if (title != null) {
            inflater.inflate(R.layout.custom_toast_two, null)
        } else {
            inflater.inflate(R.layout.custom_toast_one, null)
        }

        if (title != null) {
            view.titleContent.text = text
            view.tvToastContent.text = text
        } else {
            view.tvToastContent.text = text
        }

        customToast.toast = Toast(MyApplication.context)
        customToast.toast.view = view
        customToast.toast.duration = duration

        return customToast
    }

    fun show() {
        toast.show()
    }

    fun setGravity(gravity: Int, xOffset: Int, yOffset: Int) {
        toast.setGravity(gravity, xOffset, yOffset)
    }
}