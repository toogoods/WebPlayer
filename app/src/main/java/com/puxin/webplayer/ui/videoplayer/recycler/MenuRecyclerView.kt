package com.puxin.webplayer.ui.videoplayer.recycler

import android.content.Context
import android.graphics.Rect
import android.os.Build
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

class MenuRecyclerView(context: Context, attrs: AttributeSet?, defStyle: Int): RecyclerView(context, attrs, defStyle), PrvInterface {

    constructor(context: Context): this(context, null)
    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, -1)

    private val TAG = "Menu"

    private interface ItemListener: OnClickListener, OnLongClickListener, OnFocusChangeListener
    interface OnItemFocusListener {
        //onFocusChange, when lose Focus
        fun onItemPreSelected(parent: MenuRecyclerView, view: View, position: Int)
        //onFocusChange, when has Focus
        fun onItemSelected(parent: MenuRecyclerView, view: View, position: Int)
        //onScrollStateChanged, after scroll
        fun onReviseFocusFollow(parent: MenuRecyclerView, view: View, position: Int)
    }
    interface OnItemClickListener {
        fun onItemClick(parent: MenuRecyclerView, view: View, position: Int)
    }
    interface OnItemLongClickListener {
        fun onItemLongClick(parent: MenuRecyclerView, view: View, position: Int)
    }

    interface OnChildViewHolderSelectedListener {
        fun onChildViewHolderSelected(parent: RecyclerView, vh: RecyclerView.ViewHolder, position: Int)
    }

    private var mItemView: View? = null

    private var mItemListener: ItemListener
    var itemClickListener: OnItemClickListener? = null
    var itemFocusListener: OnItemFocusListener? = null
    var itemLongClickListener: OnItemLongClickListener? = null

    var mPageableListener: PageableListener? = null

    var isSelectedItemCenter = true     //选中项是否居中
    var selectedItemOffsetStart = 0     //开始偏移量
    var selectedItemOffsetEnd = 0       //结束偏移量

    private var offset = -1     //baseline

    init {
        mItemListener = object: ItemListener {
            override fun onClick(p0: View) {
                itemClickListener?.let {
                    try {
                        it.onItemClick(this@MenuRecyclerView, p0, getChildLayoutPosition(p0))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            override fun onLongClick(p0: View): Boolean {
                return itemLongClickListener?.let {
                    try {
                        it.onItemLongClick(this@MenuRecyclerView, p0, getChildLayoutPosition(p0))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    true
                } ?: false
            }


            override fun onFocusChange(p0: View?, p1: Boolean) {
                itemFocusListener?.let { itemFocusListener ->
                    mItemView = p0
                    p0?.isSelected = p1
                    p0?.let {
                        if (p1) {
                            itemFocusListener.onItemSelected(this@MenuRecyclerView, it, getChildLayoutPosition(it))
                        } else {
                            itemFocusListener.onItemPreSelected(this@MenuRecyclerView, it, getChildLayoutPosition(it))
                        }
                    }
                }
            }
        }
    }

    /**************** Override ****************/

    override fun hasFocus(): Boolean {
        return super.hasFocus()
    }

    override fun isInTouchMode() = if (Build.VERSION.SDK_INT == 19) !(hasFocus() && !super.isInTouchMode()) else super.isInTouchMode()

    override fun onChildAttachedToWindow(child: View) {
        if (!child.hasOnClickListeners()) {
            child.setOnClickListener(mItemListener)
            child.setOnLongClickListener(mItemListener)
        }
        if (child.onFocusChangeListener == null) {
            child.onFocusChangeListener = mItemListener
        }
    }

    override fun onFocusChanged(gainFocus: Boolean, direction: Int, previouslyFocusedRect: Rect?) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect)
    }

    override fun requestChildFocus(child: View?, focused: View?) {
        child?.let {
            if(isSelectedItemCenter) {
                selectedItemOffsetStart = if(layoutDirection()) getFreeWidth() - child.width else getFreeHeight() - child.height
                selectedItemOffsetStart /= 2
                selectedItemOffsetEnd = selectedItemOffsetStart
            }
        }
        super.requestChildFocus(child, focused)
    }

    override fun requestChildRectangleOnScreen(child: View, rect: Rect, immediate: Boolean): Boolean {
        val parentRight = width - paddingRight
        val parentBottom = height - paddingBottom

        val childLeft = child.left + rect.left
        val childTop = child.top + rect.top

        val childRight = childLeft + rect.width()
        val childBottom = childTop + rect.height()

        val offScreenLeft = 0.coerceAtMost(childLeft - paddingLeft - selectedItemOffsetStart)
        val offScreenTop = 0.coerceAtMost(childTop - paddingTop - selectedItemOffsetStart)
        val offScreenRight = 0.coerceAtLeast(childRight - parentRight + selectedItemOffsetEnd)
        val offScreenBottom = 0.coerceAtLeast(childBottom - parentBottom + selectedItemOffsetEnd)

        val dx = if (layoutManager!!.canScrollHorizontally()) {
            if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
                if (offScreenRight != 0) offScreenRight else max(offScreenLeft, childRight - parentRight)
            } else {
                if (offScreenLeft != 0) offScreenLeft else min(childLeft - paddingLeft, offScreenRight)
            }
        } else 0

        val dy = if (layoutManager!!.canScrollVertically()) {
            if( offScreenTop != 0) offScreenTop else min(childTop - paddingTop, offScreenBottom)
        } else 0

        offset = if (layoutDirection()) dx else dy
        if (dx != 0 || dy != 0) {
            if (immediate) {
                scrollBy(dx, dy)
            } else {
                smoothScrollBy(dx, dy)
            }
            return true
        }

        postInvalidate()
        return false
    }

    override fun getChildDrawingOrder(childCount: Int, i: Int): Int {
        val view = focusedChild
        var position = getChildAdapterPosition(view) - getFirstItemPosition()
        return if (position < 0) {
            i
        } else {
            when {
                i == childCount - 1 -> {
                    if (position > i) { position = i }
                    position
                }
                position == i -> {
                    childCount - 1
                }
                else -> i
            }
        }

    }

    /**
     * Scroll 状态改变回调
     * SCROLL_STATE_IDLE: 滚动完成, 空闲状态
     * SCROLL_STATE_DRAGGING: 正在拖动状态
     * SCROLL_STATE_SETTLING: 正在滚动状态
     * */
    override fun onScrollStateChanged(state: Int) {

        when(state) {
            SCROLL_STATE_IDLE -> {
                offset = -1
                val focus = focusedChild
                if (itemFocusListener != null && focus != null) {
                    itemFocusListener!!.onReviseFocusFollow(this, focus, getChildLayoutPosition(focus))
                }
            }
        }
        super.onScrollStateChanged(state)
    }

    override fun getBaseline() = offset
    override fun onInterceptTouchEvent(e: MotionEvent?): Boolean { return super.onInterceptTouchEvent(e) }

    /**************** Override ****************/

    /**************** KeyEvent ****************/

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val action = event.action
        val keyCode = event.keyCode
        if (action == KeyEvent.ACTION_UP) {
            if (!layoutDirection() && keyCode == KeyEvent.KEYCODE_DPAD_DOWN ) {
                forwardItem()
            } else if (layoutDirection() && keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                forwardItem()
            }
        }
        return super.dispatchKeyEvent(event)
    }

    /**************** KeyEvent ****************/

    /**************** loading ****************/

    private var isLoading = false

    interface PageableListener {
        fun onLoadMoreItem()
    }

    override fun setOnLoadMoreComplete() { isLoading = false }
    override fun setPageableListener(pageableListener: PageableListener) {
        this.mPageableListener = pageableListener
    }

    /**************** loading ****************/

    /**************** util ****************/

    /**
     * 判断布局方向
     * true 水平布局
     * false 垂直布局
     * */
    private fun layoutDirection() = layoutManager?.let {
        when(it) {
            it as LinearLayoutManager -> {
                LinearLayoutManager.HORIZONTAL == it.orientation
            }
            it as GridLayoutManager -> {
                GridLayoutManager.HORIZONTAL == it.orientation
            }
            else -> false
        }
    } ?:false

    /**
     * 向下一个Item移动
     * */
    private fun forwardItem(): Boolean {
        val totalItemCount = layoutManager!!.itemCount
        val visibleItemCount = childCount
        val firstVisibleItem = findFirstVisibleItemPosition()

        //判断是否为最底部
        if (!isLoading && (totalItemCount - visibleItemCount) <= firstVisibleItem) {
            isLoading = true
            if (mPageableListener != null) {
                mPageableListener!!.onLoadMoreItem()
                return true
            }
        }
        return false
    }

    /**
     * 获取第一个Item的Position
     * */
    private fun getFirstItemPosition() = if (childCount != 0) getChildLayoutPosition(getChildAt(0)) else 0

    /**
     * 查找第一个显示的Item的索引
     * */
    private fun findFirstVisibleItemPosition() = layoutManager?.let {
        when(it) {
            it as LinearLayoutManager, it as GridLayoutManager -> it.findFirstVisibleItemPosition()
            else -> NO_POSITION
        }
    } ?: NO_POSITION

    /**
     * 滑动到底部
     * */
    fun findLastCompletelyVisibleItemPosition() = layoutManager?.let {
        when(it) {
            it as LinearLayoutManager, it as GridLayoutManager -> it.findLastCompletelyVisibleItemPosition()
            else -> NO_POSITION
        }
    } ?: NO_POSITION

    /**
     * 最后的位置
     * */
    fun findLastVisibleItemPosition() = layoutManager?.let {
        when(it) {
            it as LinearLayoutManager, it as GridLayoutManager -> it.findLastVisibleItemPosition()
            else -> NO_POSITION
        }
    } ?: NO_POSITION

    /**
     * 获取View对应索引
     * */
    private fun getPositionByView(view: View?) = view?.let {
        val params = it.layoutParams as LayoutParams
        return if (params.isItemRemoved) NO_POSITION else params.viewPosition
    } ?: NO_POSITION

    private fun getFreeWidth() = width - paddingLeft - paddingRight
    private fun getFreeHeight() = height - paddingTop - paddingBottom

    /**************** util ****************/
}