package io.github.stellardeca.lazyime.ide.toolwindow

import io.github.stellardeca.lazyime.ide.LazyimeProjectService
import com.intellij.openapi.wm.ex.ToolWindowManagerListener
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowManager
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.ide.Global
import io.github.stellardeca.lazyime.ide.settings.SettingsState
import io.github.stellardeca.lazyime.server.Server

/// IDE 内部工具窗口变化监听
class ToolWindowsListener : ToolWindowManagerListener {
    override fun toolWindowShown(toolWindow: ToolWindow) {
        // window 初始化时 输入法切换到指定输入法
        // 获取 service 保证初始化完成
        val project = toolWindow.project
        project.getService(LazyimeProjectService::class.java)
        TaskMgr.submit("ToolWindowsListenerShown") {
            val target = getSetting(toolWindow.id)
            target?.let {
                try {
                    Server.methodOnly(it)
                    Global.methodMode = it
                } catch (e: Exception) {
                    Global.methodMode = null
                    throw e
                } finally {
                    Global.grammarMode = null
                }
            }
        }
    }

    override fun stateChanged(toolWindowManager: ToolWindowManager) {
        val activeId = toolWindowManager.activeToolWindowId
        TaskMgr.submit("ToolWindowsListenerShown") {
            val target = activeId?.let { getSetting(it) }
            target?.let { Server.methodOnly(it) }
        }
    }

    private fun getSetting(id: String): MethodMode? {
        val window = SupportWindows.fromString(id)
        val settings = SettingsState.instance
        return when (window) {
            null -> null
            SupportWindows.Run -> settings.runWindowMethod
            SupportWindows.Project -> settings.projectWindowMethod
            SupportWindows.Commit -> settings.commitWindowMethod
            SupportWindows.Terminal -> settings.terminalWindowMethod
        }
    }
}
