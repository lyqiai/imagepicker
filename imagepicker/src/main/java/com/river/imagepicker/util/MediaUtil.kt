package com.river.imagepicker.util

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import com.river.imagepicker.entry.LocalMedia

/**

 * @Author River
 * @Date 2021/11/1-10:24
 */
object MediaUtil {
    private val QUERY_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    private val QUERY_FIELDS = arrayOf(
        MediaStore.Images.Media._ID,
        MediaStore.Images.Media.DATA,
        MediaStore.Images.Media.SIZE,
        MediaStore.Images.Media.DISPLAY_NAME,
        MediaStore.Images.Media.WIDTH,
        MediaStore.Images.Media.HEIGHT
    )
    private val QUERY_WHERE = "${MediaStore.Images.Media.MIME_TYPE} = ? or ${MediaStore.Images.Media.MIME_TYPE} = ?"
    private val QUERY_ARGS = arrayOf("image/jpeg", "image/png")
    private val QUERY_SORT = "${MediaStore.Images.Media.DATE_MODIFIED} desc"

    fun loadImages(context: Context): List<LocalMedia> {
        val contentResolver = context.contentResolver

        val cursor = contentResolver.query(
            QUERY_URI,
            QUERY_FIELDS,
            QUERY_WHERE,
            QUERY_ARGS,
            QUERY_SORT
        )!!

        val localMediaList = mutableListOf<LocalMedia>()
        while (cursor.moveToNext()) {
            localMediaList.add(convert2LocalMedia(cursor))
        }

        cursor.close()

        return localMediaList
    }

    fun queryImageByPath(context: Context, path: String): LocalMedia? {
        val cursor = context.contentResolver.query(
            QUERY_URI,
            QUERY_FIELDS,
            "${MediaStore.Images.Media.MIME_TYPE} = ? or ${MediaStore.Images.Media.MIME_TYPE} = ? and ${MediaStore.Images.Media.DATA} = ?",
            arrayOf("image/jpeg", "image/png", path),
            QUERY_SORT
        )

        if (cursor?.moveToFirst() == true) {
            return convert2LocalMedia(cursor)
        }

        return null
    }

    @SuppressLint("Range")
    private fun convert2LocalMedia(cursor: Cursor): LocalMedia {
        val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Images.Media._ID))
        val path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
        val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        val width = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.WIDTH))
        val height = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.HEIGHT))

        val name = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DISPLAY_NAME))
        val size = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media.SIZE))

        return LocalMedia(id, path, name, uri.toString(), size, width, height)
    }

}