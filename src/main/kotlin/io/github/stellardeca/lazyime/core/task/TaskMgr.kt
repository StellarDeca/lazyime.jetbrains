package io.github.stellardeca.lazyime.core.task

import io.github.stellardeca.lazyime.core.log.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.coroutines.cancellation.CancellationException

/// 任务调度器 确保任务顺序执行
object TaskMgr {
    /// 任务管理器声明周期标志
    private var running = true

    /// 协程环境 + 允许子任务崩溃 + 单线程单实例运行
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO.limitedParallelism(1))

    /// 使用 LinkedHashMap
    /// 数据顺序存储并可以 根据 hash map 快速查找
    private val pendingTasks = LinkedHashMap<TaskType, suspend () -> Unit>()
    private val mutex = Mutex()

    /// 使用 Channel 仅作为新任务的信号
    /// Channel.CONFLATED 保证信号 只用最新的
    private val signal = Channel<Unit>(Channel.CONFLATED)

    init {
        // 在初始化时启动唯一的 长驻的任务监听协程
        scope.launch { workerLoop() }
    }

    /// 关闭任务执行器
    fun shutdown() {
        // 不再接受新任务并关闭 worker
        running = false
        scope.launch {
            mutex.withLock { pendingTasks.clear() }
        }
        scope.cancel(CancellationException("TaskMgr shutdown"))
        signal.close()
    }

    /// 任务提交方法
    // 直接在 ui 线程中可以直接调用
    fun submit(name: String, task: suspend () -> Unit) {
        // 在协程 线程中进行 任务 提交操作
        // 防止 worker 与 submit 静态
        if (!running) return
        scope.launch {
            mutex.withLock {
                // 如果任务已存在，
                // 先删除旧的 再存入新的以保证在队尾
                val type = TaskType(name)
                pendingTasks.remove(type)
                pendingTasks[type] = task
            }
            // 发送信号 通知 worker 开始工作
            signal.trySend(Unit)
        }
    }

    private suspend fun workerLoop() {
        // 无信号时自动挂起
        for (notification in signal) {
            while (true) {
                // 获取任务
                val task = mutex.withLock {
                    val firstKey = pendingTasks.keys.firstOrNull() ?: return@withLock null
                    pendingTasks.remove(firstKey)
                } ?: break // 队列空了，退出内部循环，等待 Channel 下一个信号

                // 执行任务 不阻塞入队
                try {
                    task()
                } catch (e: CancellationException) {
                    throw e  // 协程 取消信号 中断后续任务执行
                } catch (e: Throwable) {
                    handleError(e)  // 任务 错误处理函数
                }
            }
        }
    }

    private fun handleError(e: Throwable) {
        // 仅作日志记录
        Logger.warn("Error with task", e)
    }
}

/// 任务类型 数据类
data class TaskType(val name: String)
