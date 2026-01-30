package com.example.diarytest.Activity

import android.net.Uri
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.diarytest.R


class ImagePreviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image_preview)

        val imageView: ImageView = findViewById(R.id.imagePreview)
        val imageUri: String? = intent.getStringExtra("imageUri")

        imageUri?.let {
            imageView.setImageURI(Uri.parse(it))
        }

        imageView.setOnClickListener {
            finish() // Click the image to return
        }
    }
}
