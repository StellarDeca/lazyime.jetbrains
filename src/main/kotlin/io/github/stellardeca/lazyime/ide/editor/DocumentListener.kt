package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorKind
import io.github.stellardeca.lazyime.core.lib.GrammarMode
import io.github.stellardeca.lazyime.core.lib.MethodMode
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.ide.Global
import io.github.stellardeca.lazyime.server.Server
import kotlinx.coroutines.delay

class DocumentListener(
    private val project: Project,
    private val editor: Editor,
) : DocumentListener {
    override fun documentChanged(event: DocumentEvent) {
        val doc: Document = event.document

        // 只响应 主编辑器变化 与 活动编辑器
        if (!editor.contentComponent.hasFocus() || editor.editorKind != EditorKind.MAIN_EDITOR) {
            return
        }
        // 添加 输入状态检查 如果存在 ime 合成表则 不做出切换
        val composition = editor.getUserData(COMPOSITION_KEY)

        // 处理文本变更
        // 在 ui 线程中 准备数据
        val code = doc.text
        val lang = getLanguage(project, doc)
        val cursor = getCursor(editor)
        TaskMgr.submit("CursorListener") {
            // 仅仅在 语法状态变化 并且 不存在 Ime 候选框下 切换状态
            val currentGrammar = try {
                Server.analyze(code, lang, cursor)
            } catch (e: Exception) {
                Global.grammarMode = null
                Global.methodMode = null
                throw e
            }
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
                    Global.grammarMode = null
                    Global.methodMode = null
                    throw e
                }
            }
        }
    }
}
