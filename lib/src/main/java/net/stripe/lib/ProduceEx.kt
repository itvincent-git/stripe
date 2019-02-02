package net.stripe.lib

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Produce extension
 * Created by zhongyongsheng on 2019/2/1.
 */

/**
 * Send data at regular intervals, and return ReceiveChannel
 */
fun CoroutineScope.produceInterval(context: CoroutineContext = EmptyCoroutineContext,
                                   capacity: Int = 0,
                                   initialDelay: Long = 0L,
                                   period: Long) = produce {
    delay(initialDelay)
    while (true) {
        send(System.currentTimeMillis())
        delay(period)
    }
}

/**
 * Send data after delay, and return ReceiveChannel
 */
fun CoroutineScope.produceDelay(context: CoroutineContext = EmptyCoroutineContext,
                                   capacity: Int = 0,
                                   delay: Long) = produce {
    delay(delay)
    send(System.currentTimeMillis())
}