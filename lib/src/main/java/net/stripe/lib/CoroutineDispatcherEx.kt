package net.stripe.lib

import android.util.Log
import kotlinx.coroutines.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.locks.LockSupport
import kotlin.coroutines.CoroutineContext

/**
 * custom defined the ExecutorCoroutineDispatcher extentions
 * Created by zhongyongsheng on 2019/1/28.
 */


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
            Log.d("CoroutineEx", "dispatch $context $block")
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

/**
 * Executors.newFixedThreadPool Pool
 */
object ThreadPoolDispatcher: ExecutorCoroutineDispatcher() {
    private val poolName = "ThreadPoolDispatcher"
    private val TAG = "CoroutineDispatcherEx"
    private val poolSize = AVAILABLE_PROCESSORS.coerceAtLeast(4)
    init {
        if (BuildConfig.DEBUG) Log.d(TAG, "ThreadPoolDispatcher init $poolSize")
    }

    override val executor: Executor = Executors.newFixedThreadPool(poolSize, object : ThreadFactory{
        private val threadNum = AtomicInteger(0)

        override fun newThread(r: Runnable): Thread {
            return object : Thread(poolName + threadNum.incrementAndGet()) {
                override fun run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND)
                    r.run()
                }
            }
        }

    })

    override fun close() = error("Close cannot be invoked on ThreadPoolDispatcher")

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        try {
            if (BuildConfig.DEBUG) Log.d(TAG, "dispatch $context $block")
            executor.execute(RunnableWrapper(block))
        } catch (e: RejectedExecutionException) {
            DefaultExecutor.dispatch(context, block)
        }
    }

    class RunnableWrapper(val realRunnable: Runnable) : Runnable {
        private val number = AtomicInteger(0)

        override fun run() {
            if (BuildConfig.DEBUG) Log.d(TAG, "RunnableWrapper run start[$number] $realRunnable")
            realRunnable.run()
            if (BuildConfig.DEBUG) Log.d(TAG, "RunnableWrapper run end[$number] $realRunnable")
        }

    }
}

@UseExperimental(ObsoleteCoroutinesApi::class)
internal val DefaultExecutor = newSingleThreadContext("DefaultExecutor")

/**
 * create dispatcher that only 1 core thread and 1 max thread
 */
fun createSingleThreadContext(name: String) = Executors.newSingleThreadExecutor {
    Thread(it, name).apply { this.isDaemon = true }
}.asCoroutineDispatcher()