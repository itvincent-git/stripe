package net.kotlin.ex.sample.util

import android.content.Context
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*

/**
 * 日志调试view
 * Created by zhongyongsheng on 2018/11/23.
 */
class LogView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : TextView(context, attrs, defStyleAttr) {

    val format = SimpleDateFormat("HH:mm:SS,sss")

    //构造函数初始化，包含读取attrs和LayoutInflater
    init {
    }

    fun log(msg: String) {
        val currentThread = Thread.currentThread()
        if (Looper.getMainLooper().thread == currentThread) {
            mainLog(msg, currentThread)
        } else {
            post { mainLog(msg, currentThread) }
        }

    }

    private fun mainLog(msg: String, currentThread: Thread) {
        val line = "[${currentTime()}] $msg [${currentThread.name}]"
        text = "$text\n$line"
        Log.i("LogView", line)
    }

    fun currentTime() = format.format(Date())
}