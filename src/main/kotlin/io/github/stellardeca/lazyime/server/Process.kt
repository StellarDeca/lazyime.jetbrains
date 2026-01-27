package io.github.stellardeca.lazyime.server

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.progress.runBackgroundableTask
import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.file.Path
import com.intellij.util.io.HttpRequests
import com.intellij.util.io.ZipUtil
import com.intellij.util.io.delete
import org.jetbrains.intellij.build.dependencies.BuildDependenciesUtil.extractTarGz
import org.jetbrains.kotlinx.dataframe.api.toPath
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.resolve

private const val PLUGIN_ID = "io.github.stellardeca.lazyime"
private const val SERVER_NAME = "LazyInputSwitcher"

class ServerStartException(message: String) : RuntimeException(message)
class ServerNotFoundException(message: String) : RuntimeException(message)

object Process {

    suspend fun runServer(): Int = withContext(Dispatchers.IO) {
        val path = findServer()

        val process = try {
            ProcessBuilder(path.toString())
                .redirectErrorStream(true)
                .start()
        } catch (e: Exception) {
            throw ServerStartException("$e")
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

    suspend fun installServer(): Unit = withContext(Dispatchers.IO) {
        val name = when {
            SystemInfo.isWindows -> "windows-x86_64.zip"
            SystemInfo.isMac && SystemInfo.isAarch64 -> "macos-arm64.tar.gz"
            SystemInfo.isMac -> "macos-intel-x86_64.tar.gz"
            else -> "linux-x86_64.tar.gz"
        }
        "https://github.com/StellarDeca/LazyInputSwitcher/releases/latest/download/$name"
        val binDir = getServerPath().parent
        binDir.createDirectories()

        // 下载可执行文件并解压缩
        val tempFile = Files.createTempFile("server-download", name)
        if (name.endsWith(".zip")) {
            ZipUtil.extract(tempFile.toAbsolutePath(), binDir, null)
        } else {
            // .tar.gz 需要使用不同的处理方式（见下文提示）
            extractTarGz(tempFile, binDir, true)
        }
        // 清理临时文件并赋予可执行权限
        tempFile.delete()
        if (!SystemInfo.isWindows) {
            getServerPath().toFile().setExecutable(true)
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
