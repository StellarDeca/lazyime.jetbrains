package io.github.stellardeca.lazyime.ide

import com.intellij.notification.NotificationGroupManager
import com.intellij.notification.NotificationType
import io.github.stellardeca.lazyime.core.task.TaskMgr
import io.github.stellardeca.lazyime.server.Server
import io.github.stellardeca.lazyime.server.Process
import io.github.stellardeca.lazyime.server.ServerNotFoundException
import com.intellij.openapi.components.Service
import com.intellij.openapi.Disposable
import io.github.stellardeca.lazyime.core.log.Logger
import io.github.stellardeca.lazyime.ide.settings.Language
import kotlinx.coroutines.runBlocking

@Service(Service.Level.PROJECT)
class LazyimeProjectService : Disposable {
    private val notificationGroupId = "io.github.stellardeca.lazyime.jetbrains.notification"

    init {
        // 启动服务
        TaskMgr.submit("LazyimeProjectInit") {
            try {
                val port = Process.runServer()
                Server.init(port)
                Logger.info("LazyimeProject init, server port: $port")
            } catch (e: Exception) {
                when (e) {
                    /// 通知用户 安装 server
                    is ServerNotFoundException -> {
                        notifyInfo("lazyime.server.notfound")
                        Logger.warn("lazyime.server not found", e)
                    }
                    /// 通知用户 lazyime 插件启动失败
                    else -> {
                        notifyInfo("lazyime.startFailed", e)
                        Logger.error("lazyime.startFailed", e)
                    }
                }
                try {
                    Server.exit()
                } catch (_: Exception) {
                }
                TaskMgr.shutdown()
            }
        }
    }

    override fun dispose() = runBlocking {
        TaskMgr.submit("LazyimeProjectExit") { Server.exit() }
    }

    /// Info 弹窗 通知
    private fun notifyInfo(key: String, vararg params: Any?) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup(notificationGroupId)
            .createNotification(Language.message(key, params), NotificationType.INFORMATION)
            .notify(null)
    }
}
