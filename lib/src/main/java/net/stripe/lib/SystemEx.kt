package net.stripe.lib

/**
 * System Property extension
 * Created by zhongyongsheng on 2019/1/17.
 */

fun setSystemProp(propertyName: String, propertyValue: String?) {
    try {
        System.setProperty(propertyName, propertyValue)
    } catch (e: SecurityException) {
    }
}

fun getSystemProp(propertyName: String): String? =
    try {
        System.getProperty(propertyName)
    } catch (e: SecurityException) {
        null
    }

