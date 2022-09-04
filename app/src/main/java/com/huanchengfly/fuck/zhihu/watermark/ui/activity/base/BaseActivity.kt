@file:Suppress("MemberVisibilityCanBePrivate", "UNCHECKED_CAST")

package com.huanchengfly.fuck.zhihu.watermark.ui.activity.base

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.WindowCompat
import androidx.viewbinding.ViewBinding
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.type.android.LayoutInflaterClass
import com.huanchengfly.fuck.zhihu.watermark.R
import com.huanchengfly.fuck.zhihu.watermark.utils.factory.isNotSystemInDarkMode
import java.lang.reflect.ParameterizedType

abstract class BaseActivity<VB : ViewBinding> : AppCompatActivity() {

    /**
     * Get the binding layout object
     *
     * 获取绑定布局对象
     */
    lateinit var binding: VB

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        javaClass.genericSuperclass.also { type ->
            if (type is ParameterizedType) {
                binding = (type.actualTypeArguments[0] as Class<VB>).method {
                    name = "inflate"
                    param(LayoutInflaterClass)
                }.get().invoke<VB>(layoutInflater) ?: error("binding failed")
                setContentView(binding.root)
            } else error("binding but got wrong type")
        }
        /**
         * Hide Activity title bar
         * 隐藏系统的标题栏
         */
        supportActionBar?.hide()
        /**
         * Init immersive status bar
         * 初始化沉浸状态栏
         */
        WindowCompat.getInsetsController(window, window.decorView).apply {
            isAppearanceLightStatusBars = isNotSystemInDarkMode
            isAppearanceLightNavigationBars = isNotSystemInDarkMode
        }
        ResourcesCompat.getColor(resources, R.color.colorThemeBackground, null).also {
            window?.statusBarColor = it
            window?.navigationBarColor = it
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) window?.navigationBarDividerColor = it
        }
        /**
         * Init children
         * 装载子类
         */
        onCreate()
    }

    /**
     * Callback [onCreate] method
     *
     * 回调 [onCreate] 方法
     */
    abstract fun onCreate()
}