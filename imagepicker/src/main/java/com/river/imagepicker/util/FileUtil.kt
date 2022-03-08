package com.river.imagepicker.util

import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
object FileUtil {
    fun file2Uri(context: Context, file: File): Uri {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        ) else Uri.fromFile(file)
    }
}