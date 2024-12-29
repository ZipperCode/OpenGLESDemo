package com.zipper.gldemo2

import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.color.MaterialColors
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.zipper.gldemo2.databinding.ActivityMainBinding
import com.zipper.gldemo2.dialog.PreviewDialog
import com.zipper.gldemo2.paint.BrushGLSurfaceView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentColor: Int = Color.RED

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        setupColorPicker()
        setupMixRatioControl()
        setupSelectionButtons()
    }

    private fun setupColorPicker() {
        // 更新颜色预览
        updateColorPreview(currentColor)

        // 设置颜色选择按钮点击事件
        binding.btnColorPicker.setOnClickListener {
            ColorPickerDialog.Builder(this)
                .setTitle("选择颜色")
                .setPositiveButton("确定", ColorEnvelopeListener { envelope, _ ->
                    val color = envelope.color
                    currentColor = color
                    updateColorPreview(color)
                    binding.glSurfaceView.renderer.setPenColor(color)
                })
                .setNegativeButton("取消") { dialogInterface, _ ->
                    dialogInterface.dismiss()
                }
                .attachAlphaSlideBar(true)
                .attachBrightnessSlideBar(true)
                .show()
        }
    }

    private fun setupMixRatioControl() {
        // 设置SeekBar变化监听
        binding.seekbarMixRatio.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val ratio = progress / 100f
                binding.tvMixRatio.text = "混合比例: %.2f".format(ratio)
                binding.glSurfaceView.renderer.setMixRatio(ratio)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun setupSelectionButtons() {
        var isSelectionMode = false

        // 选区模式按钮
        binding.btnSelectionMode.setOnClickListener {
            isSelectionMode = !isSelectionMode
            binding.btnSelectionMode.text = if (isSelectionMode) "退出选区" else "选区模式"
            binding.glSurfaceView.setSelectionMode(isSelectionMode)
            binding.btnSaveSelection.isEnabled = isSelectionMode
        }

        // 保存选区按钮
        binding.btnSaveSelection.apply {
            isEnabled = false
            setOnClickListener {
                binding.glSurfaceView.getSelectionContent()?.let { bitmap ->
                    PreviewDialog.show(this@MainActivity, bitmap)
                }
            }
        }
    }

    private fun updateColorPreview(color: Int) {
        binding.colorPreview.setBackgroundColor(color)
    }
}