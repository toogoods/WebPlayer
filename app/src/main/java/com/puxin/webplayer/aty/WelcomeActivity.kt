package com.puxin.webplayer.aty

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.os.Bundle
import com.puxin.webplayer.R
import com.puxin.webplayer.utils.*
import kotlinx.android.synthetic.main.activity_welcome.*
import rx.Observable
import rx.android.schedulers.AndroidSchedulers
import java.util.concurrent.TimeUnit

/**
 * 欢迎页
 * 动画播放
 * */
class WelcomeActivity : BaseActivity() {

    private var redirect: String? = null
    private var searchParam: String? = null

    private val DELAY_TIME: Int = 3000
    private val ANIM_TIME: Int = 1000

    private val SCALE_END: Double = 1.15

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        ActivityController.addActivity(this)

        //获取外部推荐位参数
        redirect = intent.getStringExtra("redirect")
        searchParam = intent.getStringExtra("searchParam")
    }

    override fun onResume() {
        super.onResume()
        startMainActivity()
    }

    fun startMainActivity() {
        Observable.timer(DELAY_TIME.toLong(), TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                startAnim()
            }
    }

    private fun startAnim() {
        val animatorX = ObjectAnimator.ofFloat(this.welcomeActivity, "scaleX", 1f, SCALE_END.toFloat())
        val animatorY = ObjectAnimator.ofFloat(this.welcomeActivity, "scaleY", 1f, SCALE_END.toFloat())
        val set = AnimatorSet()
        set.setDuration(ANIM_TIME.toLong()).play(animatorX).with(animatorY)

        set.start()

        set.addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                super.onAnimationEnd(animation)
                MainActivity.startActivity(this@WelcomeActivity, redirect, searchParam)
                this@WelcomeActivity.finish()
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        ActivityController.removeActivity(this)
    }

}