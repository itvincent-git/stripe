package net.stripe.lib

/**
 * System Property extension
 * Created by zhongyongsheng on 2019/1/17.
 */

/**
 * set system property
 */
fun setSystemProp(propertyName: String, propertyValue: String?) {
    try {
        System.setProperty(propertyName, propertyValue)
    } catch (e: SecurityException) {
    }
}

/**
 * get system property
 */
fun getSystemProp(propertyName: String): String? =
        try {
            System.getProperty(propertyName)
        } catch (e: SecurityException) {
            null
        }

/**
 * get system property as Int
 */
fun getSystemProp(
        propertyName: String,
        defaultValue: Int,
        minValue: Int = 1,
        maxValue: Int = Int.MAX_VALUE
): Int {
    val value = getSystemProp(propertyName) ?: return defaultValue
    val parsed = value.toIntOrNull()
            ?: error("System property '$propertyName' has unrecognized value '$value'")
    if (parsed !in minValue..maxValue) {
        error("System property '$propertyName' should be in range $minValue..$maxValue, but is '$parsed'")
    }
    return parsed
}
