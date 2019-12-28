package net.stripe.sample.util

import android.content.Context
import android.util.Log
import android.widget.Toast

/**
 * 测试日志
 *
 * Created by zhongyongsheng on 2018/9/13.
 */
const val TAG = "LogUtil"

fun debugLog(msg: String) {
    Log.i(TAG, "[${Thread.currentThread().name}] $msg")
}

fun errorLog(message: String, throwable: Throwable) {
    Log.e(TAG, "[${Thread.currentThread().name}] ${message}", throwable)
}

fun showToast(context: Context, text: String) {
    Toast.makeText(context.applicationContext, text, Toast.LENGTH_SHORT).show()
}
