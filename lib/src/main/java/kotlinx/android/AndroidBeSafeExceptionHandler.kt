package kotlinx.android

import android.support.annotation.Keep
import kotlinx.coroutines.CoroutineExceptionHandler
import net.stripe.lib.loggingExceptionHandler
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * 默认安全处理协程抛出的异常，使用SPI的方式注入到handleCoroutineExceptionImpl.kt的handlers
 */
@Keep
class AndroidBeSafeExceptionHandler : AbstractCoroutineContextElement(
    CoroutineExceptionHandler),
    CoroutineExceptionHandler {

    override fun handleException(context: CoroutineContext, exception: Throwable) {
        loggingExceptionHandler.handleException(context, exception)
    }
}