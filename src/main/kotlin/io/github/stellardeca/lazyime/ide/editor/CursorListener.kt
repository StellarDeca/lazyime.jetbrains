package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import io.github.stellardeca.lazyime.core.lib.GrammarMode
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Server

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

        // 在 ui 线程中 准备数据
        val code = doc.text
        val lang = getLanguage(project, doc)
        val cursor = getCursor(editor)

        TaskMgr.submit("CursorListener") {
            /// 仅仅在 grammar 变化时 对输入法进行切换
            val grammar = Server.analyze(code, lang, cursor)
            val method = when (grammar) {
                GrammarMode.Code -> MethodMode.English
                GrammarMode.Comment -> MethodMode.Native
            }
            Server.methodOnly(method)
        }
    }
}