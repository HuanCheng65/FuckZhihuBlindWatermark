package com.huanchengfly.fuck.zhihu.watermark.application

import androidx.appcompat.app.AppCompatDelegate
import com.highcapable.yukihookapi.hook.factory.modulePrefs
import com.highcapable.yukihookapi.hook.xposed.application.ModuleApplication
import com.huanchengfly.fuck.zhihu.watermark.DataConsts
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets


class DefaultApplication : ModuleApplication() {

    override fun onCreate() {
        super.onCreate()
        /**
         * 跟随系统夜间模式
         * Follow system night mode
         */
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        modulePrefs.put(DataConsts.JS, getStringFromAsset("js"))
    }

    fun getStringFromAsset(file: String): String {
        try {
            val inputStream: InputStream = this.assets.open(file)
            val length = inputStream.available()
            val buffer = ByteArray(length)
            inputStream.read(buffer)
            return String(buffer, StandardCharsets.UTF_8)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}