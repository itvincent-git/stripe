package net.stripe.lib

import android.annotation.SuppressLint
import android.arch.lifecycle.GenericLifecycleObserver
import android.arch.lifecycle.Lifecycle
import android.arch.lifecycle.LifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * 协程生命周期的扩展
 * @author zhongyongsheng
 */

/**
 * 全局App生命周期的Scope，替代GlobalScope。这个Scope是有Job的。
 */
val appScope: CoroutineScope
    get() = CoroutineScope(Job() + Dispatchers.Default + loggingExceptionHandler)

// ----------- lifecycleScope Start --------------
/**
 * 绑定在LifecycleOwner的coroutineScope，在Lifecycle onDestroy时，会把关联的任务全部停止
 */
inline val LifecycleOwner.lifecycleScope get() = lifecycle.lifecycleScope

/**
 * Lifecycle.createScope，创建cancelEvent发生时，会把关联的任务全部停止
 */
fun Lifecycle.createScope(cancelEvent: Lifecycle.Event): CoroutineScope {
    return CoroutineScope(createJob(cancelEvent) + Dispatchers.Default + loggingExceptionHandler)
}

/**
 * 创建绑定生命周期的Job
 * @param cancelEvent 当收到该生命周期时取消Job，默认ON_DESTROY；forbiddenCancelEvents是不支持的类型
 */
fun Lifecycle.createJob(cancelEvent: Lifecycle.Event = Lifecycle.Event.ON_DESTROY): Job {
    if (cancelEvent in forbiddenCancelEvents) {
        throw UnsupportedOperationException("$cancelEvent is forbidden for createJob(…).")
    }
    return Job().also { job ->
        if (currentState == Lifecycle.State.DESTROYED) job.cancel()
        else addObserver(@SuppressLint("RestrictedApi") object : GenericLifecycleObserver {
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
        val newScope = CoroutineScope(job + Dispatchers.Default + loggingExceptionHandler)
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
        if (currentState() == ViewModelState.Cleared) job.cancel()
        else addObserver(object : ViewModelObserver {
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
        val newScope = CoroutineScope(job + Dispatchers.Default + loggingExceptionHandler)
        if (job.isActive) {
            viewModelCoroutineScopes[this] = newScope
            job.invokeOnCompletion { viewModelCoroutineScopes -= this }
        }
        newScope
    }
// ----------- viewModelScope end --------------