package com.huanchengfly.fuck.zhihu.watermark.hook

import com.highcapable.yukihookapi.annotation.xposed.InjectYukiHookWithXposed
import com.highcapable.yukihookapi.hook.factory.configs
import com.highcapable.yukihookapi.hook.factory.encase
import com.highcapable.yukihookapi.hook.factory.method
import com.highcapable.yukihookapi.hook.log.loggerD
import com.highcapable.yukihookapi.hook.log.loggerI
import com.highcapable.yukihookapi.hook.type.java.UnitType
import com.highcapable.yukihookapi.hook.xposed.proxy.IYukiHookXposedInit
import com.huanchengfly.fuck.zhihu.watermark.DataConsts

@InjectYukiHookWithXposed(entryClassName = "FuckZhihuBlindWatermark")
class HookEntry : IYukiHookXposedInit {
    override fun onInit() = configs {}

    override fun onHook() = encase {
        loadApp(name = "com.zhihu.android") {
            val js = prefs.get(DataConsts.JS)
            loggerD(msg = js)
            ("com.zhihu.android.answer.module.content.appview.AnswerChromeClient").hook {
                injectMember {
                    method {
                        name = "onReceivedTitle"
                        paramCount(2)
                        superClass()
                    }
                    afterHook {
                        loggerI(msg = "hooked AnswerChromeClient onReceivedTitle")
                        val loadUrlMethod = method.parameters[0].type.method {
                            param(String::class.java)
                            returnType(UnitType)
                        }.get(args[0])
                        loggerI(msg = "find method $loadUrlMethod")
                        loadUrlMethod.call(js)
                        loggerI(msg = "called method $loadUrlMethod")
                    }
                }
            }
        }
    }
}