package net.kotlin.ex.lib

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Handler
import android.os.Looper
import java.io.InputStream
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * 任务相关，有只运行一次的任务
 * Created by zhongyongsheng on 2018/10/25.
 */

/**
 * 只运行一次的任务
 * @param runBlock 任务
 */
class RunOnceTask(private val runBlock: () -> Unit): Runnable {
    private val hasRun = AtomicBoolean(false)

    override fun run() {
        if (hasRun.compareAndSet(false, true)) {
            runBlock()
        }
    }
}

/**
 * 把Runnable转成RunOnceTask
 */
fun Runnable.toRunOnceTask() = RunOnceTask { this.run() }

/**
 * 可取消的任务
 */
class CancelableTask(private val handler: Handler) : Cancelable {
    var runnable : Runnable? = null

    fun run(delay: Long, timeUnit: TimeUnit, _runnable: Runnable) {
        runnable = _runnable
        if (delay > 0L) {
            handler.postDelayed(runnable, timeUnit.toMillis(delay))
        } else {
            handler.post(runnable)
        }
    }

    override fun cancel() {
        handler.removeCallbacks(runnable)
    }
}

/**
 * 主线程handler
 */
private val mainHandler : Handler = Handler(Looper.getMainLooper())

/**
 * 在main线程执行
 * @param lifecycleOwner 跟生命周期绑定
 * @param delay 延迟时间执行
 * @param timeUnit 延迟时间单位
 * @param cancelWhenEvent 当生命周期跑到此阶段则取消任务
 * @param block 执行的任务
 */
@JvmOverloads
fun runInMainThread(lifecycleOwner: LifecycleOwner? = null,
                    delay: Long = 0L,
                    timeUnit: TimeUnit = TimeUnit.MILLISECONDS,
                    cancelWhenEvent: Lifecycle.Event? = null,
                    block: () -> Unit) {
    val cancelableTask = CancelableTask(mainHandler)
    cancelableTask.run(delay, timeUnit, Runnable { block() })
    lifecycleOwner?.let {
        CancelableLifecycle().observe(it, cancelableTask, cancelWhenEvent)
    }
}