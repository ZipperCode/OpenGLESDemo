package com.zipper.gldemo2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.IOException

object AssetsUtil {
    fun getAssetsImage(
        context: Context,
        filename: String,
    ): Bitmap {
        return BitmapFactory.decodeStream(context.assets.open(filename))
    }

    fun decodeBitmapFromAssets(context: Context, fileName: String): Bitmap? {
        var bitmap: Bitmap? = null
        try {
            context.assets.open(fileName).use { `is` -> bitmap = BitmapFactory.decodeStream(`is`) }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return bitmap
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

    fun checkFileExist(context: Context, filename: String?): Boolean {
        var result = false
        val asset = context.assets
        try {
            asset.open(filename!!)
            result = true
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return result
    }
}
