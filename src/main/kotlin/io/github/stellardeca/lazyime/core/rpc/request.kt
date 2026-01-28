package io.github.stellardeca.lazyime.core.rpc

import kotlinx.serialization.encodeToString  // Must import encodeToString function
import io.github.stellardeca.lazyime.core.lib.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

fun analyze(cid: Int, code: String, language: String, cursor: Cursor): Request = Request(
    cid,
    CommandMode.Analyze,
    AnalyzeParams(code, language, cursor)
)

fun methodOnly(cid: Int, target: MethodMode): Request = Request(
    cid,
    CommandMode.MethodOnly,
    MethodOnlyParams(target)
)

fun switch(cid: Int, code: String, language: String, cursor: Cursor): Request = Request(
    cid,
    CommandMode.Switch,
    SwitchParams(code, language, cursor)
)

fun exit(cid: Int): Request = Request(
    cid,
    CommandMode.Exit,
    EmptyParams()
)

// 序列化默认值
private val JsonWithDefaults = Json {
    encodeDefaults = true
}

@Serializable
sealed interface Params

@Serializable
enum class CommandMode {
    @SerialName("Analyze")
    Analyze,

    @SerialName("Switch")
    Switch,

    @SerialName("MethodOnly")
    MethodOnly,

    @SerialName("Exit")
    Exit,
}

@Serializable
class Request(
    val cid: Int,
    val command: CommandMode,
    val params: Params
) {
    fun toJson(): String = JsonWithDefaults.encodeToString(this)
}

@Serializable
class AnalyzeParams(
    val code: String,
    val language: String,
    val cursor: Cursor
) : Params

@Serializable
class MethodOnlyParams(
    val mode: MethodMode
) : Params

@Serializable
class SwitchParams(
    val code: String,
    val language: String,
    val cursor: Cursor
) : Params

@Serializable
class EmptyParams : Params
