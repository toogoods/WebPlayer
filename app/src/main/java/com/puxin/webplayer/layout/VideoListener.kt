package com.puxin.webplayer.layout

import tv.danmaku.ijk.media.player.IMediaPlayer

interface VideoListener: IMediaPlayer.OnBufferingUpdateListener, IMediaPlayer.OnCompletionListener,
    IMediaPlayer.OnPreparedListener, IMediaPlayer.OnInfoListener,
    IMediaPlayer.OnVideoSizeChangedListener,
    IMediaPlayer.OnErrorListener, IMediaPlayer.OnSeekCompleteListener {
}