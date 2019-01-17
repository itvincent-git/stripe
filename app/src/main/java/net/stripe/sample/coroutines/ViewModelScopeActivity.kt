package net.stripe.sample.coroutines

import android.arch.lifecycle.ViewModelProviders
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_view_model_scope.*
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import net.stripe.lib.ObserverableViewModel
import net.stripe.lib.viewModelScope
import net.stripe.sample.R
import net.stripe.sample.util.debugLog

class ViewModelScopeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_model_scope)
        val viewModel = ViewModelProviders.of(this).get(MyViewModel::class.java)
        button.setOnClickListener { viewModel.call() }
    }
}

class MyViewModel: ObserverableViewModel() {

    fun call() {
        viewModelScope.async {
            delay(2000)
            debugLog("MyViewModel call")
        }
    }
}
