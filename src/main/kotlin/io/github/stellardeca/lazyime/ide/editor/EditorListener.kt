package io.github.stellardeca.lazyime.ide.editor

import io.github.stellardeca.lazyime.ide.LazyimeProjectService
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener

class EditorListener : EditorFactoryListener {
    /// editor 创建时 挂载 editor 相关监听器
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return

        // 实例化 service
        val service = project.getService(LazyimeProjectService::class.java)

        // 必须是主编辑器 有文件 有项目
        // 才挂载监听器
        if (event.editor.editorKind == EditorKind.MAIN_EDITOR) {
            // 将 CursorListener 直接挂在当前 editor 的 caretModel 上
            // 绑定 project 生命周期
            editor.caretModel.addCaretListener(CursorListener(), service)
            editor.document.addDocumentListener(DocumentListener(project, editor), service)
        }
    }
}
