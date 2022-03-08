package com.river.imagepicker.decoration

import android.graphics.Rect
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class GridSpaceDecoration(val offset: Int) : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(outRect: Rect, itemPosition: Int, parent: RecyclerView) {
        val layoutManager = parent.layoutManager as GridLayoutManager
        val spanCount = layoutManager.spanCount

        //横向右边距处理：最后一列边距为0
        val cellPos = itemPosition % spanCount
        if (cellPos != spanCount) {
            outRect.right = offset / 2
        }
        //横向左边距处理：第一列边距为0
        if (cellPos != 0) {
            outRect.left = offset / 2
        }
        //纵向上边距处理：第一行为0
        if (itemPosition > spanCount) {
            outRect.top = offset
        }
    }
}