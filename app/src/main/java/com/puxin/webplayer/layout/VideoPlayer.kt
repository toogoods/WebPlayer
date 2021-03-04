package com.puxin.webplayer.layout

import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.net.Uri
import android.util.AttributeSet
import android.view.Gravity
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.FrameLayout
import tv.danmaku.ijk.media.player.IMediaPlayer
import tv.danmaku.ijk.media.player.IjkMediaPlayer

/**
 * ijkplayer
 */
class VideoPlayer(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {

    //多构造函数, 解决 inflating Error
    constructor(context: Context): this(context, null)
    constructor(context: Context, attr: AttributeSet?) :this(context, attr, 0)

    //播放器容器
    private val mSurfaceView = SurfaceView(context)
    private var mMediaPlayer: IMediaPlayer? = null

    /**
     * 播放地址, 头信息
     */
    var path: String? = null
    private var header: Map<String, String>? = null

    /**
     * 播放器监听回调
     * */
    var listener: VideoListener? = null

    /**
     * 音频管理
     * */
    private val mAudioManager by lazy {
        context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
    private val mAudioFocusHelper by lazy {
        AudioFocusHelper()
    }

    init {
        setBackgroundColor(Color.BLACK)
        createSurfaceView()
    }

    //硬件解码
    var mEnableMediaCodec = false

    private fun createSurfaceView() {
        mSurfaceView.holder.addCallback(object: SurfaceHolder.Callback {
            override fun surfaceCreated(p0: SurfaceHolder?) {

            }

            override fun surfaceChanged(p0: SurfaceHolder?, p1: Int, p2: Int, p3: Int) {
                mMediaPlayer?.setDisplay(p0)
            }

            override fun surfaceDestroyed(p0: SurfaceHolder?) {

            }
        })

        val layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, Gravity.CENTER)
        addView(mSurfaceView, 0, layoutParams)
    }

    /**
     * 创建播放器
     * 配置相关参数
     * */
    private fun createPlayer(): IMediaPlayer {
        val ijkMediaPlayer = IjkMediaPlayer()
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "opensles", 1)
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "overlay-format", IjkMediaPlayer.SDL_FCC_RV32.toLong())
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "framedrop", 1)
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "start-on-prepared", 0)

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "http-detect-range-support", 1)

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "skip_loop_filter", 48)
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_CODEC, "min-frames", 100)
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "enable-accurate-seek", 1)
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_FORMAT, "dns_cache_clear", 1)      //清理DNS缓存

        ijkMediaPlayer.setVolume(1.0f, 1.0f)

        setEnableMediaCodec(ijkMediaPlayer, mEnableMediaCodec)
        return ijkMediaPlayer
    }

    //设置是否开启硬件解码
    private fun setEnableMediaCodec(ijkMediaPlayer: IjkMediaPlayer, isEnable: Boolean) {
        val value = if(isEnable) 1 else 0


        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec", value.toLong()) //开启硬解码

        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-auto-rotate", value.toLong())
        ijkMediaPlayer.setOption(IjkMediaPlayer.OPT_CATEGORY_PLAYER, "mediacodec-handle-resolution-change", value.toLong())
    }

    //开始加载视频
    fun load() {
        mMediaPlayer?.stop()
        mMediaPlayer?.release()

        mMediaPlayer = createPlayer()
        setListener(mMediaPlayer!!)
        mMediaPlayer?.setDisplay(mSurfaceView.holder)
        mMediaPlayer?.setDataSource(context, Uri.parse(path), header)
        mMediaPlayer?.prepareAsync()
    }

    fun start() {
        mMediaPlayer?.start()
        mAudioFocusHelper.requestFocus()
    }

    fun release() {
        mMediaPlayer?.reset()
        mMediaPlayer?.release()
        mMediaPlayer = null
        mAudioFocusHelper.abandonFocus()
    }

    fun pause() {
        mMediaPlayer?.pause()
        mAudioFocusHelper.abandonFocus()
    }

    fun stop() {
        mMediaPlayer?.stop()
        mAudioFocusHelper.abandonFocus()
    }

    fun reset() {
        mMediaPlayer?.reset()
        mAudioFocusHelper.abandonFocus()
    }

    fun getDuration() = mMediaPlayer?.duration ?: 0

    fun getCurrentPosition() = mMediaPlayer?.currentPosition ?: 0

    /**
     * 协程运行
     */
    suspend fun seekTo(l: Long) {
        mMediaPlayer?.seekTo(l)
    }

    fun isPlaying() = mMediaPlayer?.isPlaying ?: false

    //设置播放器监听
    private fun setListener(player: IMediaPlayer) {
        player.setOnPreparedListener {
            listener?.onPrepared(it)
        }

        player.setOnInfoListener { iMediaPlayer, i, i2 ->
            listener?.onInfo(iMediaPlayer, i, i2)
            true
        }

        player.setOnBufferingUpdateListener { iMediaPlayer, i ->
            listener?.onBufferingUpdate(iMediaPlayer, i)
        }

        player.setOnCompletionListener {
            listener?.onCompletion(it)
        }

        player.setOnVideoSizeChangedListener { iMediaPlayer, i, i2, i3, i4 ->
            listener?.onVideoSizeChanged(iMediaPlayer, i, i2, i3, i4)
        }

        player.setOnSeekCompleteListener {
            listener?.onSeekComplete(it)
        }
    }

    //音频监听回调
    @Suppress("DEPRECATION")
    inner class AudioFocusHelper: AudioManager.OnAudioFocusChangeListener {
        private var startRequest = false
        private var pausedForLoss = false
        private var currentFocus = 0
        override fun onAudioFocusChange(p0: Int) {
            if (currentFocus == p0) return
            currentFocus = p0
            when(p0) {
                AudioManager.AUDIOFOCUS_GAIN, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> {
                    //获得焦点, 暂时获得焦点
                    if (startRequest || pausedForLoss) {
                        start()
                        startRequest = false
                        pausedForLoss = false
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    //失去焦点, 暂时失去焦点
                    if(isPlaying()) {
                        pausedForLoss = true
                        pause()
                    }
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    //此时需降低音量
                    if (isPlaying()) {
                        mMediaPlayer?.setVolume(0.1f, 0.1f)
                    }
                }
            }
        }

        fun requestFocus(): Boolean {
            if (currentFocus == AudioManager.AUDIOFOCUS_GAIN) {
                return true
            }

            val status = mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN)
            if (status == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                currentFocus = AudioManager.AUDIOFOCUS_GAIN
                return true
            }

            startRequest = true
            return false
        }

        fun abandonFocus() = mAudioManager.let {
            startRequest = false
            AudioManager.AUDIOFOCUS_REQUEST_GRANTED == it.abandonAudioFocus(this)
        }
    }
}