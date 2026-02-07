package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseListener
import io.github.stellardeca.lazyime.core.lib.GrammarMode
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.ide.Global
import io.github.stellardeca.lazyime.server.Server

class MouseListener : EditorMouseListener {
    override fun mouseClicked(event: EditorMouseEvent) {
        val editor = event.editor
        val doc: Document = editor.document
        val project = editor.project ?: return

        // 只响应 主编辑器变化 与 活动编辑器
        if (!editor.contentComponent.hasFocus() || editor.editorKind != EditorKind.MAIN_EDITOR) {
            return
        }

        // 鼠标寺点击事件响应
        // 在 ui 线程中 准备数据
        val code = doc.text
        val lang = getLanguage(project, doc)
        val cursor = getCursor(editor)
        TaskMgr.submit("DocumentListener") {
            /// 仅仅在 grammar 变化时 对输入法进行切换
            val grammar = Server.analyze(code, lang, cursor)
            val method = when (grammar) {
                GrammarMode.Code -> MethodMode.English
                GrammarMode.Comment -> MethodMode.Native
            }
            try {
                Server.methodOnly(method)
                Global.grammarMode = grammar
                Global.methodMode = method
            } catch (e: Throwable) {
                Global.grammarMode = null
                Global.methodMode = null
                throw e
            }
        }
    }
}
