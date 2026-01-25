package io.github.stellardeca.lazyime.core.rpc

import io.github.stellardeca.lazyime.core.lib.*
import kotlinx.serialization.*
import kotlinx.serialization.json.*

fun decodeResponse(json: String): ClientResponse {
    return Json.decodeFromString<ClientResponse>(json)
}

/// 原始 json 解析器
/// 根据 success 字段解析为 ClientResponse 子类
object ClientResponseSerializer : JsonContentPolymorphicSerializer<ClientResponse>(ClientResponse::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ClientResponse> {
        val jsonObject = element.jsonObject
        // 核心逻辑：根据 success 字段的布尔值决定解析器
        return if (jsonObject["success"]?.jsonPrimitive?.booleanOrNull == true) {
            OkResponse.serializer()
        } else {
            ErrResponse.serializer()
        }
    }
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

@Serializable(with = ClientResponseSerializer::class)
sealed class ClientResponse {
    abstract val cid: Int
}

@Serializable
data class OkResponse(
    override val cid: Int,
    val success: Boolean = true, // 保持字段存在以匹配 JSON
    val result: ResponseResult
) : ClientResponse()

@Serializable
data class ErrResponse(
    override val cid: Int,
    val success: Boolean = false,
    val error: String
) : ClientResponse()

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
