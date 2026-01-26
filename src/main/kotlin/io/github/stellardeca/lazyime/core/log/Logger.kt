package io.github.stellardeca.lazyime.core.log

import com.intellij.openapi.diagnostic.Logger as Log

object Logger {
    private val logger = Log.getInstance("LazyimePlugin")

    /// 日记 级别方法
    fun trace(msg: String) = logger.trace(msg)
    fun debug(msg: String) = logger.debug(msg)
    fun info(msg: String) = logger.info(msg)
    fun warn(msg: String, t: Throwable? = null) = logger.warn(msg, t)
    fun error(msg: String, t: Throwable? = null) = logger.error(msg, t)
}
