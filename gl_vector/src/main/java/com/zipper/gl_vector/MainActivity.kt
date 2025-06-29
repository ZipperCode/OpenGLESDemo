package com.zipper.gl_vector

import android.graphics.BitmapFactory
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zipper.gl_vector.surfaceview.GLView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<GLView>(R.id.surfaceView).apply {
           register(DrawImageRender(context))
        }

//        Thread(Runnable{
//            val bitmap = BitmapFactory.decodeStream(assets.open("svg2png.png"))
//            val newBitmap = RegionCalculator.calculateRegions(bitmap)
//            println()
//
//        }).start()
    }
}