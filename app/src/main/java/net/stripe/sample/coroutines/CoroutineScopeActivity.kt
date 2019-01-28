package net.stripe.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import net.stripe.lib.AppScheduler
import net.stripe.lib.lifecycleScope
import net.stripe.sample.R
import net.stripe.sample.util.debugLog

/**
 * use the lifecycleScope bind to the Activity lifecycle
 */
class CoroutineScopeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine_scope)

        lifecycleScope.async(Dispatchers.Main) {
            doSomething()
        }
    }

    suspend fun doSomething() = coroutineScope {
        debugLog("this will run in Main")
        // launch ten coroutines for a demo, each working for a different time
        repeat(10) { i ->
            //this job will cancel when activity onDestory
            launch(AppScheduler.Default) {
                delay((i + 1) * 500L) // variable delay 200ms, 400ms, ... etc
                debugLog("Coroutine $i is done")
            }
        }
    }
}
