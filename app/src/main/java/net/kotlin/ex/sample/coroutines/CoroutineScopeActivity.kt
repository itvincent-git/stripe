package net.kotlin.ex.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kotlin.ex.lib.coroutineScope
import net.kotlin.ex.sample.util.LogUtil
import net.kotlin.ex.sample.R

/**
 * use the coroutineScope bind to the Activity lifecycle
 */
class CoroutineScopeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_scope)

        coroutineScope.async {
            doSomething()
        }
    }

    suspend fun doSomething() {
        // launch ten coroutines for a demo, each working for a different time
        repeat(10) { i ->
            //this job will cancel when activity onDestory
            coroutineScope.launch(Dispatchers.Default) {
                delay((i + 1) * 500L) // variable delay 200ms, 400ms, ... etc
                LogUtil.debug("Coroutine $i is done")
            }
        }
    }
}
