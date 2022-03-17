package com.river.imagepicker

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.river.imagepicker.adapter.ImagePickerAdapter
import com.river.imagepicker.decoration.GridSpaceDecoration
import com.river.imagepicker.util.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

/**

 * @Author river
 * @Date 2021/11/1-10:24
 */
class ImagePickerActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedCountText: TextView
    private lateinit var actionConfirm: Button
    private lateinit var actionBack: ImageView
    private lateinit var title: TextView

    private lateinit var adapter: ImagePickerAdapter

    private var maxSelectedCount = 0
    private var selectedIds: LongArray? = null
    private var tempFile: File? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        HiStatusBar.setStatusBar(this, true, translucent = false)

        maxSelectedCount = intent.getIntExtra("maxSelectedCount", Int.MAX_VALUE)
        selectedIds = intent.getLongArrayExtra("selectedIds")

        setContentView(R.layout.activity_image_picker)
        recyclerView = findViewById(R.id.list_content)
        selectedCountText = findViewById(R.id.selected_count)
        actionConfirm = findViewById(R.id.action_confirm)
        actionBack = findViewById(R.id.action_back)
        title = findViewById(R.id.title)

        title.text = ImagePicker.title

        actionConfirm.text = ImagePicker.confirm

        setSelectedCountText(selectedIds?.size ?: 0)

        adapter = ImagePickerAdapter(this, maxSelectedCount)
        adapter.selectedChangedListener = this::setSelectedCountText
        adapter.onItemClickListener = this::onItemClickListener
        adapter.onTakePhotoListener = this::onTakePhotoListener
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(GridSpaceDecoration(DisplayUtil.dp2px(this, 2)))
        //滚动时是否禁止加载图片
//        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                adapter.allowLoadImage = newState == SCROLL_STATE_IDLE
//                if (adapter.allowLoadImage) {
//                    adapter.notifyDataSetChanged()
//                }
//            }
//        })

        actionBack.setOnClickListener { finish() }

        actionConfirm.setOnClickListener(this::handleConfirm)

        checkPermission(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CODE_PERM_WRITE_EXTERNAL_STORAGE,
                this::loadData
        )
    }

    private fun checkPermission(
            permissions: Array<String>,
            requestCode: Int,
            callback: () -> Unit
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val lostPerms = permissions.filter { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }
            if (lostPerms.isEmpty()) {
                callback.invoke()
            } else {
                requestPermissions(lostPerms.toTypedArray(), requestCode)
            }
        } else {
            callback.invoke()
        }
    }

    private fun onTakePhotoListener() {
        checkPermission(arrayOf(Manifest.permission.CAMERA), CODE_PERM_CAMERA, this::takePhoto)
    }

    private fun takePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        tempFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
        val photoURI = FileUtil.file2Uri(this@ImagePickerActivity, tempFile!!)

        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.resolveActivity(packageManager)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)

        startActivityForResult(intent, CODE_TAKE_PIC)
    }

    private fun onItemClickListener(position: Int) {
        PreviewImageDialog.show(
                supportFragmentManager,
                adapter.getData(),
                adapter.getSelectedList(),
                maxSelectedCount,
                position
        ) {
            adapter.setSelectedList(it)
        }
    }

    private fun handleConfirm(view: View) {
        val intent = Intent()
        intent.putExtra("selectedList", adapter.getSelectedList() as Serializable)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun setSelectedCountText(count: Int) {
        selectedCountText.text = String.format(ImagePicker.selectedCount, count)
    }

    private fun loadData() {
        lifecycleScope.launch(Dispatchers.IO) {
            val localMediaList = MediaUtil.loadImages(this@ImagePickerActivity)
            val selectedLocalMediaList =
                    localMediaList.filter { selectedIds?.contains(it.id) ?: false }
            if (isFinishing || isDestroyed) {
                return@launch
            }

            withContext(Dispatchers.Main) {
                adapter.setData(localMediaList, selectedLocalMediaList)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == CODE_TAKE_PIC) {
            MediaUtil.refreshPhotoAlbum(this@ImagePickerActivity, tempFile!!) { path, uri ->
                val localMedia = MediaUtil.queryImageByPath(
                        this@ImagePickerActivity,
                        path
                )
                if (localMedia != null) {
                    recyclerView.post {
                        adapter.insertFirstAndSelected(localMedia)
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CODE_PERM_WRITE_EXTERNAL_STORAGE) {
            if (grantResults.size == permissions.size) {
                loadData()
            }
        }

        if (requestCode == CODE_PERM_CAMERA) {
            if (grantResults.size == permissions.size) {
                takePhoto()
            }
        }
    }

    companion object {
        private const val CODE_TAKE_PIC = 1

        private const val CODE_PERM_WRITE_EXTERNAL_STORAGE = 10

        private const val CODE_PERM_CAMERA = 11
    }
}