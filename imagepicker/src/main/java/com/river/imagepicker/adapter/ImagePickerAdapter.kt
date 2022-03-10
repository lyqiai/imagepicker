package com.river.imagepicker.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.river.imagepicker.ImagePicker
import com.river.imagepicker.R
import com.river.imagepicker.entry.LocalMedia

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class ImagePickerAdapter(val context: Context, val maxSelectedCount: Int) :
    RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {
    private var localMediaList = mutableListOf<LocalMedia>()
    private var selectedList = mutableListOf<LocalMedia>()
    private val width = context.resources.displayMetrics.widthPixels / 4
    var allowLoadImage = true
    var selectedChangedListener: ((selectedCount: Int) -> Unit)? = null
    var onItemClickListener: ((position: Int) -> Unit)? = null
    var onTakePhotoListener: (() -> Unit)? = null

    fun setData(data: List<LocalMedia>, selected: List<LocalMedia>) {
        localMediaList = data.toMutableList()
        selectedList = selected.toMutableList()
        notifyDataSetChanged()
    }

    fun getData() = localMediaList

    fun getSelectedList(): List<LocalMedia> = selectedList

    inner class ViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val image = view.findViewById<ImageView>(R.id.image)
        private val checkBox = view.findViewById<CheckBox>(R.id.checkbox)
        private val checkBoxContainer = view.findViewById<FrameLayout>(R.id.checkbox_container)

        fun bindData(position: Int) {
            if (position == 0) {
                image.tag = "camera"
                view.setOnClickListener {
                    onTakePhotoListener?.invoke()
                }
            } else {
                checkBox.visibility = View.VISIBLE
                val item = localMediaList[position - 1]

                if (allowLoadImage) {
                    Glide.with(context).load(item.uri).override(width, width).centerCrop().into(image)
                }

                checkBox.isChecked = selectedList.contains(item)
                checkBoxContainer.setOnClickListener {
                    if (!checkBox.isChecked) {
                        if (selectedList.size >= maxSelectedCount) {
                            Toast.makeText(context, String.format(ImagePicker.outMaxSelectedTip, maxSelectedCount), Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }
                        selectedList.add(item)
                        checkBox.isChecked = true
                        selectedChangedListener?.invoke(selectedList.size)
                    } else {
                        selectedList.remove(item)
                        checkBox.isChecked = false
                        selectedChangedListener?.invoke(selectedList.size)
                    }
                }

                view.setOnClickListener {
                    onItemClickListener?.invoke(position - 1)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutId = if (viewType == TYPE_CAMERA) R.layout.item_camera else R.layout.item_photo
        val view = LayoutInflater.from(parent.context).inflate(layoutId, null)
        view.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, width)

        return ViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_CAMERA else TYPE_PHOTO
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(position)
    }

    override fun getItemCount() = localMediaList.size + 1

    fun insertFirstAndSelected(localMedia: LocalMedia) {
        localMediaList.add(0, localMedia)
        if (selectedList.size < maxSelectedCount) {
            selectedList.add(localMedia)
        }
        selectedChangedListener?.invoke(selectedList.size)
        notifyItemInserted(1)
    }

    fun setSelectedList(selected: List<LocalMedia>) {
        selectedList = selected.map {selectedItem-> localMediaList.first {allItem->  allItem.id == selectedItem.id} }.toMutableList()
        selectedChangedListener?.invoke(selectedList.size)
        notifyDataSetChanged()
    }

    companion object {
        const val TYPE_CAMERA = 0
        const val TYPE_PHOTO = 1
    }
}