package com.river.imagepickerdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.river.imagepicker.ImagePicker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val imagePicker = ImagePicker(this)
        findViewById<TextView>(R.id.action_image_picker).setOnClickListener {
            imagePicker.pickerImage(null, 5) {

            }
        }
    }
}