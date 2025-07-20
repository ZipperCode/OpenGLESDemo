package com.zipper.gldemo.pen

import android.os.Bundle
import android.widget.SeekBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.zipper.gldemo.pen.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
//        val brushView = findViewById<GLPenView>(R.id.pen_view)
        binding.seekBar.progress = 40
        binding.seekBar.max = 100
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvBrushSize.text = "笔刷大小：${progress}"
                binding.penView.setBrushSize(progress.toFloat())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {

            }
        })
        binding.btn1.setOnClickListener {
            binding.tvBrushSize.text = "笔刷大小：${binding.seekBar.progress}"
            val config = BrushManager.getBrushConfigs("Brush1") ?: return@setOnClickListener
            binding.penView.setBrushConfig(config)
        }

        binding.btn2.setOnClickListener {
            binding.tvBrushSize.text = "笔刷大小：${binding.seekBar.progress}"
            val config = BrushManager.getBrushConfigs("Brush2") ?: return@setOnClickListener
            binding.penView.setBrushConfig(config)
        }
        binding.btn3.setOnClickListener {
            binding.penView.clean()
        }
        binding.btnBack.setOnClickListener {
            binding.penView.undo()
        }
        binding.btnForward.setOnClickListener {
            binding.penView.fallback()
        }
    }
}