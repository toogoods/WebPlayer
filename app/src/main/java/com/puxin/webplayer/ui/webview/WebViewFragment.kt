package com.puxin.webplayer.ui.webview

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.*
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.puxin.webplayer.R
import com.puxin.webplayer.data.WebViewInfo
import com.puxin.webplayer.utils.BaseFragment
import com.puxin.webplayer.utils.LogUtil
import kotlinx.android.synthetic.main.fragment_webview.*
import java.lang.ref.WeakReference

class WebViewFragment: BaseFragment() {
    private val TAG = "WebView"

    /************************ listener ************************/

    private lateinit var listener: WebViewCallback
    /************************ listener ************************/

    /************************ handler ************************/


    /**
     * 播放器相关处理
     * */
    open class PlayerHandler(looper: Looper, fragment: WebViewFragment): Handler() {
        private val reference = WeakReference<Fragment>(fragment)

        override fun handleMessage(msg: Message) {
            val fragment = reference.get() as WebViewFragment
            when(msg.what) {
                1 -> {
                    fragment.webView.setBackgroundColor(0)
                    fragment.webView.setBackgroundResource(0)
                }
            }
        }
    }

    val playerHandler = PlayerHandler(Looper.getMainLooper(), this)

    /************************ handler ************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(activity is WebViewCallback) {
            listener = activity as WebViewCallback
        } else {
            throw IllegalStateException("activity must implements WebViewCallback")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val webSettings = webView?.settings
        webView.setBackgroundColor(ContextCompat.getColor(context!!, android.R.color.transparent))
        webView.setBackgroundResource(R.drawable.index_imgbg)
        webSettings?.javaScriptEnabled = true
        webSettings?.domStorageEnabled = true
        webSettings?.blockNetworkImage = false
        webSettings?.setAppCacheEnabled(true)
        webSettings?.cacheMode = WebSettings.LOAD_DEFAULT
        //webView.requestFocus()
        webSettings?.useWideViewPort = true
        webSettings?.loadWithOverviewMode = true
        webSettings?.allowFileAccess = true
        webView.webChromeClient = WebChromeClient()

        //回调接口
        webView.webViewClient = object: WebViewClient() {
            //网页不可用回调
            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                LogUtil.e(TAG, error.toString())
            }

            //网页加载完成回调
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                LogUtil.d(TAG, url!!)

                webView.visibility = View.VISIBLE
                webView.requestFocus()
            }

            //重写后可以使用浏览器中的按钮
            override fun shouldOverrideKeyEvent(view: WebView?, event: KeyEvent?): Boolean {
                if (event?.keyCode == KeyEvent.KEYCODE_MENU) {
                    return true
                }
                return super.shouldOverrideKeyEvent(view, event)
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                LogUtil.d(TAG, "开始加载网页")
            }
        }

        //网页加载
        webView.loadUrl(WebViewInfo.webUrl)

        //设置渲染模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        } else {
            webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        webSettings?.allowUniversalAccessFromFileURLs = true         //允许本地html跨域请求

        webView.addJavascriptInterface(Js(), "android")
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
    }

    /************************ init ************************/

    /************************ WebView JS ************************/

    inner class Js {
        @JavascriptInterface
        fun setVid(episodeData: String, position: Int) {

            //设置WebView透明
            playerHandler.sendMessage(Message().apply {
                what = 1
            })

            listener.startPlayer(episodeData, position)

        }

        @JavascriptInterface
        fun play(i: Int) {
            LogUtil.d(TAG, "position: $i")
            listener.play(i)
        }

        @JavascriptInterface
        fun logUpload(type: Int, content: String) {

        }

        @JavascriptInterface
        fun setOrderImageVisible() {
            playerHandler.sendMessage(playerHandler.obtainMessage(3))
        }

    }

    /************************ WebView JS ************************/



    /**
     * 按键操作
     * */
    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        LogUtil.d(TAG, keyCode.toString())

        return if (keyCode == KeyEvent.KEYCODE_BACK) {
            var ret = true
            webView.evaluateJavascript("javascript:myKeyBackEvent()") { p0 ->
                //处理myKeyBackEvent返回结果
                if (p0 == "\"finish\"") {
                    ret = false
                }
            }
            ret
        } else true
    }
}