package com.river.imagepicker.callback

import com.river.imagepicker.entry.LocalMedia

/**
 * @Author: River
 * @Emial: 1632958163@qq.com
 * @Create: 2021/11/9
 **/
fun interface ImagePickerListener {
    fun onChoose(data: List<LocalMedia>)
}