package io.github.stellardeca.lazyime.server

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.util.SystemInfo
import com.intellij.util.io.Decompressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.file.Path
import com.intellij.util.io.HttpRequests
import java.nio.file.Files
import kotlin.concurrent.thread

private const val PLUGIN_ID = "io.github.StellarDeca.lazyime.jetbrains"
private const val SERVER_NAME = "LazyInputSwitcher"

class ServerStartException(message: String) : RuntimeException(message)
class ServerNotFoundException(message: String) : RuntimeException(message)

object Process {

    suspend fun runServer(stderr: (String) -> Unit): Int = withContext(Dispatchers.IO) {
        val path = findServer()

        val process = try {
            // 分开处理 stdout 与 stderr
            ProcessBuilder(path.toString()).start()
        } catch (e: Exception) {
            throw ServerStartException("$e")
        }

        /// 启动 额外线程监听 stderr 用于捕获 server 错误
        thread(start = true, isDaemon = true, name = "LazyIME-Stderr-Watcher") {
            try {
                process.errorStream.bufferedReader().use { reader ->
                    val err = reader.readText()
                    stderr(err)
                }
            } catch (_: Exception) {
                // 进程关闭时流断开是正常的，忽略异常
            }
        }

        val reader = process.inputStream.bufferedReader()
        val port = withTimeoutOrNull(5_000) {
            reader.readLine()?.trim()?.toIntOrNull()
        } ?: run {
            process.destroyForcibly()
            throw ServerStartException("Server did not output port within timeout")
        }
        return@withContext port
    }

    fun findServer(): Path {
        val serverPath = getServerPath()
        if (!serverPath.toFile().exists()) {
            throw ServerNotFoundException("Server $serverPath not found")
        }
        return serverPath
    }

    fun installServer(indicator: ProgressIndicator?) {
        val name = when {
            SystemInfo.isWindows -> "windows-x86_64.zip"
            SystemInfo.isMac && SystemInfo.isAarch64 -> "macos-arm64.tar.gz"
            SystemInfo.isMac && !SystemInfo.isAarch64 -> "macos-intel-x86_64.tar.gz"
            else -> "linux-x86_64.tar.gz"
        }
        val url = "https://github.com/StellarDeca/LazyInputSwitcher/releases/latest/download/$name"
        val targetPath = getServerPath()
        val binDir = targetPath.parent
        if (!Files.exists(binDir)) Files.createDirectories(binDir)
        // 下载可执行文件并解压缩
        // 同时配置 进度
        val tempFile = Files.createTempFile("lazyime-server", name)

        try {
            HttpRequests.request(url).saveToFile(tempFile, indicator)
            if (name.endsWith(".zip")) {
                Decompressor.Zip(tempFile).extract(binDir)
            } else {
                Decompressor.Tar(tempFile).extract(binDir)
            }

            // 设置 文件权限
            if (!SystemInfo.isWindows) {
                val file = targetPath.toFile()
                if (file.exists()) {
                    file.setExecutable(true)
                }
            }
        } finally {
            // 清理临时文件
            Files.deleteIfExists(tempFile)
        }
    }

    private fun getServerPath(): Path {
        val pluginId = PluginId.getId(PLUGIN_ID)
        val pluginDescriptor =
            PluginManagerCore.getPlugin(pluginId) ?: throw ServerStartException("Plugin $pluginId not found")
        val pluginPath = pluginDescriptor.pluginPath
        val fileName = if (SystemInfo.isWindows) "$SERVER_NAME.exe" else SERVER_NAME
        return pluginPath.resolve("bin").resolve(fileName)
    }
}
