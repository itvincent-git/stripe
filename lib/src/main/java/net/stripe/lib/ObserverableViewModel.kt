package net.stripe.lib

import android.arch.lifecycle.ViewModel
import android.os.Looper

/**
 * ObserverableViewModel implement the ViewModelObserverable
 *
 * Created by zhongyongsheng on 2019/1/9.
 */
open class ObserverableViewModel(): ViewModel(), ViewModelObserverable {
    val observers = mutableSetOf<ViewModelObserver>()

    init {
        onCreating()
    }

    /**
     * invoke when the ViewModel constructing
     */
    protected fun onCreating() {
    }

    /**
     * add ViewModelObserver
     */
    override fun addObserver(observer: ViewModelObserver) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            mainHandler.post { observers.add(observer) }
        } else {
            observers.add(observer)
        }
    }

    /**
     * remove ViewModelObserver
     */
    override fun removeObserver(observer: ViewModelObserver) {
        if (Looper.getMainLooper() != Looper.myLooper()) {
            mainHandler.post { observers.remove(observer) }
        } else {
            observers.remove(observer)
        }
    }

    override fun onCleared() {
        super.onCleared()
        for (i in observers) {
            i.onCleared()
        }
        observers.clear()
    }
}

/**
 * ViewModel that can add or remove ViewModelObserver
 */
interface ViewModelObserverable {
    fun addObserver(observer: ViewModelObserver)
    fun removeObserver(observer: ViewModelObserver)
}

/**
 * onCleared will invoke when the ViewModel finish
 */
interface ViewModelObserver {
    fun onCleared()
}
