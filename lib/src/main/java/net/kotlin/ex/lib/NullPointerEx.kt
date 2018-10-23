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
fun <T, R> T?.notNullElse(block: (T)-> R): (() -> R) -> R = { if (this == null) it() else block(this) }


/**
 * 判断2个变量都非空执行block, 参数a, b为对象的非null类型
 * allNotNull(first, second) { a, b ->
 *     printText("$a, $b is all not null")
 * }
 */
fun <A, B> allNotNull(first: A?, second: B?, block: (A, B) -> Unit) {
    if (first != null && second != null) block(first, second)
}

/**
 * 判断2个变量都非空执行第一个block, 参数a, b为对象的非null类型；为null则执行第二个block
 *  val result = allNotNullElse(first, second) { a, b ->
 *     "$a, $b is all not null"
 *  } ({ "one of them is null"})
 */
fun <A, B, R> allNotNullElse(first: A?, second: B?, block: (A, B) -> R) : (() -> R) -> R {
    return {
        if (first != null && second != null) {
            block(first, second)
        } else {
            it()
        }
    }

}