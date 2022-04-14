package com.river.imagepicker

import android.Manifest
import android.app.Activity
import android.content.*
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
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

 * @Author River
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

    private var takePhotoUri: Uri? = null
    private val takePhotoLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) refresh()
        }

    private val externalStoragePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) loadData()
        }

    private val cameraPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            if (it) takePhoto()
        }


    override fun onCreate(savedInstanceState: Bundle?) {
        initUIStyle()
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_image_picker)

        maxSelectedCount = intent.getIntExtra("maxSelectedCount", Int.MAX_VALUE)
        selectedIds = intent.getLongArrayExtra("selectedIds")

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

        actionBack.setOnClickListener { finish() }

        actionConfirm.setOnClickListener(this::handleConfirm)

        externalStoragePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun onTakePhotoListener() {
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun takePhoto() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val photoName = "$timeStamp.jpg"

        takePhotoUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val values = ContentValues()
            values.put(MediaStore.MediaColumns.DISPLAY_NAME, photoName)
            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        } else {
            FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                File(externalCacheDir!!.absolutePath, photoName)
            )
        }

        takePhotoLauncher.launch(takePhotoUri)
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

    private fun refresh() {
        val localMedia = MediaUtil.queryImageByPath(this@ImagePickerActivity, takePhotoUri!!.path!!)
        if (localMedia != null) {
            recyclerView.post {
                adapter.insertFirstAndSelected(localMedia)
            }
        }
    }

    private fun initUIStyle() {
        val isDark = UIModeUtil.isDarkMode(this)
        val themeId = if (isDark)
            com.google.android.material.R.style.Theme_AppCompat_DayNight_NoActionBar
        else
            com.google.android.material.R.style.Theme_AppCompat_Light_NoActionBar
        setTheme(themeId)

        HiStatusBar.setStatusBar(
            this,
            !isDark,
            translucent = false,
            statusBarColor = android.R.color.background_dark
        )
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        initUIStyle()
    }
}