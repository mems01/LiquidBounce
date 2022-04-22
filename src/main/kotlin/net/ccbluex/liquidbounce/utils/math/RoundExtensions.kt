package net.ccbluex.liquidbounce.utils.math

import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Rounds a double. From https://stackoverflow.com/a/2808648/9140494
 *
 * @param value  the value to be rounded
 * @param places Decimal places
 * @return The rounded value
 */
fun round(value: Double, places: Int): Double {
    require(places >= 0)

    var bd = BigDecimal.valueOf(value)
    bd = bd.setScale(places, RoundingMode.HALF_UP)

    return bd.toDouble()
}
