package net.stripe.lib

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleObserver
import android.arch.lifecycle.LifecycleOwner
import android.arch.lifecycle.OnLifecycleEvent
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
 * 全局App生命周期的Scope，替代GlobalScope
 */
val AppScope = GlobalScope + LoggingExceptionHandler

/**
 * 协程支持生命周期
 * Created by zhongyongsheng on 2018/9/20.
 */
class CoroutineLifecycle {
    var mLastEvent: Lifecycle.Event = Lifecycle.Event.ON_ANY
    var mTargetEvent: Lifecycle.Event = Lifecycle.Event.ON_ANY

    fun <T> observe(lifecycleOwner: LifecycleOwner, deferred: Deferred<T>) {
        lifecycleOwner.lifecycle.addObserver(object: LifecycleObserver {
            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            fun onStateChanged(owner:LifecycleOwner, event: Lifecycle.Event) {
                if (mLastEvent == Lifecycle.Event.ON_ANY) {
                    mLastEvent = event
                    when (mLastEvent) {
                        Lifecycle.Event.ON_CREATE -> mTargetEvent = Lifecycle.Event.ON_DESTROY
                        Lifecycle.Event.ON_START -> mTargetEvent = Lifecycle.Event.ON_STOP
                        Lifecycle.Event.ON_RESUME -> mTargetEvent = Lifecycle.Event.ON_PAUSE
                    }
                }
                if (event == mTargetEvent) {
                    //cancel the coroutine
                    deferred.cancel()
                }
                if (owner.lifecycle.currentState == Lifecycle.Event.ON_DESTROY) {
                    owner.lifecycle.removeObserver(this)
                    return
                }
            }
        })
    }
}

/**
 * 执行异步任务并绑定生命周期
 */
fun <T, S: CoroutineScope> S.asyncWithLifecycle(lifecycleOwner: LifecycleOwner,
                                                context: CoroutineContext = EmptyCoroutineContext,
                                                start: CoroutineStart = CoroutineStart.DEFAULT,
                                                block: suspend CoroutineScope.() -> T): Deferred<T> {
    val job = AppScope.async(context = context, start = start, block = block)
    CoroutineLifecycle().observe(lifecycleOwner, job)
    return job
}

/**
 * 为协程block绑定lifecycle生命周期
 */
inline fun <T, S: CoroutineScope> S.bindLifecycle(lifecycleOwner: LifecycleOwner,
                                                  block: CoroutineScope.() -> Deferred<T>): Deferred<T> {
    val job = block.invoke(this)
    CoroutineLifecycle().observe(lifecycleOwner, job)
    return job
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