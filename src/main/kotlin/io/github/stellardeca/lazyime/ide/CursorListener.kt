package io.github.stellardeca.lazyime.ide

import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import io.github.stellardeca.lazyime.core.lib.*
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Server

/// 光标 事件监听
class CursorListener : CaretListener {
    /// 光标移动事件
    override fun caretPositionChanged(event: CaretEvent) {
        val editor = event.editor
        val project = editor.project ?: return
        val doc = editor.document

        TaskMgr.submit("CursorListener") {
            /// 仅仅在 grammar 变化时 对输入法进行切换
            val grammar = Server.analyze(
                doc.text,
                getLanguage(project, doc),
                getCursor(editor)
            )
            val method = when (grammar) {
                GrammarMode.Code -> MethodMode.English
                GrammarMode.Comment -> MethodMode.Native
            }
            Server.methodOnly(method)
        }
    }
}
