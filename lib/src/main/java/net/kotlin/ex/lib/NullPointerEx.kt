package net.kotlin.ex.lib

/**
 * 判空用的扩展方法
 * Created by zhongyongsheng on 2018/10/22.
 */
/**
 * 判断非null执行第一个block,it为对象的非null类型;为null则执行第二个block
 *
 * sample:
 * val result = nullableString.notNullElse {
 *  "$it is not null"
 * } ({ "is null" })
 */
infix fun <T, R> T?.notNullElse(block: (T)-> R): (() -> R) -> R {
    return {
        if (this == null)
        {
            it()
        }
        else
            block(this)

    }
}