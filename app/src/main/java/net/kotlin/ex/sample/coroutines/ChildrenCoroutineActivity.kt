package net.kotlin.ex.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.*
import net.kotlin.ex.lib.lifecycleScope
import net.kotlin.ex.sample.R
import net.kotlin.ex.sample.util.debugLog

/**
 * 当子协程使用父协程的coroutineScope时，当父协程cancel()时，子协程也会自动取消执行
 */
class ChildrenCoroutineActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_children_coroutine)

        lifecycleScope.launch {

            val request = launch {
                // it spawns two other jobs, one with GlobalScope
                GlobalScope.launch {
                    debugLog("job1: I run in GlobalScope and execute independently!")
                    delay(1000)
                    debugLog("job1: I am not affected by cancellation of the request")
                }
                // and the other inherits the parent context
                launch {
                    delay(100)
                    debugLog("job2: I am a child of the request coroutine")
                    delay(1000)
                    debugLog("job2: I will not execute this line if my parent request is cancelled")
                }
            }
            delay(500)
            request.cancel() // cancel processing of the request
            delay(1000) // delay a second to see what happens
            debugLog("main: Who has survived request cancellation?")
        }
    }
}
