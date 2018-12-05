package net.kotlin.ex.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import net.kotlin.ex.lib.random
import net.kotlin.ex.sample.util.debugLog

/**
 * 数组示例
 */
class CollectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        val data = mutableListOf(1, 3, 4, 5, 8, 9, 11)
        data.retainAll { it % 2 == 0 }.let { debugLog("保留偶数 $data") }
        data += listOf(1, 3, 4, 5, 8, 9, 11)
        data.removeAll { it % 2 == 0 }.let { debugLog("移除偶数 $data") }

        data += 66
        data.let { debugLog("增加66 $it") }
        data += listOf(12, 13)
        data.let { debugLog("增加12,13 $it") }
        data -= 66
        data.let { debugLog("删除66 $it") }


        debugLog("随机从数组取一个数： ${data.random()}")
    }
}
