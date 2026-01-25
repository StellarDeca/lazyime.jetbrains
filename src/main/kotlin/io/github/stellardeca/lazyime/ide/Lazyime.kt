package io.github.stellardeca.lazyime.ide

import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Server
import io.github.stellardeca.lazyime.server.Process
import io.github.stellardeca.lazyime.server.ServerNotFoundException
import com.intellij.openapi.components.Service
import com.intellij.openapi.Disposable
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

@Service(Service.Level.PROJECT)
class LazyimeProjectService : Disposable {
    init {
        // 注册服务

        // 启动服务
        TaskMgr.submit {
            try {
                val port = Process.runServer()
                Server.init(port)
            } catch (e: Exception) {
                // 弹窗 通知 server 启动失败
                when (e) {
                    is ServerNotFoundException -> {
                        /// 通知用户 安装 server
                        notifyWarning("server.notfound", e)
                    }

                    else -> {
                        /// 通知用户 lazyime 插件启动失败
                        notifyWarning("lazyime.startFailed", e)
                    }
                }
            }
        }
    }

    override fun dispose() {
        TaskMgr.submit { Server.exit() }
    }
}
