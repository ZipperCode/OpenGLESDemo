package com.zipper.gldemo2

import android.annotation.SuppressLint
import android.graphics.Color
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.widget.Button
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import com.google.android.material.color.MaterialColors
import com.skydoves.colorpickerview.ColorPickerDialog
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.zipper.gldemo2.databinding.ActivityMainBinding
import com.zipper.gldemo2.dialog.PreviewDialog
import com.zipper.gldemo2.paint.BrushGLSurfaceView

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentColor: Int = Color.RED

    private var inputRed = 0
    private var inputGreen = 0
    private var inputBlue = 0

    @SuppressLint("SetTextI18n")
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

        binding.etRed.addTextChangedListener{
            inputRed = it.toString().toIntOrNull()?.coerceAtLeast(0)?.coerceAtMost(255) ?: 0
        }
        binding.etGreen.addTextChangedListener{
            inputGreen = it.toString().toIntOrNull()?.coerceAtLeast(0)?.coerceAtMost(255) ?: 0
        }
        binding.etBlue.addTextChangedListener{
            inputBlue = it.toString().toIntOrNull()?.coerceAtLeast(0)?.coerceAtMost(255) ?: 0
        }

        binding.btnColorConfirm.setOnClickListener {
            val color = Color.rgb(inputRed, inputGreen, inputBlue)
            currentColor = color
            binding.glSurfaceView.renderer.setPenColor(color)
            updateSelectColor()
        }

        binding.glSurfaceView.onPickColor = {
            binding.tvPickColorValue.text = it.toRgbString()
            binding.pickColorPreview.setBackgroundColor(it)
        }
        updateSelectColor()

        binding.btnReset.setOnClickListener {
            binding.glSurfaceView.reset()
            updateSelectColor()
            binding.tvPickColorValue.text = null
            binding.pickColorPreview.background = null
            binding.seekbarMixRatio.setProgress(50)
            binding.etRed.editableText.clear()
            binding.etGreen.editableText.clear()
            binding.etBlue.editableText.clear()
        }
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
                    binding.glSurfaceView.renderer.setPenColor(color)
                    updateSelectColor()
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
            binding.glSurfaceView.setMode(if (isSelectionMode) BrushGLSurfaceView.Mode.Selection else BrushGLSurfaceView.Mode.Normal)
            binding.btnSaveSelection.isEnabled = isSelectionMode
        }

        var isPickMode = false
        binding.btnPickMode.setOnClickListener {
            isPickMode = !isPickMode
            binding.btnPickMode.text = if (isPickMode) "退出取色" else "取色模式"
            binding.glSurfaceView.setMode(if (isPickMode) BrushGLSurfaceView.Mode.PickColor else BrushGLSurfaceView.Mode.Normal)
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

    private fun updateSelectColor() {
        val color = binding.glSurfaceView.renderer.brushPen.penColor
        binding.tvColorValue.text = color.toRgbString()
        binding.colorPreview.setBackgroundColor(color)
        val red = Color.red(color)
        val green = Color.green(color)
        val blue = Color.blue(color)
        binding.etRed.setText(red.toString())
        binding.etGreen.setText(green.toString())
        binding.etBlue.setText(blue.toString())
    }

    fun Int.toRgbString(): String {
        return "(${Color.red(this)}, ${Color.green(this)}, ${Color.blue(this)})"
    }
}