package net.kotlin.ex.sample

import android.util.Log

/**
 * Created by zhongyongsheng on 2018/9/13.
 */
object LogUtil {
    fun debug(msg: String) {
        Log.i("LogUtil", "[${Thread.currentThread().name}] $msg")
    }
}
