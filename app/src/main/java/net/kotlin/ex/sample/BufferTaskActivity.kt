package net.kotlin.ex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_buffer_task.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.kotlin.ex.lib.BufferTask
import net.kotlin.ex.lib.bindWithLifecycle
import net.kotlin.ex.lib.lifecycleScope
import net.kotlin.ex.sample.util.debugLog
import java.util.*


/**
 * 示例：可缓存被执行的任务
 */
class BufferTaskActivity : AppCompatActivity() {

    private val bufferTask = BufferTask<Int>(delayTime = 2000) {
        debugLog("receive buffer int $it")
        log_view.log("receive buffer int $it")
    }.bindWithLifecycle(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_buffer_task)
        log_view.log("start")

        emit_btn.setOnClickListener {
            val randomInt = Random(System.currentTimeMillis()).nextInt(5)
            log_view.log("add new value: $randomInt")
            bufferTask.emit(randomInt)
        }

        autoEmit()
    }

    fun autoEmit() {
        lifecycleScope.launch {
            for (i in 1..10) {
                val randomInt = Random(System.currentTimeMillis()).nextInt(5)
                log_view.log("add new index:$i, value: $randomInt")
                bufferTask.emit(randomInt)
                delay(300)
            }
        }
    }
}
