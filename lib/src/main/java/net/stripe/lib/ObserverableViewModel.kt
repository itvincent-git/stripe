package net.stripe.lib

import android.arch.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import java.util.concurrent.CopyOnWriteArraySet

/**
 * ObserverableViewModel implement the ViewModelObserverable
 *
 * Created by zhongyongsheng on 2019/1/9.
 */
open class ObserverableViewModel() : ViewModel(), ViewModelObserverable {
    private val observers = CopyOnWriteArraySet<ViewModelObserver>()
    private val handler = Handler(Looper.getMainLooper())
    private var currentState = ViewModelState.NotCleared

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
            handler.post { observers.add(observer) }
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

    override fun currentState() = currentState

    override fun onCleared() {
        currentState = ViewModelState.Cleared
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
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
    fun currentState(): ViewModelState
}

/**
 * onCleared will invoke when the ViewModel finish
 */
interface ViewModelObserver {
    fun onCleared()
}

/**
 * ViewModelState
 */
enum class ViewModelState { NotCleared, Cleared }
