package com.puxin.webplayer.ui.videoplayer.recycler

interface PrvInterface {
    fun setOnLoadMoreComplete()

    fun setPageableListener(pageableListener: MenuRecyclerView.PageableListener)
}