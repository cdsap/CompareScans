package io.github.cdsap.comparescans.collector

import kotlin.math.pow
import kotlin.math.roundToInt

fun Double.round(): Double {
    if (this.isNaN()) {
        return Double.NaN
    }
    val factor = 10.0.pow(0)
    return (this * factor).roundToInt() / factor
}
