package io.github.stellardeca.lazyime.ide.settings

import com.intellij.AbstractBundle
import org.jetbrains.annotations.PropertyKey

private const val BUNDLE = "messages.MyBundle"

/// 多语言 适配器
object Language : AbstractBundle(BUNDLE) {
    /// 加载 消息
    /// @PropertyKey 在编译时检查 key 存在性
    fun message(@PropertyKey(resourceBundle = BUNDLE) key: String, vararg params: Any): String {
        return getMessage(key, *params)
    }
}
