package com.zipper.gl_vector

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zipper.gl_vector.surfaceview.GLView
import androidx.core.graphics.createBitmap
import java.io.File
import kotlin.system.measureTimeMillis

class MainActivity : AppCompatActivity() {

    private val render = DrawImageRender(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val surfaceView = findViewById<GLView>(R.id.surfaceView)

        surfaceView.apply {
            register(render)
            Thread(Runnable {
                val time = measureTimeMillis {
                    val bitmap = BitmapFactory.decodeStream(assets.open("5.png"))
                    val maskBitmap = createBitmap(bitmap.width, bitmap.height)
                    val ret = RegionCalculator.regionGenerate(bitmap, maskBitmap)
                    println()

                    queueEvent {
                        render.uploadLine(bitmap)
                        render.uploadMask(maskBitmap)
                        requestRender()
                    }

                    File(filesDir, "mask.png").outputStream().use {
                        maskBitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }

                    File(filesDir, "line.png").outputStream().use {
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
                    }
                }
                Log.d("BAAA", "耗时 = ${time}")

            }).start()
        }

    }
}