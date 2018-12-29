package net.stripe.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import net.stripe.lib.AppScope
import net.stripe.lib.lifecycleScope
import net.stripe.sample.R
import net.stripe.sample.util.errorLog
import java.lang.RuntimeException

/**
 * 父子协程处理异常
 */
class CoroutineExceptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_exception)
        loadData()
        loadDataLifecycle()
        loadDataInAppScope()
    }

//    val job: Job = Job()//子协程的异常或取消，会停止父协程的运行，直接抛出子协程的异常，导致崩溃
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
    private fun loadDataInAppScope() = AppScope.launch {
        try {
            async {
                throw RuntimeException()
            }.await()
        } catch (e: Exception) {
            errorLog("loadDataInAppScope", e)
        }
    }
}
