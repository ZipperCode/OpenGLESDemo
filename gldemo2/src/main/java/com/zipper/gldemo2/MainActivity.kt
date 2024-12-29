package com.zipper.gldemo2

import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zipper.gldemo2.paint.BrushGLSurfaceView

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
        val surfaceView = findViewById<BrushGLSurfaceView>(R.id.glSurfaceView)
        val btnRed = findViewById<Button>(R.id.btn_red)
        val btnBlue = findViewById<Button>(R.id.btn_blue)
        btnRed.setOnClickListener {
            surfaceView.setColor(Color.RED)
        }
        btnBlue.setOnClickListener {
            surfaceView.setColor(Color.BLUE)
        }
    }
}