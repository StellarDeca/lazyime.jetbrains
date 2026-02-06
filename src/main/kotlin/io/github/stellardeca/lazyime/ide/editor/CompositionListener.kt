package io.github.stellardeca.lazyime.ide.editor

import com.intellij.openapi.editor.Editor
import java.awt.event.FocusEvent
import java.awt.event.FocusAdapter
import java.awt.event.InputMethodListener
import java.awt.event.InputMethodEvent
import java.util.concurrent.atomic.AtomicBoolean

/// 对 ime 输入 进行监听 检查 ime合成框的存在性
class CompositionListener(editor: Editor) {
    private val composing = AtomicBoolean(false)

    /// 对于 editor 挂载监听
    init {
        val comp = editor.contentComponent
        comp.addInputMethodListener(object : InputMethodListener {
            override fun inputMethodTextChanged(event: InputMethodEvent) {
                // event.text != null 且非空表示存在正在 composition 的文本
                val hasComposed = event.text != null && (event.text.endIndex - event.text.beginIndex) > 0
                composing.set(hasComposed)
            }

            override fun caretPositionChanged(event: InputMethodEvent?) {
                // empty method
            }
        })

        // focus lost 时清理状态
        comp.addFocusListener(object : FocusAdapter() {
            override fun focusLost(e: FocusEvent) {
                composing.set(false)
            }
        })
    }

    fun isComposing() = composing.get()
}
