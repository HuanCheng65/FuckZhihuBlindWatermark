package com.huanchengfly.fuck.zhihu.watermark

import android.content.res.AssetManager
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets


fun AssetManager.readFileAsString(fileName: String): String {
    try {
        val inputStream: InputStream = open(fileName)
        val length = inputStream.available()
        val buffer = ByteArray(length)
        inputStream.read(buffer)
        return String(buffer, StandardCharsets.UTF_8)
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return ""
}