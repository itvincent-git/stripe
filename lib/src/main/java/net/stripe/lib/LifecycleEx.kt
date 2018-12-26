package net.stripe.lib

import android.arch.lifecycle.*
import android.os.Looper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * 生命周期相关的扩展
 * Created by zhongyongsheng on 2018/11/20.
 */

/**
 * 绑定Cancelable
 */
fun LifecycleOwner.bindCancelable(cancelWhenEvent:Lifecycle.Event = Lifecycle.Event.ON_DESTROY, block: () -> Cancelable)
        = bindCancelableBlockWithLifecycle(this, cancelWhenEvent, block)

/**
 * 绑定到LifecycleOwner
 */
fun <T : Cancelable> T.bindWithLifecycle(lifecycleOwner: LifecycleOwner?, cancelWhenEvent:Lifecycle.Event = Lifecycle.Event.ON_DESTROY)
        = this.apply { bindCancelableBlockWithLifecycle(lifecycleOwner, cancelWhenEvent) { this } }

/**
 * 添加LifecycleObserver到Lifecycle，当LifecycleOwner生命周期变化时，会通知observer，保证在Main线程执行
 */
fun Lifecycle.addObserverInMain(observer: LifecycleObserver) {
    tryCatch {
        //捕获LifecycleRegistry#upEvent IllegalArgumentException
        if (Looper.getMainLooper() != Looper.myLooper()) {
            mainHandler.post { addObserver(observer) }
        } else {
            addObserver(observer)
        }
    }
}

/**
 * 可取消的接口
 */
interface Cancelable {

    /**
     * 取消时执行
     */
    fun cancel()
}

/**
 * 可取消的接口使用的生命周期绑定
 */
class CancelableLifecycle {

    fun observe(lifecycleOwner: LifecycleOwner, cancelable: Cancelable) {
        observe(lifecycleOwner, cancelable)
    }

    fun observe(lifecycleOwner: LifecycleOwner, cancelable: Cancelable, cancelWhenEvent:Lifecycle.Event = Lifecycle.Event.ON_DESTROY) {
        lifecycleOwner.lifecycle.addObserverInMain(object : LifecycleObserver {

            @OnLifecycleEvent(Lifecycle.Event.ON_ANY)
            fun onStateChanged(owner: LifecycleOwner, event: Lifecycle.Event) {

                if (event == cancelWhenEvent) {
                    //取消任务
                    cancelable.cancel()
                    owner.lifecycle.removeObserver(this)
                }
            }
        })
    }
}

/**
 * 给Cancelable绑定生命周期，生命周期结束时，则调用cancel()方法
 * 例如onCreate的时候绑定，则onDestroy cancel；onStart时绑定，则onStop cancel
 *
 * @param lifecycleOwner 生命周期owner
 * @param cancelWhenEvent 当此生命周期出现时，才cancel
 * @param block block返回Cancelable用于取消
 */
fun bindCancelableBlockWithLifecycle(lifecycleOwner: LifecycleOwner?, cancelWhenEvent:Lifecycle.Event = Lifecycle.Event.ON_DESTROY, block: () -> Cancelable) {
    lifecycleOwner?.let {
        CancelableLifecycle().observe(it, block(), cancelWhenEvent)
    }
}

/**
 * 给Cancelable绑定生命周期，生命周期结束时，则调用cancel()方法
 * 例如onCreate的时候绑定，则onDestroy cancel；onStart时绑定，则onStop cancel
 *
 * @param lifecycleOwner 生命周期owner
 * @param cancelWhenEvent 当此生命周期出现时，才cancel
 * @param cancelable Cancelable用于取消
 */
fun bindCancelableWithLifecycle(lifecycleOwner: LifecycleOwner?, cancelWhenEvent:Lifecycle.Event = Lifecycle.Event.ON_DESTROY, cancelable: Cancelable) {
    lifecycleOwner?.let {
        CancelableLifecycle().observe(it, cancelable, cancelWhenEvent)
    }
}

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