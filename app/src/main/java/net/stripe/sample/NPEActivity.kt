package net.stripe.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Toast
import net.stripe.lib.allNotNull
import net.stripe.lib.allNotNullElse
import net.stripe.lib.notNullElse

class NPEActivity : AppCompatActivity() {

    var multableString:String? = null

//    fun run() {
//        multableString = "a"
//        val string = multableString ?: ""
//        printText(string)
//    }
// ============
//    lateinit var multableString: String
//    fun run() {
//        multableString = "a"
//        printText(multableString)
//    }
// ============
    //var mutableInt: Int by Delegates.notNull<Int>()

//    ============

    private fun run() {
        multableString = "a"
        printText(multableString ?: "")
    }

    private fun notNullElse() {
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


    var first: String? = null
    var second: String? = null
    var third: String? = null
    private fun _notNull() {
        first = "1"
        second = "2"
        third = "3"

        // 2====
        allNotNull(first, second) { a, b ->
            printText("$a, $b is all not null")
        }

        val result = allNotNullElse(first, second) { a, b ->
            "$a, $b is all not null"
        }({ "one of them is null"})
        printText(result)

        // 3====
        allNotNull(first, second, third) { a, b, c ->
            printText("$a, $b, $c is all not null")
        }

        val result2 = allNotNullElse(first, second, third) { a, b, c ->
            "$a, $b, $c is all not null"
        }({ "one of them is null"})
        printText(result2)
    }

    fun printText(text: String) {
        Toast.makeText(this.applicationContext, text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_npe)
//        run()
//        notNullElse()
        _notNull()
    }
}
