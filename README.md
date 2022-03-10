## ImagePicker

### 集成
根目录build.gradle：
```
allprojects {
    repositories {
    	...
        maven { url 'https://jitpack.io' }						ADD
    }
}
```
模块build.gradle：
```
dependencies {
	...
    implementation 'com.github.lyqiai:imagepicker:0.0.1'			ADD
}

```
### 使用
```kotlin
val imagePicker = ImagePicker(FragmentActivity)

imagePicker.pickerImage(selectedIds, maxSelectedCount, listener)

fun pickerImage(
     selectedIds: List<Long>? = null,       //对应LocalMedia.id集合
     maxSelectedCount: Int = Int.MAX_VALUE, //最大选中图片数量
     listener: ImagePickerListener          //回调事件
)

fun interface ImagePickerListener {
    fun onChoose(data: List<LocalMedia>)
}
```

### 图片对象
```kotlin
data class LocalMedia(
    val id: Long,
    val path: String,
    val name: String,
    val uri: String,
    val size: Int,
    val width: Int,
    val height: Int,
)
```

### 相关文字配置
```kotlin
class ImagePicker {
    companion object {
            var title: String = "所有图片"
            var selectedCount: String = "已选%D张"
            var confirm: String = "完成"
            var preview: String = "浏览图片"
            var outMaxSelectedTip: String = "最多选择%d张图片" //必须包含%d
    }
}
```
