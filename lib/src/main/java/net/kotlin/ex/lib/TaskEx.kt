package net.kotlin.ex.lib

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Created by zhongyongsheng on 2018/10/25.
 */

/**
 * 只运行一次的任务
 * @param runBlock 任务
 */
class RunOnceTask(private val runBlock: () -> Unit) {
    private val hasRun = AtomicBoolean(false)

    fun runOnce() {
        if (hasRun.compareAndSet(false, true)) {
            runBlock()
        }
    }
}