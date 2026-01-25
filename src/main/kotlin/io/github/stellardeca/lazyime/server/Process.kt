package io.github.stellardeca.lazyime.server

import com.intellij.ide.plugins.PluginManagerCore
import com.intellij.openapi.extensions.PluginId
import com.intellij.openapi.util.SystemInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.nio.file.Path

private const val PLUGIN_ID = "io.github.StellarDeca.lazyime"
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

    private fun findServer(): Path {
        val pluginId = PluginId.getId(PLUGIN_ID)
        val pluginDescriptor =
            PluginManagerCore.getPlugin(pluginId) ?: throw ServerStartException("Plugin $pluginId not found")
        val pluginPath = pluginDescriptor.pluginPath
        val fileName = if (SystemInfo.isWindows) "$SERVER_NAME.exe" else SERVER_NAME
        val serverPath = pluginPath.resolve("bin").resolve(fileName)

        if (!serverPath.toFile().exists()) {
            throw ServerNotFoundException("Server $serverPath not found")
        }
        return serverPath
    }
}
