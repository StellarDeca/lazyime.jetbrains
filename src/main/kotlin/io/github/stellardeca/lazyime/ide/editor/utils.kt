package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import io.github.stellardeca.lazyime.core.lib.*

/// 获取编辑的文件类型 不在编辑窗口中时为null
fun getLanguage(project: Project, document: Document): String {
    val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document) ?: return ""
    return psiFile.language.id
}

/// 获取编辑器 光标 不支持多光标
fun getCursor(editor: Editor): Cursor {
    val document = editor.document
    val caret = editor.caretModel.primaryCaret

    val offset = caret.offset
    val line = document.getLineNumber(offset)
    val lineStartOffset = document.getLineStartOffset(line)
    val linePrefix = document.getText(
        TextRange(lineStartOffset, offset)
    )

    val column = linePrefix.toByteArray(io.ktor.utils.io.charsets.Charsets.UTF_8).size
    return Cursor(line, column)
}
