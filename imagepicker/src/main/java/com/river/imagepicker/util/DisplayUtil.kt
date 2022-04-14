package com.river.imagepicker.util

import android.content.Context

/**

 * @Author River
 * @Date 2021/11/1-10:24
 */
object DisplayUtil {
    /**
     * 将dip或dp值转换为px值，保证尺寸大小不变
     *
     * @param dipValue
     * @return
     */
    fun dp2px(context: Context, dpValue: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dpValue * scale + 0.5f).toInt()
    }
}