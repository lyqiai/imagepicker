package com.river.imagepicker

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.river.imagepicker.callback.ImagePickerListener

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class ImagePicker(private val context: Context, private val registry: ActivityResultRegistry) :
    DefaultLifecycleObserver {
    private lateinit var launcher: ActivityResultLauncher<Intent>
    private var listener: ImagePickerListener? = null

    override fun onCreate(owner: LifecycleOwner) {
        super.onCreate(owner)
        launcher = registry.register(
            "image_picker",
            owner,
            ActivityResultContracts.StartActivityForResult()
        ) {

        }
    }

    fun pickerImage(
        selectedIds: List<Long>? = null,
        maxSelectedCount: Int = Int.MAX_VALUE,
        listener: ImagePickerListener
    ) {
        this.listener = listener

        val intent = Intent(context, ImagePickerActivity::class.java).apply {
            putExtra("maxSelectedCount", maxSelectedCount)
            putExtra("selectedIds", selectedIds?.toLongArray())
        }
        launcher.launch(intent)
    }
}
