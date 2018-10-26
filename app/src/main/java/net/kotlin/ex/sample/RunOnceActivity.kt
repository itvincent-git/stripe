package net.kotlin.ex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_run_once.*
import net.kotlin.ex.lib.RunOnceTask

class RunOnceActivity : AppCompatActivity() {

    val task = RunOnceTask(fun () {
        Toast.makeText(this, "toast once", Toast.LENGTH_SHORT).show()
    })

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_run_once)

        btn_once.setOnClickListener {
            task.runOnce()
        }
    }
}
