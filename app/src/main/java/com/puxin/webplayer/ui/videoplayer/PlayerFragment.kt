package com.puxin.webplayer.ui.videoplayer

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.puxin.webplayer.MyApplication
import com.puxin.webplayer.R
import com.puxin.webplayer.anim.RectData
import com.puxin.webplayer.anim.RectEvaluator
import com.puxin.webplayer.aty.MainActivity
import com.puxin.webplayer.layout.VideoListener
import com.puxin.webplayer.ui.videoplayer.recycler.FocusLinearLayoutManager
import com.puxin.webplayer.ui.videoplayer.recycler.MenuRecyclerView
import com.puxin.webplayer.ui.videoplayer.recycler.VideoAdapter
import com.puxin.webplayer.ui.webview.WebViewCallback
import com.puxin.webplayer.utils.BaseFragment
import com.puxin.webplayer.utils.LogUtil
import com.puxin.webplayer.utils.selfReference
import com.puxin.webplayer.utils.toast
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.android.synthetic.main.video_button.*
import kotlinx.android.synthetic.main.video_info.*
import kotlinx.coroutines.async
import tv.danmaku.ijk.media.player.IMediaPlayer
import java.io.IOException
import java.util.*

class PlayerFragment: BaseFragment() {

    /************************ parameter ************************/

    private val TAG = "PlayerFragment"

    private var url: String? = null     //播放串参数
    private var num = 0                 //播放集数
    private var recordSeekTime = 0      //历史播放时间

    private val handlerList = ArrayList<Handler>()  //存储handlers

    //声明时间参数变量, 减少内存开辟工作
    private var totalSeconds = 0
    private var second = 0
    private var minute = 0
    private var hour = 0
    private var formatBuilder = StringBuilder()
    private val formatter by lazy {
        Formatter(formatBuilder, Locale.getDefault())
    }

    /**
     * 播放器窗口管理
     * */
    private var windowType: String? = null
    private var windowWidth: Int = 0
    private var windowHeight: Int = 0

    private val SMALLWINDOW = 1
    private val FULLSCREEN = 2

    /**
     * 播放器切换参数
     * */
    private val ERROR = -1      //播放地址获取失败操作
    private val BACK = 100      //播放结束返回操作
    private val FORWARD = 200      //切换集数操作

    /**
     * 进度条参数
     * */
    private var timeCount = 0   //进度条隐藏 时间计数
    private var seekTime: Long = 0    //进度条时间
    private var longClick = 0   //长按统计

    /**
     * menu adapter
     * */
    private lateinit var videoAdapter: VideoAdapter

    /**
     * PlayerCallback
     * */
    private lateinit var listener: PlayerCallback

    /************************ parameter ************************/

    /************************ handler ************************/

