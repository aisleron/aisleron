package com.aisleron.widgets

import android.content.Context
import android.util.AttributeSet
import android.view.ContextMenu
import android.view.View
import androidx.recyclerview.widget.RecyclerView


class ContextMenuRecyclerView: RecyclerView {

    constructor(context: Context) :super (context)

    constructor(context: Context, attrs: AttributeSet?) :super (context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super (context, attrs, defStyleAttr)

    private var mContextMenuInfo: RecyclerViewContextMenuInfo? = null

    override fun getContextMenuInfo(): ContextMenu.ContextMenuInfo? {
        return mContextMenuInfo
    }

    override fun showContextMenuForChild(originalView: View?): Boolean {
        val longPressPosition = getChildAdapterPosition(originalView!!)
        if (longPressPosition >= 0) {
            val longPressId = adapter!!.getItemId(longPressPosition)
            mContextMenuInfo = RecyclerViewContextMenuInfo(originalView, longPressPosition, longPressId)
            return super.showContextMenuForChild(originalView)
        }
        return false
    }

    class RecyclerViewContextMenuInfo(val targetView: View, val position: Int, val id: Long?) : ContextMenu.ContextMenuInfo

}