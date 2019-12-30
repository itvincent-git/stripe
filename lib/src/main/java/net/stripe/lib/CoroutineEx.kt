package net.stripe.lib

import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import java.util.concurrent.TimeUnit

/**
 * 协程相关扩展
 * @author zhongyongsheng
 */

/**
 * 协程异常时打印日志，外部可自定义[CoroutineExceptionHandler]
 */
var loggingExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    Log.e("CoroutineException", "Coroutine exception occurred. $context", throwable)
}

/**
 * 等待[Deferred.await]的结果，如果出现异常或出现超时，则返回为null
 * @param timeout 超时时长，默认为0，则不设置超时
 * @param unit 时长单位
 * @param finalBlock 无论正常还是异常都会执行的finally块
 */
suspend fun <T> Deferred<T>.awaitOrNull(
    timeout: Long = 0L,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    finalBlock: (() -> Unit)? = null
): T? {
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
        finalBlock?.invoke()
    }
}

/**
 * 执行全部的[CoroutineScope.launch]
 */
fun <T : CoroutineScope> T.launchAll(vararg args: suspend () -> Unit): List<Job> {
    return args.map { launch { it() } }
}