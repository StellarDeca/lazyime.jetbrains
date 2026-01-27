package io.github.stellardeca.lazyime.ide.settings

import com.intellij.openapi.options.SearchableConfigurable
import com.intellij.ui.SimpleListCellRenderer
import com.intellij.ui.dsl.builder.bindItem
import com.intellij.ui.dsl.builder.panel
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Process
import javax.swing.JComponent

class Configurable : SearchableConfigurable {
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
                val status = try {
                    Process.findServer()
                    Language.message("settings.server.install")
                } catch (_: Exception) {
                    Language.message("settings.server.uninstall")
                }
                row {
                    label(status)
                    val text = if (status == Language.message("settings.server.install")) {
                        Language.message("settings.server.button.reinstall")
                    } else {
                        Language.message("settings.server.button.install")
                    }
                    button(text) {
                        TaskMgr.submit("InstallServer") { Process.installServer() }
                    }
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
}
