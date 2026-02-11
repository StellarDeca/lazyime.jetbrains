package io.github.stellardeca.lazyime.ide.focus

import com.intellij.openapi.application.ApplicationActivationListener
import com.intellij.openapi.wm.IdeFrame
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.ide.Global
import io.github.stellardeca.lazyime.server.Server
import io.github.stellardeca.lazyime.ide.settings.SettingsState

/// IDE 窗口焦点监听
/// 对与 windows、
class FocusListener : ApplicationActivationListener {
    override fun applicationActivated(ideFrame: IdeFrame) {
        // ide 获得焦点 时尝试恢复到指定输入法
        val settings = SettingsState.instance
        clearGlobalFlags()
        TaskMgr.submit("IdeFocusListener") {
            settings.ideFocusGainedMethod?.let {
                Server.methodOnly(it)
            }
        }
    }

    override fun applicationDeactivated(ideFrame: IdeFrame) {
        // ide 失去焦点 时尝试 恢复到 指定输入法
        val settings = SettingsState.instance
        clearGlobalFlags()
        TaskMgr.submit("IdeFocusListener") {
            settings.ideFocusLoseMethod?.let {
                Server.methodOnly(it)
            }
        }
    }

    private fun clearGlobalFlags() {
        Global.grammarMode = null
        Global.methodMode = null
    }
}
