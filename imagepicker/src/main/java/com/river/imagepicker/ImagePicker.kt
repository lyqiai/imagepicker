package com.river.imagepicker

import android.app.Activity
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import com.river.imagepicker.callback.ImagePickerListener
import com.river.imagepicker.entry.LocalMedia

/**

 * @Author River
 * @Date 2021/11/1-10:24
 */
class ImagePicker(private val activity: FragmentActivity) {

    private val launcher: ActivityResultLauncher<Intent> = (activity as ComponentActivity).registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            val selectedList = it.data?.getSerializableExtra("selectedList") as List<LocalMedia>?
            listener?.onChoose(selectedList ?: emptyList())
        }
    }
    private var listener: ImagePickerListener? = null

    fun pickerImage(
            selectedIds: List<Long>? = null,
            maxSelectedCount: Int = Int.MAX_VALUE,
            listener: ImagePickerListener
    ) {
        this.listener = listener

        val intent = Intent(activity, ImagePickerActivity::class.java).apply {
            putExtra("maxSelectedCount", maxSelectedCount)
            putExtra("selectedIds", selectedIds?.toLongArray())
        }
        launcher.launch(intent)
    }

    companion object {
        var title: String = "所有图片"
        var selectedCount: String = "已选%d张"
        var confirm: String = "完成"
        var preview: String = "浏览图片"
        var outMaxSelectedTip: String = "最多选择%d张图片"
    }
}
