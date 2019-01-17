package net.stripe.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import net.stripe.lib.getSystemProp
import net.stripe.lib.setSystemProp

class SystemActivity : AppCompatActivity() {
    val TAG = "SystemActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_system)

        Log.i(TAG, "getSystemProp:" + getSystemProp("kotlinx.coroutines.scheduler"))
        setSystemProp("kotlinx.coroutines.scheduler", "off")
        Log.i(TAG, "setSystemProp done")
    }
}
