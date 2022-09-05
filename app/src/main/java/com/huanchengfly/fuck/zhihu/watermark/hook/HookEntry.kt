package com.huanchengfly.fuck.zhihu.watermark.hook

import android.annotation.SuppressLint
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.res.XmlResourceParser
import android.webkit.JsResult
import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.field
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.param.PackageParam
import com.highcapable.yukihookapi.hook.type.android.BundleClass
import com.highcapable.yukihookapi.hook.type.java.CharSequenceType
import com.highcapable.yukihookapi.hook.type.java.IntType
import com.highcapable.yukihookapi.hook.type.java.StringType
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.huanchengfly.fuck.zhihu.watermark.R
import com.huanchengfly.fuck.zhihu.watermark.readFileAsString
import org.xmlpull.v1.XmlPullParser


@InjectYukiHookWithXposed(
    entryClassName = "FuckZhihuBlindWatermark",
    isUsingResourcesHook = true
)
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {
        debugTag = "FuckZhihuBlindWatermark"
    }

    @SuppressLint("DiscouragedApi")
    override fun onHook() = encase {
        loadApp(name = "com.zhihu.android") {
            val js = moduleAppResources.assets.readFileAsString("js")

            var settingsResId = 0

            val preferenceInflaterClass = findClass("androidx.preference.i")
            val settingsFragmentClass = findClass("com.zhihu.android.app.ui.fragment.preference.SettingsFragment")
            val preferenceFragmentCompatClass = findClass("androidx.preference.g", "androidx.preference.f")
            val preferenceClass = findClass("androidx.preference.Preference")
            val addPreferencesFromResourceMethod = preferenceFragmentCompatClass.clazz.method {
                name = "b"
                param(IntType)
                returnType(UnitType)
            }
            val inflateMethod = preferenceInflaterClass.instance?.method {
                param(XmlPullParser::class.java, "androidx.preference.PreferenceGroup")
                returnType("androidx.preference.Preference")
            }
            val setSummaryMethod = preferenceClass.instance?.method {
                name = "a"
                param(CharSequenceType)
                returnType(UnitType)
            }
            val findPreferenceMethod = settingsFragmentClass.instance?.method {
                name = "a"
                param(CharSequenceType)
                superClass()
            }
            settingsFragmentClass.hook {
                injectMember {
                    method {
                        name = "i"
                        emptyParam()
                        returnType(IntType)
                    }
                    afterHook {
                        settingsResId = result<Int>() ?: 0
                    }
                }
                injectMember {
                    method {
                        name = "onCreate"
                        param(BundleClass)
                    }
                    afterHook {
                        findPreferenceMethod?.get(instance())?.call("fuck_zhihu_blind_watermark")?.let {
                            setSummaryMethod?.get(it)?.call("已工作 ${appPref.getInt(APP_PREF_STAT_TIMES_KEY, 0)} 次")
                        }
                    }
                }
            }
            preferenceFragmentCompatClass.hook {
                injectMember {
                    method {
                        name = "b"
                        param(IntType)
                        returnType(UnitType)
                    }
                    beforeHook {
                        val id = args[0] as Int
                        loggerD(msg = "hooked addPreferencesFromResource id = $id")
                        if (id == settingsResId) {
                            addPreferencesFromResourceMethod.get(instance()).call(114514)
                        }
                    }
                }
            }
            preferenceInflaterClass.hook {
                injectMember {
                    method {
                        param(IntType, "androidx.preference.PreferenceGroup")
                        returnType("androidx.preference.Preference")
                    }
                    beforeHook {
                        val parser: XmlResourceParser
                        val id = args[0] as Int
                        if (id == 114514) {
                            parser = moduleAppResources.getXml(R.xml.settings)
                        } else {
                            return@beforeHook
                        }
                        result = inflateMethod?.get(instance())?.call(parser, args[1])
                    }
                }
            }
            findClass("com.zhihu.android.answer.module.content.appview.AnswerChromeClient").instance?.superclass?.hook {
                loggerI(msg = "startHook ${this.instanceClass.simpleName}")
                injectMember {
                    method {
                        name = "onReceivedTitle"
                        paramCount(2)
                        superClass()
                    }
                    afterHook {
                        loggerI(msg = "hooked ${this.instanceClass.simpleName} ${this.method.name}")
                        val loadUrlMethod = method.parameters[0].type.method {
                            param(StringType)
                            returnType(UnitType)
                        }.get(args[0])
                        loggerD(msg = "find loadUrl method $loadUrlMethod")
                        loadUrlMethod.call(js)
                        loggerD(msg = "try to call loadUrl method $loadUrlMethod to inject js")
                    }
                }
                injectMember {
                    method {
                        name = "onJsAlert"
                        paramCount(4)
                        superClass()
                    }
                    beforeHook {
                        loggerI(msg = "hook ${this.instanceClass.simpleName} ${this.method.name}")
                        val message = args[2] as String
                        loggerD(msg = "onJsAlert message = $message")
                        if (message == JS_FUCKED_MESSAGE) {
                            var times = appPref.getInt(APP_PREF_STAT_TIMES_KEY, 0)
                            times++
                            appPref.edit().putInt(APP_PREF_STAT_TIMES_KEY, times).apply()
                            val jsResultWrapper = args[3]!!
                            val jsResult = jsResultWrapper.javaClass.field {
                                type("android.webkit.JsResult")
                            }.get(jsResultWrapper).self as JsResult
                            loggerD(msg = "jsResult = $jsResult")
                            jsResult.confirm()
                            resultTrue()
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val APP_PREF_NAME = "fuck_zhihu_blind_watermark"
        private const val APP_PREF_STAT_TIMES_KEY = "stat_times"
        private const val JS_FUCKED_MESSAGE = "Fucked!"

        private fun PackageParam.getSharedPreferences(name: String): SharedPreferences {
            return appContext.getSharedPreferences(name, MODE_PRIVATE)
        }

        private val PackageParam.appPref: SharedPreferences
            get() = getSharedPreferences(APP_PREF_NAME)
    }
}