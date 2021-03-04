package com.puxin.webplayer.aty

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.util.DisplayMetrics
import android.view.KeyEvent
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.puxin.webplayer.R
import com.puxin.webplayer.layout.ZOrderFrameLayout
import com.puxin.webplayer.logic.model.Data
import com.puxin.webplayer.ui.videoplayer.PlayerCallback
import com.puxin.webplayer.ui.videoplayer.PlayerFragment
import com.puxin.webplayer.ui.webview.WebViewCallback
import com.puxin.webplayer.ui.webview.WebViewFragment
import com.puxin.webplayer.utils.BaseActivity
import com.puxin.webplayer.utils.LogUtil
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : BaseActivity(), WebViewCallback, PlayerCallback {

    /************************ static parameter ************************/

    companion object {
        fun startActivity(context: Context, redirect: String?, searchParam: String?) {
            val intent = Intent(context, MainActivity::class.java)
            intent.putExtra("redirect", redirect)
            intent.putExtra("searchParam", searchParam)
            context.startActivity(intent)
        }

        var data: Data? = null
    }

    /************************ static parameter ************************/

    /************************ parameter ************************/

    private lateinit var webViewFragment: WebViewFragment
    private var playerFragment: PlayerFragment? = null

    var type: String? = null        //播放器窗口类型 "smallWindow" 小窗 "fullScreen" 全屏

    //UI message
    private val SMALLWINDOW = 1
    private val FULLSCREEN = 2

    /************************ parameter ************************/

    /************************ handler ************************/

    private val timerHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                1 -> {
                    //延迟处理切换到小窗时, PlayerFragment和WebViewFragment的切换
                    val playerLayoutParams = playerLayout.layoutParams as ZOrderFrameLayout.Companion.LayoutParams
                    playerLayoutParams.zOrder = 1
                    playerLayout.layoutParams = playerLayoutParams
                    mainLayout.refreshLayout()
                }
            }
        }
    }

    private val UIHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                SMALLWINDOW -> {
                    playerFragment?.setSmallWindow()
                    timerHandler.sendMessageDelayed(Message().apply { what = 1 }, 1000)
                }
                FULLSCREEN -> {
                    val playerLayoutParams = playerLayout.layoutParams as ZOrderFrameLayout.Companion.LayoutParams
                    playerLayoutParams.zOrder = 2
                    playerLayout.layoutParams = playerLayoutParams
                    mainLayout.refreshLayout()
                    playerFragment?.fullScreen(msg.arg1)
                }
            }
        }
    }

    /************************ handler ************************/

    /************************ init ************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webViewFragment = WebViewFragment()
        addFragment(R.id.frameLayout, webViewFragment)
    }

    /************************ init ************************/

    /**
     * 按键操作
     * */
    override fun onKeyDown(keyCode: Int, event: KeyEvent) = when(keyCode) {
        KeyEvent.KEYCODE_BACK -> {
            LogUtil.d("MainActivity", "$keyCode")
            if (type != null) {
                if (type == "smallWindow") {
                    //小屏播放返回键
                    playerFragment?.let {
                        it.releasePlayer()
                        removeFragment(it)
                    }
                    playerFragment = null
                    val ret = webViewFragment.onKeyDown(keyCode, event)
                    if (!ret) {
                        finish()
                    }
                    ret || super.onKeyDown(keyCode, event)
                } else {
                    //大屏播放返回键
                    UIHandler.sendMessage(Message().apply { what = SMALLWINDOW })
                    type = "smallWindow"
                    super.onKeyDown(keyCode, event)
                }
            } else {
                val ret = webViewFragment.onKeyDown(keyCode, event)
                if (!ret) {
                    finish()
                }
                ret || super.onKeyDown(keyCode, event)
            }
        }
        else -> {
            LogUtil.d("MainActivity", "$keyCode")
            super.onKeyDown(keyCode, event)
        }
    }

    /**
     * 播放器按键操作
     * */
    override fun dispatchKeyEvent(event: KeyEvent?): Boolean {
        return if (type != null && type == "fullScreen") {
            playerFragment?.dispatchKeyEvent(event) ?: super.dispatchKeyEvent(event)
        } else {
            super.dispatchKeyEvent(event)
        }
    }

    /************************ fragment callback ************************/

    /**
     * WebView 播放回调
     * */
    override fun startPlayer(data: String, position: Int) {
        MainActivity.data = Gson().fromJson(data, Data::class.java)

        //小窗播放
        type = "smallWindow"

        //开启播放
        val bundle = Bundle()
        //bundle.putString("url", MainActivity.data?.episodes?.get(position)?.play_url)

        bundle.putString("url", "http://gslbserv.itv.cmvideo.cn/4232.ts?channel-id=puxinsp&Contentid=${MainActivity.data?.episodes?.get(position)?.play_url}&authCode=3a&stbId=003503FF00010060000100E400F53140&usergroup=g29097100000&userToken=3742ec2d15baf405f2e2924134657b3f06wx")
        bundle.putInt("num", position + 1)
        bundle.putInt("windowWidth", getWindowSize("width"))
        bundle.putInt("windowHeight", getWindowSize("height"))
        bundle.putString("type", type)

        playerFragment = PlayerFragment()
        playerFragment?.arguments = bundle
        addFragment(R.id.playerLayout, playerFragment!!)
    }

    override fun play(position: Int) {

        UIHandler.sendMessage(Message().apply {
            what = FULLSCREEN
            arg1 = position
        })

        /*val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.hide(webViewFragment)
        ft.commit()*/
    }

    override fun changeType(type: String) {
        this.type = type
    }

    /************************ fragment callback ************************/

    /************************ util ************************/

    //添加Fragment
    private fun addFragment(layout: Int, fragment: Fragment) {

        //第一步 获取 fragment manager
        val fm = supportFragmentManager

        //第二步 获取 fragment transaction
        val ft = fm.beginTransaction()

        //第三步 动态添加 fragment
        ft.add(layout, fragment)
        ft.commit()
    }

    private fun removeFragment(fragment: Fragment) {
        val fm = supportFragmentManager
        val ft = fm.beginTransaction()
        ft.remove(fragment)
        ft.commit()
    }

    /**
     * 获取屏幕宽高
     * */
    private fun getWindowSize(type: String) = when (type){
        "width" -> {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            metrics.widthPixels
        }
        "height" -> {
            val metrics = DisplayMetrics()
            windowManager.defaultDisplay.getMetrics(metrics)
            metrics.heightPixels
        }
        else -> 0
    }

    /************************ util ************************/
}