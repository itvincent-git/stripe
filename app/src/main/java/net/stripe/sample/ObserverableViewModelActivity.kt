package net.stripe.sample

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import net.stripe.lib.ObserverableViewModel
import net.stripe.lib.ViewModelObserver
import net.stripe.sample.util.debugLog

class ObserverableViewModelActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_observerable_view_model)
        val viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        viewModel.addObserver()
    }
}

class MyViewModel: ObserverableViewModel() {

    fun addObserver() {
        addObserver(observer = object: ViewModelObserver {
            override fun onCleared() {
                debugLog("MyViewModel onCleared")
            }
        })

        addObserver(observer = object: ViewModelObserver {
            override fun onCleared() {
                debugLog("MyViewModel onCleared2")
            }
        })
    }
}
