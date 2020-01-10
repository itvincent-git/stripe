package net.stripe.sample.coroutines

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_actor.offer_btn
import kotlinx.coroutines.channels.Channel.Factory.RENDEZVOUS
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.delay
import net.stripe.lib.lifecycleScope
import net.stripe.sample.R
import net.stripe.sample.util.debugLog

class ActorActivity : AppCompatActivity() {

    //RENDEZVOUS模式，表示只会收到最后一个
    val actor = lifecycleScope.actor<Int>(capacity = RENDEZVOUS) {
        consumeEach {
            debugLog("actor handle $it")
            delay(1000)
        }
    }

    var counter = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_actor)

        offer_btn.setOnClickListener {
            actor.offer(counter++)
        }
    }
}
