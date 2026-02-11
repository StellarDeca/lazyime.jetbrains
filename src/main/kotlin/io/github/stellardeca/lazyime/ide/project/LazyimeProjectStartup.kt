package io.github.stellardeca.lazyime.ide.project

import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import io.github.stellardeca.lazyime.ide.LazyimeAppService

class LazyimeProjectStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        // 项目加载时 执行 lazyime app 初始化
        service<LazyimeAppService>()
    }
}