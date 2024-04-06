package com.zipper.egl_fbo

import android.os.Bundle
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat


class MainActivity : AppCompatActivity() {

    private var mImageView: ImageView? = null

    private val imageRender = ImageRender(this) {
        runOnUiThread {
            mImageView?.setImageBitmap(it)
        }
    }

    private val surfaceView = ImageEGLSurface()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        mImageView = findViewById(R.id.imageView)

        surfaceView.setRender(imageRender)
        surfaceView.requestRender()

    }

}