package net.kotlin.ex.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import net.kotlin.ex.sample.R
import net.kotlin.ex.sample.util.errorLog
import java.lang.RuntimeException

class CoroutineExceptionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_exception)
        loadData()
    }

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


}
