package io.github.stellardeca.lazyime.ide

import io.github.stellardeca.lazyime.core.lib.*

/// 存储一些全局状态
/// 生命周期 绑定到 lazyime
object Global {
    var grammarMode: GrammarMode? = null
    var methodMode: MethodMode = MethodMode.English
}
