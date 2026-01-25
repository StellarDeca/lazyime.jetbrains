package io.github.stellardeca.lazyime.core.lib

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class MethodMode {
    @SerialName("English")
    English,

    @SerialName("Native")
    Native
}

@Serializable
enum class GrammarMode {
    @SerialName("Code")
    Code,

    @SerialName("Comment")
    Comment
}

@Serializable
data class Cursor(
    val row: Int,  // 0基 行号
    val column: Int  // 0基 行内 utf8 字节偏移量
)

sealed class Result<out T> {

    data class Ok<out T>(val value: T) : Result<T>()

    data class Err(val message: String) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> =
        when (this) {
            is Ok -> Ok(transform(value))
            is Err -> this
        }

    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> =
        when (this) {
            is Ok -> transform(value)
            is Err -> this
        }

    inline fun onFailure(block: (Err) -> Unit): Result<T> {
        if (this is Err) block(this)
        return this
    }

    inline fun onSuccess(block: (T) -> Unit): Result<T> {
        if (this is Ok) block(value)
        return this
    }
}
