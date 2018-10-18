package net.kotlin.ex.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle

/**
 * 数组示例
 */
class CollectionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_collection)

        val data = mutableListOf(1, 3, 4, 5, 8, 9, 11)
        data.retainAll { it % 2 == 0 }.let { LogUtil.debug("保留偶数 $data") }
        data += listOf(1, 3, 4, 5, 8, 9, 11)
        data.removeAll { it % 2 == 0 }.let { LogUtil.debug("移除偶数 $data") }

        data += 66
        data.let { LogUtil.debug("增加66 $it") }
        data += listOf(12, 13)
        data.let { LogUtil.debug("增加12,13 $it") }
        data -= 66
        data.let { LogUtil.debug("删除66 $it") }

    }
}
