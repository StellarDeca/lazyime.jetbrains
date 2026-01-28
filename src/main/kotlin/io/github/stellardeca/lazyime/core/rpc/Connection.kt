package io.github.stellardeca.lazyime.core.rpc

import io.github.stellardeca.lazyime.core.lib.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.InetSocketAddress
import java.net.Socket

class Connection(
    private val port: Int
) {
    private lateinit var socket: Socket
    private lateinit var input: DataInputStream
    private lateinit var output: DataOutputStream

    /// socket 连接方法
    suspend fun connect(): Result<Unit> = withContext(Dispatchers.IO) {
        close()
        val s = Socket()
        try {
            s.connect(InetSocketAddress("localhost", port), 3_000)
            s.tcpNoDelay = true
            s.keepAlive = true

            socket = s
            input = DataInputStream(BufferedInputStream(s.getInputStream()))
            output = DataOutputStream(BufferedOutputStream(s.getOutputStream()))
            Result.Ok(Unit)
        } catch (_: Exception) {
            close()
            Result.Err("Failed to connect localhost:$port")
        }
    }

    suspend fun sendMessage(message: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val bytes = message.toByteArray(Charsets.UTF_8)
            output.writeLong(bytes.size.toLong())
            output.write(bytes)
            output.flush()
            Result.Ok(Unit)
        } catch (e: Exception) {
            close()
            Result.Err("Failed to send message: $message, $e")
        }
    }

    suspend fun readMessage(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val length = input.readLong()
            val bytes = ByteArray(length.toInt())
            input.readFully(bytes)
            Result.Ok(String(bytes, Charsets.UTF_8))
        } catch (e: Exception) {
            close()
            Result.Err("Failed to send message: $e")
        }
    }

    private fun close() {
        try {
            input.close()
        } catch (_: Exception) {
        } // 不做处理
        try {
            output.close()
        } catch (_: Exception) {
        } // 不做处理
        try {
            socket.close()
        } catch (_: Exception) {
        } // 不做处理
    }
}
