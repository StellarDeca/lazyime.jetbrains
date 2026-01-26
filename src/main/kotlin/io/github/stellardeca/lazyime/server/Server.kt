package io.github.stellardeca.lazyime.server

import io.github.stellardeca.lazyime.core.lib.*
import io.github.stellardeca.lazyime.core.rpc.*

/// Server 自定义错误类
sealed class RpcException(message: String) : RuntimeException(message)
class ConnectionException(message: String) : RpcException(message)  // 连接错误
class RemoteException(message: String) : RpcException(message)  // 服务器错误
class ProtocolException(message: String) : RpcException(message)  // json 协议错误

object Server {
    private var cid: Int = 0
    private lateinit var connection: Connection

    /// 初始化
    suspend fun init(port: Int) {
        connection = Connection(port)
        connection.connect()
        setCid()
    }

    /// Analyze 请求
    suspend fun analyze(code: String, lang: String, cursor: Cursor): GrammarMode {
        val req = analyze(cid, code, lang, cursor)
        val res = sendAndReceive(req)
        return if (res.success) {
            when (val r = res.result) {
                is AnalyzeResult -> r.grammar
                else -> throw ProtocolException("Expect AnalyzeResult, but got $res. Req: $req")
            }
        } else {
            throw RemoteException("Error request. Req: ${req.toJson()}, Error: ${res.error}")
        }
    }

    /// MethodOnly 请求
    suspend fun methodOnly(target: MethodMode): Boolean {
        val req = methodOnly(cid, target)
        val res = sendAndReceive(req)
        return if (res.success) {
            when (val r = res.result) {
                is MethodOnlyResult -> r.method == target
                else -> throw ProtocolException("Expect MethodOnlyResult, but got $res. Req: $req")
            }
        } else {
            throw RemoteException("Error request. Req: ${req.toJson()}, Error: ${res.error}")
        }
    }

    /// Switch 请求
    suspend fun switch(code: String, lang: String, cursor: Cursor): Pair<GrammarMode, MethodMode> {
        val req = switch(cid, code, lang, cursor)
        val res = sendAndReceive(req)
        return if (res.success) {
            when (val r = res.result) {
                is SwitchResult -> Pair(r.grammar, r.method)
                else -> throw ProtocolException("Expect SwitchResult, but got $res. Req: $req")
            }
        } else {
            throw RemoteException("Error request. Req: ${req.toJson()}, Error: ${res.error}")
        }
    }

    /// Exit 请求
    suspend fun exit(): Unit = withRetry {
        val req = exit(cid)
        when (val r = connection.sendMessage(req.toJson())) {
            is Result.Ok -> Unit
            is Result.Err -> throw ConnectionException("Failed to send server exit command. Req: $req")
        }
    }

    /// 自动重连 方法
    private suspend fun <T> withRetry(task: suspend () -> T): T {
        // 尝试执行业务函数 失败则尝试重连一次并重新执行任务
        return try {
            task()
        } catch (_: Exception) {
            connection.connect()
            task()
        }
    }

    /// 带有自动重连的 消息收发方法
    private suspend fun sendAndReceive(req: Request): ClientResponse = withRetry {
        when (val r = connection.sendMessage(req.toJson())) {
            is Result.Err -> throw ConnectionException(r.message)
            is Result.Ok -> {}
        }

        when (val r = connection.readMessage()) {
            is Result.Err -> throw ConnectionException(r.message)
            is Result.Ok -> {
                try {
                    decodeResponse(r.value)
                } catch (e: Exception) {
                    throw ProtocolException("Could not decode response. Req: $req, Error: $e")
                }
            }
        }
    }

    /// cid 初始方法
    private suspend fun setCid() {
        val req = analyze(cid, "", "Kotlin", Cursor(0, 0))
        val res = sendAndReceive(req)
        cid = res.cid
    }
}
