package io.github.stellardeca.lazyime.core.task

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/// 任务调度器 确保任务顺序执行
object TaskMgr {
    /// 协程环境 + 允许子任务崩溃 + 单线程单实例运行
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.IO.limitedParallelism(1)
    )

    fun submit(task: suspend () -> Unit) = scope.launch {
        task()
    }
}
