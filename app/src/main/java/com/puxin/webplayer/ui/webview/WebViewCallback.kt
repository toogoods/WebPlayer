package com.puxin.webplayer.ui.webview

interface WebViewCallback {
    abstract fun startPlayer(data: String, position: Int)
    abstract fun play(position: Int)
}