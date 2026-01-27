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
    val pos = editor.caretModel.primaryCaret.logicalPosition

    val row = pos.line
    val charColumn = pos.column

    // 获取当前行的 字符起始偏移
    // 计算光标在本行内的字符跨度
    val lineStartOffset = document.getLineStartOffset(row)
    val lineEndOffset = document.getLineEndOffset(row)
    val lengthInLine = charColumn.coerceAtMost(lineEndOffset - lineStartOffset)

    // 获取该行光标前的字符串
    val linePrefix = document.getText(
        TextRange(
            lineStartOffset,
            lineStartOffset + lengthInLine
        )
    )

    // 转为 UTF-8 字节数组并获取大小
    val byteColumn = linePrefix.toByteArray(Charsets.UTF_8).size
    return Cursor(row, byteColumn)
}
