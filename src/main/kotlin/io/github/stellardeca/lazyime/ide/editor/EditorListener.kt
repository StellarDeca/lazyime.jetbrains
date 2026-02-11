package io.github.stellardeca.lazyime.ide.editor

import io.github.stellardeca.lazyime.ide.project.LazyimeProjectService
import com.intellij.openapi.editor.EditorKind
import com.intellij.openapi.editor.event.EditorFactoryEvent
import com.intellij.openapi.editor.event.EditorFactoryListener
import com.intellij.openapi.util.Key

// 定义一个唯一的 Key 用来存储监听器
val COMPOSITION_KEY = Key.create<CompositionListener>("LazyIme.CompositionListener")

class EditorListener : EditorFactoryListener {
    /// editor 创建时 挂载 editor 相关监听器
    override fun editorCreated(event: EditorFactoryEvent) {
        val editor = event.editor
        val project = editor.project ?: return

        // 获取 项目级别服务实例
        val service = project.getService(LazyimeProjectService::class.java)

        // 必须是主编辑器 有文件 有项目
        // 才挂载监听器
        if (event.editor.editorKind == EditorKind.MAIN_EDITOR) {
            // 挂载 ime 监听器
            editor.putUserData(COMPOSITION_KEY, CompositionListener(editor))

            // 将 CursorListener 直接挂在当前 editor 的 caretModel 上
            // 绑定 project 生命周期
            editor.caretModel.addCaretListener(CursorListener(), service)
            editor.document.addDocumentListener(DocumentListener(project, editor), service)
            editor.addEditorMouseListener(MouseListener(), service)
        }
    }

    override fun editorReleased(event: EditorFactoryEvent) {
        // 卸载 ime 监听器
        val editor = event.editor
        editor.putUserData(COMPOSITION_KEY, null)
    }
}
