package net.kotlin.coroutines.lib

import android.util.Log

/**
 * 捕获block的异常，然后返回block的值，默认异常是打印logcat日志
 * Created by zhongyongsheng on 2018/9/20.
 */
fun <T> tryCatch(catchBlock:(Throwable) -> Unit = { t -> Log.i("TryEx", "tryCatchLogcat print:", t) },
        tryBlock:() -> T
): T? {
    try {
       return tryBlock()
    } catch (t: Throwable) {
        catchBlock(t)
    }
    return null
}


/**
 * try catch运行block，如果有异常则再运行，直接超时times的次数
 */
fun <R> tryTimes(times: Int, block: (Int) -> R): R? {
    var currentTimes = 0
    while (currentTimes < times) {
        try {
            return block(currentTimes)
        } catch (e: Throwable) {
            currentTimes++
        }
    }
    return null
}