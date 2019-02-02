package net.stripe.sample.coroutines

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_produce.*
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import net.stripe.lib.lifecycleScope
import net.stripe.lib.produceDelay
import net.stripe.lib.produceInterval
import net.stripe.sample.R
import net.stripe.sample.util.debugLog
import java.util.concurrent.TimeUnit

class ProduceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_produce)

        produce_interval_btn.setOnClickListener {
            lifecycleScope.launch {
                produceInterval(period = TimeUnit.SECONDS.toMillis(1)).consumeEach {
                    debugLog("produceInterval consume $it")
                }
            }
        }

        produce_delay_btn.setOnClickListener {
            lifecycleScope.launch {
                produceDelay(delay = TimeUnit.SECONDS.toMillis(2)).consumeEach {
                    debugLog("produceDelay consume $it")
                }
            }
        }
    }
}
