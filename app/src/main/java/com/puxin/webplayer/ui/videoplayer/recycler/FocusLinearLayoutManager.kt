package com.puxin.webplayer.ui.videoplayer.recycler

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager

open class FocusLinearLayoutManager(context: Context): LinearLayoutManager(context) {
    override fun onInterceptFocusSearch(focused: View, direction: Int): View? {
        var fromPos = getPosition(focused)  //当前焦点的位置
        when(direction) {
            View.FOCUS_DOWN -> fromPos++
            View.FOCUS_UP -> fromPos--
        }

        if (fromPos < 0 || fromPos >= itemCount) {
            //如果下一个位置 < 0, 或者超出item的总数, 则返回当前的View, 即焦点不动
            return focused
        } else {
            if(fromPos > findLastCompletelyVisibleItemPosition()) {
                //如果下一个位置大于最新的已显示的item, 即下一个位置的View没有显示, 则滑动到那个位置, 让他显示, 就可以获取焦点了
                scrollToPosition(fromPos)
            }
        }

        return super.onInterceptFocusSearch(focused, direction)

    }
}