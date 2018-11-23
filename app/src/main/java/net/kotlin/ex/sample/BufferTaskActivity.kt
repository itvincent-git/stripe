package net.kotlin.ex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_buffer_task.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kotlin.ex.lib.BufferTask
import net.kotlin.ex.lib.Cancelable
import net.kotlin.ex.lib.bindCancelableBlockWithLifecycle
import net.kotlin.ex.lib.bindCancelableWithLifecycle
import java.util.*

class BufferTaskActivity : AppCompatActivity() {

    private val bufferTask = BufferTask<Int>(delayTime = 1000) {
        log_view.log("receive buffer int $it")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buffer_task)
        log_view.log("start")
        bindCancelableBlockWithLifecycle(this) { bufferTask }

        val job = GlobalScope.launch {
            for (i in 1..10) {
                val randomInt = Random(System.currentTimeMillis()).nextInt(10)
                log_view.log("add new index:$i, value: $randomInt")
                bufferTask.emit(randomInt)
                delay(300)
            }
        }
        bindCancelableWithLifecycle(this, cancelable = object : Cancelable {
            override fun cancel() {
                job.cancel()
            }
        })



    }
}
