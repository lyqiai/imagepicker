package com.river.imagepicker.entry

import java.io.Serializable

/**

 * @Author River
 * @Date 2021/11/1-10:24
 */
data class LocalMedia(
    val id: Long,
    val path: String,
    val name: String,
    val uri: String,
    val size: Int,
    val width: Int,
    val height: Int,
) : Serializable