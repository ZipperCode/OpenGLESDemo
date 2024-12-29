package com.zipper.gldemo2.dialog

import android.app.Dialog
import android.content.Context
import android.graphics.Bitmap
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PreviewDialog(
    context: Context,
    private val previewBitmap: Bitmap
) : Dialog(context) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 创建对话框布局
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setPadding(32, 32, 32, 32)
        }

        // 添加预览图片
        val imageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
            setImageBitmap(previewBitmap)
        }
        layout.addView(imageView)

        // 添加确定按钮
        val button = AppCompatButton(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = 32
            }
            text = "确定"
            setOnClickListener {
                dismiss()
            }
        }
        layout.addView(button)

        setContentView(layout)
    }

    companion object {
        fun show(context: Context, bitmap: Bitmap) {
            PreviewDialog(context, bitmap).show()
        }
    }
}
