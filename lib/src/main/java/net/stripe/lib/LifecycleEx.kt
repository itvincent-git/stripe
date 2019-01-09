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