    /**
     * 大小屏切换, UI更新操作
     * */
    private val uiHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                SMALLWINDOW -> {
                    //smallWindow
                    playerLayout.setBackgroundColor(Color.BLACK)
                    val anim = ObjectAnimator.ofObject(playerLayout, "rectData",
                            RectEvaluator(),
                            RectData(0f, 0f,  windowWidth.toFloat(), windowHeight.toFloat()),
                            RectData(150f, 90f, 404f, 228f))
                    anim.duration = 1000
                    anim.start()
                }
                FULLSCREEN -> {
                    //fullScreen
                    playerLayout.setBackgroundColor(Color.BLACK)
                    val anim = ObjectAnimator.ofObject(playerLayout, "rectData",
                            RectEvaluator(),
                            RectData(150f, 90f, 404f, 228f),
                            RectData(0f, 0f,  windowWidth.toFloat(), windowHeight.toFloat()))
                    anim.duration = 1000
                    anim.start()
                }
            }
        }
    }

    /**
     * 播放, 暂停图标延迟隐藏
     * */
    private val visibleHandler = object: Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            pauseImage.visibility = View.GONE
            playImage.visibility = View.GONE
        }
    }

    /**
     * 切换集数
     * */
    private val playerHandler = object: Handler(Looper.getMainLooper()) {
        @SuppressLint("SetTextI18n")
        override fun handleMessage(msg: Message) {
            when(msg.what) {
                ERROR -> {
                    video.pause()
                    video.stop()
                    "请稍后重试".toast("播放失败")
                }
                BACK -> {
                    activity?.onBackPressed()
                }
                FORWARD -> {
                    //发送播放记录
                    //Repository.records("", )

                    //设置播放参数
                    video.path = url
                    video.load()
                    menu.visibility = View.GONE
                    "即将播放第${num}集".toast()

                    //显示网速
                    charProgress.visibility = View.VISIBLE
                    netSpeed.visibility = View.VISIBLE

                    //修改标题
                    videoInfoText.text = "${MainActivity.data?.title}第${num}集"
                }
            }
        }
    }

    /**
     * 进度条 时间更新
     * */
    private val timeHandler = Handler(Looper.getMainLooper())
    private val runnable: Runnable = selfReference {
        Runnable {
            setTimeProgress()
            if (videoButton.visibility == View.VISIBLE ) {
                timeCount++
                if (timeCount > 5 && video.isPlaying()) {
                    videoButton.visibility = View.GONE
                    videoInfo.visibility = View.GONE
                    timeCount = 0
                }
            }
            timeHandler.postDelayed(self, 1000)
        }
    }

    /************************ handler ************************/

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(activity is WebViewCallback) {
            listener = activity as PlayerCallback
        } else {
            throw IllegalStateException("activity must implements WebViewCallback")
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val bundle = this.arguments
        url = bundle?.getString("url")
        num = bundle?.getInt("num") ?: 0
        windowType = bundle?.getString("type") ?: ""
        windowWidth = bundle?.getInt("windowWidth") ?: 0
        windowHeight = bundle?.getInt("windowHeight") ?: 0

        return inflater.inflate(R.layout.fragment_video, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        handlerList.add(visibleHandler)
        handlerList.add(playerHandler)
        handlerList.add(timeHandler)
    }

    @SuppressLint("SetTextI18n")
    override fun onStart() {
        super.onStart()

        if (windowType == "smallWindow") {
            setSmallWindow()
        }

        showRecyclerViewLinearLayout()
        setListener()

        videoInfoText.text = "${MainActivity.data?.title}第${num}集"
        video?.path = url
        try {
            video.load()
        } catch (e: IOException) {
            "播放失败".toast()
            e.printStackTrace()
        }
    }

    /**
     * 播放器回调
     * */
    private fun setListener() {
        video?.listener = object: VideoListener {
            override fun onBufferingUpdate(p0: IMediaPlayer?, p1: Int) {
                //缓冲进度条修改
                seekBar?.secondaryProgress  = p1
            }

            override fun onCompletion(p0: IMediaPlayer?) {
                try {

                    if (MainActivity.data?.episodes?.size!! <= num) {
                        //播放即将结束
                        playerHandler.sendMessageDelayed(playerHandler.obtainMessage(BACK), 1000)
                        "播放即将结束".toast()
                    } else {
                        setPlayUrl(num)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            @Suppress("DeferredResultUnused")
            override fun onPrepared(p0: IMediaPlayer?) {
                //初始化时间参数
                initialTimeParams()
                video.start()
                charProgress.visibility = View.GONE
                netSpeed.visibility = View.GONE
                if (recordSeekTime != 0) {
                    lifecycleScope.async {
                        video.seekTo((recordSeekTime * 1000).toLong())
                    }
                }
                timeDuration.text = stringForTime(video.getDuration().toInt())
                seekBar.max = video.getDuration().toInt()

                timeHandler.postDelayed(runnable, 2000)
            }

            override fun onInfo(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
                when(p1) {
                    MediaPlayer.MEDIA_INFO_BUFFERING_START -> {
                        charProgress.visibility = View.VISIBLE
                        netSpeed.visibility = View.VISIBLE
                    }
                    MediaPlayer.MEDIA_INFO_BUFFERING_END -> {
                        if (p0?.isPlaying!!) {
                            charProgress.visibility = View.GONE
                            netSpeed.visibility = View.GONE
                        }
                    }
                }
                return false
            }

            override fun onVideoSizeChanged(p0: IMediaPlayer?, p1: Int, p2: Int, p3: Int, p4: Int) {

            }

            override fun onError(p0: IMediaPlayer?, p1: Int, p2: Int): Boolean {
                return false
            }

            override fun onSeekComplete(p0: IMediaPlayer?) {
                p0?.start()
            }

        }
    }

    /**
     * menu处理
     * */
    private fun showRecyclerViewLinearLayout() {
        val linearLayoutManager = FocusLinearLayoutManager(context!!) as LinearLayoutManager
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL
        menu?.layoutManager = linearLayoutManager
        videoAdapter = VideoAdapter(MainActivity.data?.episodes!!)
        menu?.adapter = videoAdapter
        menu?.itemClickListener = object: MenuRecyclerView.OnItemClickListener {
            override fun onItemClick(parent: MenuRecyclerView, view: View, position: Int) {
                setPlayUrl(position)
            }
        }
        menu?.itemLongClickListener = object: MenuRecyclerView.OnItemLongClickListener {
            override fun onItemLongClick(parent: MenuRecyclerView, view: View, position: Int) {
                setPlayUrl(position)
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        return true
    }

    /************************ init ************************/

    /************************ util ************************/
    //设置播放地址
    private fun setPlayUrl(position: Int) {
        url = MainActivity.data?.episodes?.get(position)?.play_url?.let { getPlayUrl(it) } ?: ""
        if (url != "") {
            LogUtil.d(TAG, "play_url:$url")
            num = position + 1
            recordSeekTime = 0
            playerHandler.sendMessage(playerHandler.obtainMessage(FORWARD))
        }

    }

    //初始化时间参数
    private fun initialTimeParams() {
        totalSeconds = 0
        second = 0
        minute = 0
        hour = 0
        recordSeekTime = 0
    }

    //进度条时间刷新
    private fun setTimeProgress() {
        val position = video.getCurrentPosition()
        seekTime = position
        val duration = video.getDuration()
        if (duration > 0) {
            seekBar?.progress = position.toInt()
        }
        timeCurrent.text = stringForTime(position.toInt())
    }

    //时间转换
    private fun stringForTime(timeMs: Int): String {
        totalSeconds = timeMs / 1000
        second = totalSeconds % 60
        minute = (totalSeconds / 60) % 60
        hour = totalSeconds / 3600
        formatBuilder.setLength(0)
        return if (hour > 0) {
            formatter.format("%d:%02d:%02d", hour, minute, second).toString()
        } else {
            formatter.format("%02d:%02d", minute, second).toString()
        }
    }

    //播放, 暂停动画
    private fun animation(view: ImageView) {
        view.visibility = View.VISIBLE
        val animation = AnimationUtils.loadAnimation(MyApplication.context, R.anim.alpha_anim)
        view.startAnimation(animation)
        visibleHandler.sendMessageDelayed(Message(), 3000)
    }

    //快进, 快退
    private fun seek(direction: Int) {
        menu.visibility = View.GONE
        videoButton.visibility = View.VISIBLE
        videoInfo.visibility = View.VISIBLE
        longClick++
        timeHandler.removeCallbacksAndMessages(null)
        timeCount = 0
        when(direction) {
            1 -> {
                //快退 left
                seekTime -= if (longClick > 3) 60000 else 10000
                seekTime = if (seekTime < 0) 1000 else seekTime
            }
            2 -> {
                //快进 right
                seekTime += if (longClick > 3) 60000 else 10000
                seekTime = if (seekTime > video.getDuration()) video.getDuration() - 3000 else seekTime
            }
        }
    }


    //简化play_url获取
    private fun getPlayUrl(url: String) = "http://gslbserv.itv.cmvideo.cn/4232.ts?channel-id=puxinsp&Contentid=${url}&authCode=3a&stbId=003503FF00010060000100E400F53140&usergroup=g29097100000&userToken=3742ec2d15baf405f2e2924134657b3f06wx"

    //切换小窗
    fun setSmallWindow() {
        windowType = "smallWindow"
        uiHandler.sendMessage(Message().apply { what = SMALLWINDOW })
    }

    //切换大屏
    fun fullScreen(position: Int? = -1) {

        if (position != num - 1) {
            //如果传递了不同集数的值, 则播放不同集数的节目
            setPlayUrl(position!!)
        }
        uiHandler.sendMessage(Message().apply { what = FULLSCREEN })
        windowType = "fullScreen"
        listener.changeType(windowType!!)
    }

    //Activity 小窗返回 释放播放器
    fun releasePlayer() {
        video?.stop()
        video?.release()
    }

    /************************ util ************************/

    /**
     * 按键操作
     * */
    @Suppress("DeferredResultUnused")
    fun dispatchKeyEvent(keyEvent: KeyEvent?) = keyEvent?.let { event ->
        val action = event.action
        val keyCode = event.keyCode
        return if (action == KeyEvent.ACTION_UP) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                lifecycleScope.async {
                    video.seekTo(seekTime)
                }
                longClick = 0
                timeHandler.postDelayed(runnable, 200)
            }
            true
        } else {
            when(keyCode) {
                KeyEvent.KEYCODE_BACK -> {
                    //大屏返回时, 不释放播放器
                    //返回键
                    if (menu.visibility == View.VISIBLE) {
                        menu.visibility = View.GONE
                    } else {
                        //发送播放记录
                        //Repository.records()
                        //释放播放器
                        if(windowType != "fullScreen") {
                            video?.stop()
                            video?.release()
                        }
                        //返回播放集数参数
                        activity?.onKeyDown(keyCode, event)
                    }
                }
                KeyEvent.KEYCODE_ENTER, KeyEvent.KEYCODE_DPAD_CENTER -> {
                    if (menu.visibility == View.VISIBLE) {
                        menu.dispatchKeyEvent(event)
                    } else {
                        videoButton.visibility = View.VISIBLE
                        videoInfo.visibility = View.VISIBLE

                        if (video.isPlaying()) {
                            //暂停播放
                            animation(pauseImage)
                            video.pause()
                        } else {
                            //继续播放
                            animation(playImage)
                            video.start()
                        }
                    }
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    seek(1)
                }
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    seek(2)
                }
                KeyEvent.KEYCODE_MENU -> {
                    if(menu.visibility == View.VISIBLE) {
                        menu.visibility = View.GONE
                    } else {
                        //显示菜单, 并滚动到对应集数
                        menu.visibility = View.VISIBLE
                        videoButton.visibility = View.GONE
                        videoInfo.visibility = View.GONE
                        menu.scrollToPosition(num -1)
                        videoAdapter.mPosition = num - 1
                        videoAdapter.notifyDataSetChanged()
                    }
                }
            }
            false
        }
    } ?: false

    override fun onDestroy() {
        super.onDestroy()
        video?.stop()
        video?.release()
        //统一删除
        for(handler in handlerList) {
            handler.removeCallbacksAndMessages(null)
        }
        handlerList.clear()
    }
}
