package net.stripe.lib

import java.util.*

/**
 * 组合，数组等扩展
 * Created by zhongyongsheng on 2018/11/21.
 */

/**
 * 从一个List中随机取1个数据
 */
fun <E> List<E>.random(): E? = if (size > 0) get(Random().nextInt(size)) else null