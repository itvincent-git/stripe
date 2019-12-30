package net.stripe.sample.coroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import net.stripe.lib.appScope
import net.stripe.lib.lifecycleScope
import net.stripe.lib.toSafeSendChannel
import net.stripe.sample.R
import net.stripe.sample.util.debugLog
import net.stripe.sample.util.errorLog

/**
 * 父子协程处理异常
 */
class CoroutineExceptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_exception)
//        loadData()
//        loadDataLifecycle()
//        loadDataInAppScope()
//        loadDataInGlobal()
        actor.offer(System.currentTimeMillis())
    }

    override fun onDestroy() {
        super.onDestroy()
        //不加这判断就会崩溃
        /*if (!actor.isClosedForSend) {
            actor.offer(System.currentTimeMillis())
        }*/

        //
        val ret = actor.offer(System.currentTimeMillis())
        debugLog("ret: $ret")
        //在destory时调用lifecycleScope并不会崩溃，代码也并不会执行
        lifecycleScope.launch {
            debugLog("test onDestroy")
        }
    }

    //val job: Job = Job()//子协程的异常或取消，会停止父协程的运行，直接抛出子协程的异常，导致崩溃
    val job: Job = SupervisorJob()//子协程的异常或取消，不会影响父协程的运行
    val scope = CoroutineScope(Dispatchers.Default + job)

    // 自定义的scope用的是SupervisorJob能捕获异常
    private fun loadData() = scope.launch {
        try {
            scope.async {
                throw RuntimeException()
            }.await()
        } catch (e: Exception) {
            errorLog("loadData", e)
        }
    }

    //使用activity生命周期的lifecycleScope
    private fun loadDataLifecycle() = lifecycleScope.launch {
        try {
            //coroutineScope{}做子协程，抛出的异常，父协程也能捕获到
            coroutineScope {
                async {
                    throw RuntimeException()
                }
            }.await()
        } catch (e: Exception) {
            errorLog("loadDataLifecycle", e)
        }
    }

    //AppScope的exceptionhandler能捕获到异常
    private fun loadDataInAppScope() = appScope.launch {
        try {
            async {
                throw RuntimeException()
            }.await()
        } catch (e: Exception) {
            errorLog("loadDataInAppScope", e)
        }
    }

    //使用GlobalScope能捕获到异常，但仍然会崩溃
    private fun loadDataInGlobal() = GlobalScope.launch {
        try {
            async {
                throw RuntimeException()
            }.await()
        } catch (e: Exception) {
            errorLog("loadDataInGlobal", e)
        }
    }

    //使用actor接收时，出现崩溃
    private val actor = lifecycleScope.actor<Long> {
        consumeEach {
            //异常会被lifecycleScope中定义的ExceptionHandler处理
            throw ConcurrentModificationException()
        }
    }.toSafeSendChannel()
}
