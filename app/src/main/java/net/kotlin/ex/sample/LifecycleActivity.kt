package net.kotlin.ex.sample

import android.arch.lifecycle.Lifecycle
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import net.kotlin.ex.lib.runInMainThread

class LifecycleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle)

        //延迟3秒执行；activity onDestory时取消
        runInMainThread(lifecycleOwner = this, delay = 3000) {
            showToast(this, "runInMainThread")
        }

        //延迟4秒执行；activity onPause时取消
        runInMainThread(lifecycleOwner = this, delay = 4000, cancelWhenEvent = Lifecycle.Event.ON_PAUSE) {
            showToast(this, "runInMainThread cancel when pause")
        }
    }
}
