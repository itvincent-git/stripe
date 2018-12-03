package net.kotlin.ex.sample.util

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * Created by zhongyongsheng on 2018/9/13.
 */
object LogUtil {
    fun debug(msg: String) {
        Log.i("LogUtil", "[${Thread.currentThread().name}] $msg")
    }
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).show()
}
