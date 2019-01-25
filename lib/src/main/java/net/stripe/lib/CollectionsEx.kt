package net.stripe.lib

import java.util.*

/**
 * Collections/arrays extensions
 * Created by zhongyongsheng on 2018/11/21.
 */

/**
 * Returns one data at random from a List
 */
inline fun <E> List<E>.random(): E? = if (size > 0) get(Random().nextInt(size)) else null

/**
 * Returns a set containing the results of applying the given [transform] function
 * to each element in the original collection.
 */
inline fun <T, R> Iterable<T>.mapToSet(transform: (T) -> R) = mapTo(HashSet(), transform)