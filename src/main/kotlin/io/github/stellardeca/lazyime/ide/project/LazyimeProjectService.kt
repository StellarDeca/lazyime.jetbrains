package io.github.stellardeca.lazyime.ide.project

import com.intellij.openapi.Disposable
import com.intellij.openapi.components.Service

@Service(Service.Level.PROJECT)
class LazyimeProjectService : Disposable {
    override fun dispose() {
        // 项目级别监听器 生命周期父类
    }
}
