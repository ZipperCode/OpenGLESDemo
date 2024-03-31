package com.zipper.opengl.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.lang.Exception

object AssetsUtil {
    fun getAssetsImage(
        context: Context,
        filename: String,
    ): Bitmap {
        return BitmapFactory.decodeStream(context.assets.open(filename))
    }

    fun getAssetsContent(
        context: Context,
        filename: String,
    ): String {
        try {
            context.assets.open(filename).use {
                return it.reader(Charsets.UTF_8).readText()
            }
        } catch (e: Exception) {
            return ""
        }
    }
}
