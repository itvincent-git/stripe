package net.kotlin.ex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import net.kotlin.coroutines.lib.tryTimes
import java.lang.RuntimeException

class TryTimesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_try_times)

        tryTimes(3) {
            if (it < 2) throw RuntimeException("current times:$it fail")
            Toast.makeText(this, "run $it times ok", Toast.LENGTH_SHORT).show()
        }
    }
}
