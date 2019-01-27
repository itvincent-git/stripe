package net.stripe.lib

import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.scheduling.ExperimentalCoroutineDispatcher
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.CoroutineContext

/**
 * 协程相关扩展
 */

/**
 * 协程异常时打印日志
 */
var loggingExceptionHandler = CoroutineExceptionHandler { context, throwable ->
    Log.e("CoroutineException", "Coroutine exception occurred. $context", throwable)
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
val appScope = GlobalScope + loggingExceptionHandler


// ----------- lifecycleScope Start --------------
/**
 * 绑定在LifecycleOwner的coroutineScope，在Lifecycle onDestroy时，会把关联的任务全部停止
 */
inline val LifecycleOwner.lifecycleScope get() = lifecycle.lifecycleScope

/**
 * Lifecycle.createScope，创建cancelEvent发生时，会把关联的任务全部停止
 */
fun Lifecycle.createScope(cancelEvent: Lifecycle.Event): CoroutineScope {
    return CoroutineScope(createJob(cancelEvent) + AppScheduler.NON_BLOCKING + loggingExceptionHandler)
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
        val newScope = CoroutineScope(job + AppScheduler.NON_BLOCKING + loggingExceptionHandler)
        if (job.isActive) {
            lifecycleCoroutineScopes[this] = newScope
            job.invokeOnCompletion { _ -> lifecycleCoroutineScopes -= this }
        }
        newScope
    }
// ----------- lifecycleScope End--------------

// ----------- viewModelScope start --------------
private val viewModelCoroutineScopes = mutableMapOf<ViewModelObserverable, CoroutineScope>()
private val viewModelJobs = mutableMapOf<ViewModelObserverable, Job>()

/**
 * create a job is bind to the ViewModelObserver, will cancel when the ViewModel onClear
 */
fun ViewModelObserverable.createJob(): Job {
    return Job().also { job ->
        addObserver(object : ViewModelObserver {
            override fun onCleared() {
                removeObserver(this)
                job.cancel()
            }
        })
    }
}

/**
 * job is bind to the ViewModelObserver, will cancel when the ViewModel onClear
 */
val ViewModelObserverable.job: Job
    get() = viewModelJobs[this] ?: createJob().also {
        if (it.isActive) {
            viewModelJobs[this] = it
            it.invokeOnCompletion { viewModelJobs -= this }
        }
    }

/**
 * scope is bind to the ViewModelObserver, will cancel when the ViewModel onClear
 */
val ViewModelObserverable.viewModelScope: CoroutineScope
    get() = viewModelCoroutineScopes[this] ?: job.let { job ->
        val newScope = CoroutineScope(job + AppScheduler.NON_BLOCKING + loggingExceptionHandler)
        if (job.isActive) {
            viewModelCoroutineScopes[this] = newScope
            job.invokeOnCompletion { viewModelCoroutineScopes -= this }
        }
        newScope
    }
// ----------- viewModelScope end --------------

// number of processors at startup for consistent prop initialization
internal val AVAILABLE_PROCESSORS = Runtime.getRuntime().availableProcessors()
internal const val APP_SCHEDULER_NAME = "AppScheduler"

/**
 * custom defined App Scheduler
 */
@UseExperimental(InternalCoroutinesApi::class)
object AppScheduler : ExperimentalCoroutineDispatcher() {
    /**
     * non blocking scheduler and no more than the cpu cores
     */
    val NON_BLOCKING = CommonPool//limited(AVAILABLE_PROCESSORS)

    override fun close() {
        throw UnsupportedOperationException("$APP_SCHEDULER_NAME cannot be closed")
    }

    override fun toString(): String = APP_SCHEDULER_NAME
}


object CommonPool : ExecutorCoroutineDispatcher() {

    /**
     * Name of the property that controls default parallelism level of [CommonPool].
     * If the property is not specified, `Runtime.getRuntime().availableProcessors() - 1` will be used instead (or `1` for single-core JVM).
     * Note that until Java 10, if an application is run within a container,
     * `Runtime.getRuntime().availableProcessors()` is not aware of container constraints and will return the real number of cores.
     */
    public const val DEFAULT_PARALLELISM_PROPERTY_NAME = "kotlinx.coroutines.default.parallelism"

    override val executor: Executor
        get() = pool ?: getOrCreatePoolSync()

    // Equals to -1 if not explicitly specified
    private val requestedParallelism = run<Int> {
        val property = Try { System.getProperty(DEFAULT_PARALLELISM_PROPERTY_NAME) } ?: return@run -1
        val parallelism = property.toIntOrNull()
        if (parallelism == null || parallelism < 1) {
            error("Expected positive number in $DEFAULT_PARALLELISM_PROPERTY_NAME, but has $property")
        }
        parallelism
    }

    private val parallelism: Int
        get() = requestedParallelism.takeIf { it > 0 }
                ?: (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)

    // For debug and tests
    private var usePrivatePool = false

    @Volatile
    private var pool: Executor? = null

    private inline fun <T> Try(block: () -> T) = try { block() } catch (e: Throwable) { null }

    private fun createPool(): ExecutorService {
        if (System.getSecurityManager() != null) return createPlainPool()
        // Reflection on ForkJoinPool class so that it works on JDK 6 (which is absent there)
        val fjpClass = Try { Class.forName("java.util.concurrent.ForkJoinPool") }
                ?: return createPlainPool() // Fallback to plain thread pool
        // Try to use commonPool unless parallelism was explicitly specified or in debug privatePool mode
        if (!usePrivatePool && requestedParallelism < 0) {
            Try { fjpClass.getMethod("commonPool")?.invoke(null) as? ExecutorService }
                    ?.takeIf { isGoodCommonPool(fjpClass, it) }
                    ?.let { return it }
        }
        // Try to create private ForkJoinPool instance
        Try { fjpClass.getConstructor(Int::class.java).newInstance(parallelism) as? ExecutorService }
                ?. let { return it }
        // Fallback to plain thread pool
        return createPlainPool()
    }

    /**
     * Checks that this ForkJoinPool's parallelism is at least one to avoid pathological bugs.
     */
    internal fun isGoodCommonPool(fjpClass: Class<*>, executor: ExecutorService): Boolean {
        // We cannot use getParallelism, since it lies to us (always returns at least 1)
        // So we submit a task and check that getPoolSize is at least one after that
        // A broken FJP (that is configured for 0 parallelism) would not execute the task and
        // would report its pool size as zero.
        executor.submit {}
        val actual = Try { fjpClass.getMethod("getPoolSize").invoke(executor) as? Int }
                ?: return false
        return actual >= 1
    }

    private fun createPlainPool(): ExecutorService {
        val threadId = AtomicInteger()
        return Executors.newFixedThreadPool(parallelism) {
            Thread(it, "CommonPool-worker-${threadId.incrementAndGet()}").apply { isDaemon = true }
        }
    }

    @Synchronized
    private fun getOrCreatePoolSync(): Executor =
            pool ?: createPool().also { pool = it }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            Log.d("CoroutineEx", "dispatch $context")
            (pool ?: getOrCreatePoolSync()).execute(timeSource.wrapTask(block))
        } catch (e: RejectedExecutionException) {
            timeSource.unTrackTask()
            //DefaultExecutor.enqueue(block)
        }
    }

    // used for tests
    @Synchronized
    internal fun usePrivatePool() {
        shutdown(0)
        usePrivatePool = true
        pool = null
    }

    // used for tests
    @Synchronized
    internal fun shutdown(timeout: Long) {
        (pool as? ExecutorService)?.apply {
            shutdown()
            if (timeout > 0)
                awaitTermination(timeout, TimeUnit.MILLISECONDS)
            shutdownNow()/*.forEach { DefaultExecutor.enqueue(it) }*/
        }
        pool = Executor { throw RejectedExecutionException("CommonPool was shutdown") }
    }

    // used for tests
    @Synchronized
    internal fun restore() {
        shutdown(0)
        usePrivatePool = false
        pool = null
    }

    override fun toString(): String = "CommonPool"

    override fun close(): Unit = error("Close cannot be invoked on CommonPool")
}

internal interface TimeSource {
    fun currentTimeMillis(): Long
    fun nanoTime(): Long
    fun wrapTask(block: Runnable): Runnable
    fun trackTask()
    fun unTrackTask()
    fun registerTimeLoopThread()
    fun unregisterTimeLoopThread()
    fun parkNanos(blocker: Any, nanos: Long) // should return immediately when nanos <= 0
    fun unpark(thread: Thread)
}

internal object DefaultTimeSource : TimeSource {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
    override fun nanoTime(): Long = System.nanoTime()
    override fun wrapTask(block: Runnable): Runnable = block
    override fun trackTask() {}
    override fun unTrackTask() {}
    override fun registerTimeLoopThread() {}
    override fun unregisterTimeLoopThread() {}

    override fun parkNanos(blocker: Any, nanos: Long) {
        LockSupport.parkNanos(blocker, nanos)
    }

    override fun unpark(thread: Thread) {
        LockSupport.unpark(thread)
    }
}

internal var timeSource: TimeSource = DefaultTimeSource