package net.stripe.lib

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.SendChannel

/**
 * coroutine Channel extension
 * @author zhongyongsheng
 */

/**
 * 转换为[SafeSendChannel]
 */
fun <E> SendChannel<E>.toSafeSendChannel(exceptionHandler: ((Throwable) -> Unit)? = null) =
    SafeSendChannel(this, exceptionHandler)

/**
 *  当调用[SendChannel.offer], [SendChannel.close], [SendChannel.invokeOnClose]时会捕获异常，通过[exceptionHandler]回调
 */
class SafeSendChannel<E>(
    private val sourceChannel: SendChannel<E>,
    private val exceptionHandler: ((Throwable) -> Unit)? = null
) : SendChannel<E> by sourceChannel {

    override fun close(cause: Throwable?): Boolean {
        return try {
            sourceChannel.close(cause)
        } catch (t: Throwable) {
            exceptionHandler?.invoke(t)
            false
        }
    }

    @ExperimentalCoroutinesApi
    override fun invokeOnClose(handler: (cause: Throwable?) -> Unit) {
        try {
            sourceChannel.invokeOnClose(handler)
        } catch (t: Throwable) {
            exceptionHandler?.invoke(t)
        }
    }

    override fun offer(element: E): Boolean {
        return try {
            if (!isClosedForSend) {
                sourceChannel.offer(element)
            } else {
                false
            }
        } catch (t: Throwable) {
            exceptionHandler?.invoke(t)
            false
        }
    }
}