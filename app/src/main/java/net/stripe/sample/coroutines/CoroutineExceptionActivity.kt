package net.stripe.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
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
    }

    //val job: Job = Job()//子协程的异常或取消，会停止父协程的运行，直接抛出子协程的异常，导致崩溃
    val job: Job = SupervisorJob()//子协程的异常或取消，不会影响父协程的运行
    val scope = CoroutineScope(Dispatchers.Default + job)
    // may throw Exception
    private fun doWork(): Deferred<Unit> = scope.async { throw RuntimeException(); Unit }   // (1)

    private fun loadData() = scope.launch {
        try {
            doWork().await()                               // (2)
        } catch (e: Exception) {
            errorLog(e)
        }
    }

    //问个题coroutineScope{}做子协程，抛出的异常，父协程也能捕获到
    private suspend fun doWorkLifecycle(): Deferred<Unit> = coroutineScope {
        async { throw RuntimeException(); Unit }   // (1)
    }

    private fun loadDataLifecycle() = lifecycleScope.launch {
        try {
            doWorkLifecycle().await()                               // (2)
        } catch (e: Exception) {
            errorLog(e)
        }
    }
}
