package net.stripe.lib

import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
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
val mainHandler : Handler = Handler(Looper.getMainLooper())

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
    CancelableTask(mainHandler).bindWithLifecycle(lifecycleOwner, cancelWhenEvent).apply { this.run(delay, timeUnit, Runnable { block() }) }
}

/**
 * 可缓存被执行的任务
 * @param delayTime 缓存时长
 * @param delayTimeUnit 缓存时长单位
 * @param block 执行的任务，接收已发送的数据
 */
class BufferTask<T> @JvmOverloads constructor(val delayTime: Long = 0L,
                    val delayTimeUnit: TimeUnit = TimeUnit.MILLISECONDS,
                    val block: (List<T>) -> Unit): Cancelable {
    protected val channel = Channel<T>()
    protected val buffer = mutableListOf<T>()
    protected var receiveJob: Job? = null
    protected val delayRealTime = delayTimeUnit.toMillis(delayTime)

    /**
     * 发送数据给任务
     */
    fun emit(value: T) {
        GlobalScope.launch {
            channel.send(value)
        }

        if (receiveJob == null) {
            startReceive()
        }
    }

    fun startReceive() {
        receiveJob = GlobalScope.launch {
            while (true) {
                val startTime = System.currentTimeMillis()
                var deltaTime = 0L
                while (deltaTime < delayRealTime) {
                    withTimeoutOrNull(delayRealTime - deltaTime) {
                        val r = channel.receive()
                        buffer.add(r)
                    }
                    deltaTime = (System.currentTimeMillis() - startTime)
                }
                if (buffer.isNotEmpty()) {
                    buffer.toMutableList().let(block)
                    buffer.clear()
                }
            }
        }
    }

    override fun cancel() {
        receiveJob?.cancel()
    }
}