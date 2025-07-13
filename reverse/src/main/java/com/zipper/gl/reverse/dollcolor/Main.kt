package com.zipper.gl.reverse.dollcolor

import Config
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets

/**
 *
 * @author  zhangzhipeng
 * @date    2025/7/11
 */
object Main {

    private const val RES_PATH = "D:\\Project\\AndroidProject\\OpenGLEDemo\\reverse\\src\\main\\resources"

    private val outDir: File = File("${RES_PATH}${File.separator}build${File.separator}DollColor")

    @JvmStatic
    fun main(args: Array<String>) {
        val inDir = File(RES_PATH)
        val listFiles = inDir.listFiles() ?: return
        val jsonFiles = listFiles.filter { it.name.endsWith(".json") }
        val decodeConfig = decodeConfig("z224667603.json")
        val configOutFile = File(outDir, "config.json")
        configOutFile.writeText(Gson().toJson(decodeConfig))
        for (jsonFile in jsonFiles) {
            runCatching {
                val resConfigMap = decodeResConfig(decodeConfig.localAssetsKey, jsonFile.name)
                for (entry in resConfigMap) {
                    val key = entry.key
                    val value = resConfigMap[key] ?: continue
                    val file = File(inDir, value)
                    if (!file.exists()) {
                        continue
                    }
                    val outFile = File(outDir, key.replace("/", File.separator))
                    if (!outFile.parentFile.exists()) {
                        outFile.parentFile.mkdirs()
                    }
                    if (value.contains("jpg") || value.contains("zip")) {
                        decodeByte(file.absolutePath, outFile.absolutePath, decodeConfig.localAssetsKey)
                    } else {
                        decode(file.absolutePath, outFile.absolutePath, decodeConfig.localAssetsKey)
                    }
                }
            }.onFailure {
                println("解码失败：${jsonFile.name}")
            }
        }
    }

    /**
     * 解密配置
     */
    private fun decodeConfig(configFileName: String): Config {
        val inPath = "${RES_PATH}${File.separator}${configFileName}"
        val outPath = File(outDir, configFileName)
        val content = decode(inPath, outPath.absolutePath, "zxophjkl")
        return Gson().fromJson(content, Config::class.java)
    }

    /**
     * 解密资源
     */
    private fun decodeResConfig(key: String, configFileName: String): Map<String, String> {
        val inPath = "${RES_PATH}${File.separator}${configFileName}"
        val outPath = File(outDir, configFileName)
        val content = decode(inPath, outPath.absolutePath, key)
        return Gson().fromJson(content, object : TypeToken<Map<String, String>>() {}.type)
    }

    private fun decode(inPath: String, outPath: String, key: String): String {
        println("解码 = $inPath >> $outPath")
        val resultBytes = a(File(inPath).readBytes(), key)
        val outFile = File(outPath)
        if (!outFile.parentFile.exists()) {
            outFile.parentFile.mkdirs()
        }
        if (!outFile.exists()) {
            outFile.createNewFile()
        }
        val result = String(resultBytes)
        outFile.writeText(result)
        return result
    }

    private fun decodeByte(inPath: String, outPath: String, key: String) {
        val resultBytes = a(File(inPath).readBytes(), key)
        val outFile = File(outPath)
        if (!outFile.parentFile.exists()) {
            outFile.parentFile.mkdirs()
        }
        if (!outFile.exists()) {
            outFile.createNewFile()
        }
        outFile.writeBytes(resultBytes)
    }

    fun a(bArr: ByteArray, str: String): ByteArray {
        val length = bArr.size
        for (i2 in 0 until length) {
            bArr[i2] = (bArr[i2].toInt() xor str.toByteArray(StandardCharsets.UTF_8)[i2 % str.length].toInt()).toByte()
        }
        return bArr
    }

    fun b(str: String, str2: String, str3: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(2, SecretKeySpec(str2.toByteArray(), "AES"), IvParameterSpec(str3.toByteArray()))
        return String(cipher.doFinal(AndroidBase64.decode(str.toByteArray(charset("UTF-8")), 0)), StandardCharsets.UTF_8)
    }


    fun aesDecode(content: String, key: String, iv: String): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(2, SecretKeySpec(key.toByteArray(), "AES"), IvParameterSpec(iv.toByteArray()))
        return String(cipher.doFinal(AndroidBase64.decode(content.toByteArray(StandardCharsets.UTF_8), 8)), StandardCharsets.UTF_8)
    }
}