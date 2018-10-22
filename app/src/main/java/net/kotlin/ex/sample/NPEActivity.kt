package net.kotlin.ex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.kotlin.ex.lib.notNullElse

class NPEActivity : AppCompatActivity() {

    var multableString:String? = null

    fun run() {
        multableString = "a"
        val string = multableString ?: ""
        printText(string)
    }
// ============
//    lateinit var multableString: String
//    fun run() {
//        multableString = "a"
//        printText(multableString)
//    }
// ============
    //var mutableInt: Int by Delegates.notNull<Int>()



    fun notNullElse() {
//        val result = multableString?.let {
//            "a"
//        } ?: run {
//            "aa"
//        }

//        val result = if (multableString != null) run { "true" } else run { "false" }

        //=====
//        val result = multableString?.notNull({
//            printText(it)
//            "true"
//        } orNull {
//            "false"
//        })

        fun foo(text: String?) {
            multableString = text
            val result = multableString.notNullElse {
                "$it is not null"
            } ({ "is null" })
            printText(result)
        }

        foo("a")
        foo(null)
    }

    fun printText(text: String) {
        Toast.makeText(this.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npe)
//        run()
        notNullElse()
    }
}
