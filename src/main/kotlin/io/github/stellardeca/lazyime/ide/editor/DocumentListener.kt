package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import io.github.stellardeca.lazyime.core.lib.GrammarMode
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Server

class DocumentListener(
    private val project: Project,
    private val editor: Editor,
) : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        val doc: Document = event.document
        // 处理文本变更
        TaskMgr.submit("DocumentListener") {
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
