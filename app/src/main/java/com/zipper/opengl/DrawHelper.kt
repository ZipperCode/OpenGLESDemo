package com.zipper.opengl

import android.content.Context
import android.graphics.Bitmap
import android.text.TextUtils
import com.google.gson.reflect.TypeToken
import com.zipper.opengl.bean.PositionItem
import com.zipper.opengl.utils.AssetsUtil
import com.zipper.opengl.utils.AssetsUtil.getAssetsContent
import com.zipper.opengl.utils.KidsGsonUtil

/**
 *
 * @author zhangzhipeng
 * @date 2024/4/1
 */
class DrawHelper(private val context: Context) {

    private var projectId: String? = null

    private var maskBitmap: Bitmap? = null

    private var orgBitmap: Bitmap? = null

    private var svg2Bitmap: Bitmap? = null

    private var positionItemList: List<PositionItem>? = null

    private var isMaskCombine = false

    fun setProjectId(projectId: String) {
        this.projectId = projectId
        if (maskBitmap != null && !maskBitmap!!.isRecycled) {
            maskBitmap!!.recycle()
            maskBitmap = null
        }
        if (orgBitmap != null && !orgBitmap!!.isRecycled) {
            orgBitmap!!.recycle()
            orgBitmap = null
        }
        if (svg2Bitmap != null && !svg2Bitmap!!.isRecycled) {
            svg2Bitmap!!.recycle()
            svg2Bitmap = null
        }
    }

    fun getMaskBitmap(): Bitmap? {
        if (maskBitmap == null) {
            val maskPath = "color/$projectId/mask.png"
            if (AssetsUtil.checkFileExist(context, maskPath)) {
                maskBitmap = AssetsUtil.decodeBitmapFromAssets(context, maskPath)
            } else {
                isMaskCombine = true
                maskBitmap = AssetsUtil.decodeBitmapFromAssets(context, "color/$projectId/maskCombine.png")
            }
        }
        return maskBitmap
    }

    fun getOrgBitmap(): Bitmap? {
        if (orgBitmap == null) {
            orgBitmap = AssetsUtil.decodeBitmapFromAssets(context, "color/$projectId/org.png")
        }
        return orgBitmap
    }

    fun getSvg2Bitmap(): Bitmap? {
        if (svg2Bitmap == null) {
            svg2Bitmap = AssetsUtil.decodeBitmapFromAssets(context, "color/$projectId/svg2png.png")
        }
        return svg2Bitmap
    }

    fun getPositionItemList(): List<PositionItem> {
        if (positionItemList == null) {
            val positionJsonContent = getAssetsContent(context!!, "color/$projectId/position.json")
            positionItemList = if (TextUtils.isEmpty(positionJsonContent)) {
                ArrayList<PositionItem>()
            } else {
                KidsGsonUtil.fromJson(positionJsonContent, object : TypeToken<List<PositionItem?>?>() {}.type)
            }
        }
        return positionItemList!!
    }

    fun findPositionItem(maskColor: Int): PositionItem? {
        val positionItemList: List<PositionItem> = getPositionItemList()
        for (positionItem in positionItemList) {
            val areaColorARGB: Int = positionItem.getAreaColorARGB()
            if (maskColor == areaColorARGB) {
                return positionItem
            }
        }
        return null
    }

    fun isMaskCombine(): Boolean {
        return isMaskCombine
    }
}
