package io.github.stellardeca.lazyime.ide.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.*
import com.intellij.util.xmlb.XmlSerializerUtil
import io.github.stellardeca.lazyime.core.lib.MethodMode

/// 设为 Application 级别
/// 设置全局通用
@State(name = "io.github.stellardeca.lazyime.ide.SettingsState", storages = [Storage("lazyime.xml")])
class SettingsState : PersistentStateComponent<SettingsState> {

    // 保存的设置字段
    /// IDE 工具窗口 默认输入法 设置
    var runWindowMethod: MethodMode = MethodMode.English
    var projectWindowMethod: MethodMode = MethodMode.English
    var commitWindowMethod: MethodMode = MethodMode.English
    var terminalWindowMethod: MethodMode = MethodMode.English

    /// IDE 焦点行为设置
    var ideFocusGainedMethod: MethodMode? = null
    var ideFocusLoseMethod: MethodMode? = MethodMode.Native

    /// 获取 设置 插件加载时 Ide 自动调用
    override fun getState(): SettingsState = this

    /// 加载设置 插件加载时 Ide 自动调用
    override fun loadState(state: SettingsState) {
        XmlSerializerUtil.copyBean(state, this)
    }

    /// 获取实例 静态方法
    companion object {
        val instance: SettingsState
            get() = ApplicationManager.getApplication().getService(SettingsState::class.java)
    }
}
