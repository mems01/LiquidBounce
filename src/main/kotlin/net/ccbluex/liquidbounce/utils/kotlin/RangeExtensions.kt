package net.ccbluex.liquidbounce.utils.kotlin

import kotlin.random.Random

fun ClosedFloatingPointRange<Float>.random() = this.start + (this.endInclusive - this.start) * Random.nextFloat()