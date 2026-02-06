package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import io.github.stellardeca.lazyime.core.lib.GrammarMode
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.ide.Global
import io.github.stellardeca.lazyime.server.Server
import kotlinx.coroutines.delay

/// 光标 事件监听
class CursorListener : CaretListener {
    /// 光标移动事件
    override fun caretPositionChanged(event: CaretEvent) {
        val editor = event.editor
        val project = editor.project ?: return
        val doc = editor.document

        // 只响应 主编辑器变化 与 活动编辑器
        if (!editor.contentComponent.hasFocus() || editor.editorKind != EditorKind.MAIN_EDITOR) {
            return
        }
        // 添加 输入状态检查 检查 ime 合成表状态
        val composition = editor.getUserData(COMPOSITION_KEY)

        // 在 ui 线程中 准备数据
        val code = doc.text
        val lang = getLanguage(project, doc)
        val cursor = getCursor(editor)

        TaskMgr.submit("CursorListener") {
            // 仅仅在 语法状态变化 并且 不存在 Ime 候选框下 切换状态
            val currentGrammar = Server.analyze(code, lang, cursor)
            val previousGrammar = Global.grammarMode
            Global.grammarMode = currentGrammar  // 更新状态

            if (currentGrammar == previousGrammar) {
                return@submit
            }
            // 延迟 10 ms 以等待 Ime 候选窗口 信息同步
            delay(10)
            if (composition?.isComposing() != true) {
                val targetMethod = when (currentGrammar) {
                    GrammarMode.Code -> MethodMode.English
                    GrammarMode.Comment -> MethodMode.Native
                }
                try {
                    Server.methodOnly(targetMethod)
                    Global.methodMode = targetMethod
                } catch (e: Throwable) {
                    Global.methodMode = null
                    throw e
                }
            }
        }
    }
}