package net.stripe.sample

import android.arch.lifecycle.Lifecycle
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.coroutines.launch
import net.stripe.lib.lifecycleScope
import net.stripe.lib.runInMainThread
import net.stripe.sample.util.showToast

class LifecycleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lifecycle)

        //延迟3秒执行；activity onDestory时取消
        runInMainThread(lifecycleOwner = this, delay = 3000) {
            showToast(this, "runInMainThread")
        }

        lifecycleScope.launch {
            //延迟4秒执行；activity onPause时取消
            runInMainThread(lifecycleOwner = this@LifecycleActivity, delay = 4000, cancelWhenEvent = Lifecycle.Event.ON_PAUSE) {
                showToast(this@LifecycleActivity, "runInMainThread cancel when pause")
            }
        }
    }
}
