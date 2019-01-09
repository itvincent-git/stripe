package net.stripe.lib

import android.arch.lifecycle.*
import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * 协程相关扩展
 */

/**
 * 协程异常时打印日志
 */
val LoggingExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    Log.e("CoroutineException", "Coroutine exception occurred", throwable)
}

/**
 * 等待await的结果，如果出现异常或出现超时，则返回为null
 * @param timeout 超时时长，默认为0，则不设置超时
 * @param unit 时长单位
 * @param finalBlock 无论正常还是异常都会执行的finally块
 */
suspend fun <T> Deferred<T>.awaitOrNull(timeout: Long = 0L,
                                        unit: TimeUnit = TimeUnit.MILLISECONDS,
                                        finalBlock: () -> Unit): T? {
    return try {
        if (timeout > 0) {
            withTimeout(unit.toMillis(timeout)) {
                this@awaitOrNull.await()
            }
        } else {
            this.await()
        }
    } catch (e: Exception) {
        null
    } finally {
        finalBlock()
    }
}

/**
 * 执行全部的launch
 */
fun <T : CoroutineScope> T.launchAll(vararg args: suspend () -> Unit): List<Job> {
    return args.map { launch { it() } }
}

/**
 * 全局App生命周期的Scope，替代GlobalScope
 */
val appScope = GlobalScope + LoggingExceptionHandler


// ----------- lifecycleScope Start --------------
/**
 * 绑定在LifecycleOwner的coroutineScope，在Lifecycle onDestroy时，会把关联的任务全部停止
 */
inline val LifecycleOwner.lifecycleScope get() = lifecycle.lifecycleScope

/**
 * Lifecycle.createScope，创建cancelEvent发生时，会把关联的任务全部停止
 */
fun Lifecycle.createScope(cancelEvent: Lifecycle.Event): CoroutineScope {
    return CoroutineScope(createJob(cancelEvent) + Dispatchers.Default + LoggingExceptionHandler)
}

/**
 * 创建绑定生命周期的Job
 * @param cancelEvent 当收到该生命周期时取消Job，默认ON_DESTROY；forbiddenCancelEvents是不支持的类型
 */
fun Lifecycle.createJob(cancelEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY): Job {
    if(cancelEvent in forbiddenCancelEvents) {
        throw UnsupportedOperationException("$cancelEvent is forbidden for createJob(…).")
    }
    return Job().also { job ->
        if (currentState == Lifecycle.State.DESTROYED) job.cancel()
        else addObserver(object : GenericLifecycleObserver {
            override fun onStateChanged(source: LifecycleOwner?, event: Lifecycle.Event) {
                if (event == cancelEvent) {
                    removeObserver(this)
                    job.cancel()
                }
            }
        })
    }
}
//不支持取消的类型
private val forbiddenCancelEvents = arrayOf(
        Lifecycle.Event.ON_ANY,
        Lifecycle.Event.ON_CREATE,
        Lifecycle.Event.ON_START,
        Lifecycle.Event.ON_RESUME
)

//全局保存绑定了生命周期的jobs
private val lifecycleJobs = mutableMapOf<Lifecycle, Job>()

//返回当前Lifecycle已经绑定的job,如果已经存在则从缓存获取，否则创建一个
val Lifecycle.job: Job
    get() = lifecycleJobs[this] ?: createJob().also {
        if (it.isActive) {
            lifecycleJobs[this] = it
            it.invokeOnCompletion { _ -> lifecycleJobs -= this }
        }
    }

//全局保存绑定生命周期的CoroutineScope
private val lifecycleCoroutineScopes = mutableMapOf<Lifecycle, CoroutineScope>()

//返回当前Lifecycle绑定的CoroutineScope，使用Main线程，如果已经存在则从缓存获取，否则创建一个
val Lifecycle.lifecycleScope: CoroutineScope
    get() = lifecycleCoroutineScopes[this] ?: job.let { job ->
        val newScope = CoroutineScope(job + Dispatchers.Default + LoggingExceptionHandler)
        if (job.isActive) {
            lifecycleCoroutineScopes[this] = newScope
            job.invokeOnCompletion { _ -> lifecycleCoroutineScopes -= this }
        }
        newScope
    }
// ----------- lifecycleScope End--------------

//private val viewModelCoroutineScopes = mutableMapOf<ViewModel, CoroutineScope>()
//private val viewModelJobs = mutableMapOf<ViewModel, Job>()
//
//val ObserverableViewModel.job: Job
//    get() = viewModelJobs[this] ?: createJob().also {
//        if (it.isActive) {
//            viewModelJobs[this] = it
//            it.invokeOnCompletion { _ -> viewModelJobs -= this }
//        }
//    }
//
//val ObserverableViewModel.viewModelScope: CoroutineScope
//    get() = viewModelCoroutineScopes[this] ?: job.let { job ->
//        val newScope = CoroutineScope(job + Dispatchers.Default + LoggingExceptionHandler)
//        if (job.isActive) {
//            viewModelCoroutineScopes[this] = newScope
//            job.invokeOnCompletion { _ -> lifecycleCoroutineScopes -= this }
//        }
//        newScope
//    }
