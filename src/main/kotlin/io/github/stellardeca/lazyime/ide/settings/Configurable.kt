package io.github.stellardeca.lazyime.ide.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Process
import io.github.stellardeca.lazyime.server.Server
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JLabel

class Configurable : SearchableConfigurable {

    /// 持有 UI 组件 的引用
    private var serverLabel: JLabel? = null
    private var serverButton: JButton? = null

    /// 设置类实例
    private val settings = SettingsState.instance

    /// 设置面板实例
    private var settingsPanel: com.intellij.openapi.ui.DialogPanel? = null

    /// 设置在 IDE 树形列表中的显示名称
    override fun getDisplayName(): String = Language.message("settings.display.tittle")

    /// 唯一 Id
    override fun getId(): String = "io.github.stellardeca.lazyime.settings"

    /// 创建 UI 界面
    override fun createComponent(): JComponent {
        settingsPanel = panel {
            group(Language.message("settings.group.window_modes")) {

                val rows = listOf(
                    "settings.window.run" to settings::runWindowMethod,
                    "settings.window.project" to settings::projectWindowMethod,
                    "settings.window.commit" to settings::commitWindowMethod,
                    "settings.window.terminal" to settings::terminalWindowMethod
                )

                for ((labelKey, prop) in rows) {
                    row(Language.message(labelKey)) {
                        comboBox(
                            items = MethodMode.entries,
                            renderer = SimpleListCellRenderer.create("") { mode ->
                                when (mode) {
                                    MethodMode.Native -> Language.message("method.native")
                                    MethodMode.English -> Language.message("method.english")
                                }
                            }
                        ).bindItem(
                            getter = { prop.get() },
                            setter = { it?.let { value -> prop.set(value) } }
                        )
                    }
                }
            }

            group(Language.message("settings.group.server")) {
                row {
                    label(getServerStatusText()).applyToComponent { serverLabel = this }
                    button(getServerButtonText()) {
                        startInstallServer()
                    }.applyToComponent { serverButton = this }
                }
            }
        }
        return settingsPanel!!
    }

    /// 判断用户是否修改了设置
    override fun isModified(): Boolean = settingsPanel?.isModified() ?: false

    /// 用户点击 OK 或 Apply 时调用
    override fun apply() {
        settingsPanel?.apply()
    }

    /// 重置界面显示的值
    override fun reset() {
        settingsPanel?.reset()
    }

    /// 控制面板销毁方法
    override fun disposeUIResources() {
        settingsPanel = null
    }

    private fun startInstallServer() {
        val btn = serverButton ?: return
        btn.isEnabled = false // 禁用按钮 防止多次点击
        // 创建 Task 对象 同时禁用其他设置更改
        val task = object : Task.Modal(null, Language.message("server.download.tittle"), true) {
            // 任务函数
            override fun run(indicator: ProgressIndicator) {
                // 设置进度条的不确定状态
                TaskMgr.submit("InstallServer") {
                    Server.exit()
                }
                indicator.isIndeterminate = true
                indicator.text = Language.message("server.download.start")
                Process.installServer(indicator)
            }

            // UI 线程 任务成功完成后自动调用
            override fun onSuccess() {
                // 恢复按钮状态
                btn.isEnabled = true
                // 更新 UI 文字
                serverLabel?.text = getServerStatusText()
                serverButton?.text = getServerButtonText()
                Messages.showInfoMessage(
                    Language.message("server.download.success"),
                    Language.message("server.download.tittle")
                )
            }

            // UI 线程 任务发生异常时自动调用
            override fun onThrowable(error: Throwable) {
                btn.isEnabled = true
                Messages.showErrorDialog(
                    Language.message("server.download.error", error.toString()),
                    Language.message("server.download.tittle")
                )
            }
        }
        // 提交给 ProgressManager 执行
        ProgressManager.getInstance().run(task)
    }

    private fun getServerStatusText(): String {
        return try {
            Process.findServer()
            Language.message("settings.server.install")
        } catch (_: Exception) {
            Language.message("settings.server.uninstall")
        }
    }

    private fun getServerButtonText(): String {
        return try {
            Process.findServer()
            Language.message("settings.server.button.reinstall")
        } catch (_: Exception) {
            Language.message("settings.server.button.install")
        }
    }
}
