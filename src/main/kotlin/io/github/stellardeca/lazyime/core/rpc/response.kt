package io.github.stellardeca.lazyime.core.rpc

import io.github.stellardeca.lazyime.core.lib.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun decodeResponse(json: String): ClientResponse {
    return Json.decodeFromString<ClientResponse>(json)
}

/// result 字段解析器
/// 根据 result 字段内容 自动解析 result 字段到 ResponseResult 子类
object ResponseResultSerializer : JsonContentPolymorphicSerializer<ResponseResult>(ResponseResult::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ResponseResult> {
        val keys = element.jsonObject.keys
        return when {
            keys.contains("grammar") && keys.contains("method") -> SwitchResult.serializer()
            keys.contains("grammar") -> AnalyzeResult.serializer()
            keys.contains("method") -> MethodOnlyResult.serializer()
            else -> throw SerializationException("Unknown result type: $keys")
        }
    }
}

@Serializable
data class ClientResponse(
    val cid: Int,
    val success: Boolean,
    val error: String?,
    val result: ResponseResult?
)

@Serializable(with = ResponseResultSerializer::class)
sealed interface ResponseResult

@Serializable
data class AnalyzeResult(
    val grammar: GrammarMode
) : ResponseResult

@Serializable
data class MethodOnlyResult(
    val method: MethodMode
) : ResponseResult

@Serializable
data class SwitchResult(
    val grammar: GrammarMode,
    val method: MethodMode
) : ResponseResult
