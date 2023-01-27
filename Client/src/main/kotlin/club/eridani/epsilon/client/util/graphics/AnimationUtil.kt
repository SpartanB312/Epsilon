package club.eridani.epsilon.client.util.graphics

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

object AnimationUtil {
    fun calculateCompensation(target: Float, current: Float, delta: Long, speed: Double): Float {
        var current = current
        var delta = delta
        val diff = current - target
        if (delta < 1) {
            delta = 1
        }
        if (delta > 1000) {
            delta = 16
        }
        if (diff > speed) {
            val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
            current -= xD.toFloat()
            if (current < target) {
                current = target
            }
        } else if (diff < -speed) {
            val xD = if (speed * delta / (1000 / 60) < 0.5) 0.5 else speed * delta / (1000 / 60)
            current += xD.toFloat()
            if (current > target) {
                current = target
            }
        } else {
            current = target
        }
        return current
    }

    fun animate(target0: Int, current0: Int, speed0: Float): Int {
        if (current0 == target0) return current0
        var speed = speed0
        val larger = target0 > current0
        if (speed < 0f) speed = 0f
        else if (speed > 1f) speed = 1f
        val dif = max(current0, target0) - min(current0, target0)
        var factor = dif * speed
        if (factor < 0.1f) factor = 0.1f
        return if (larger) {
            min(current0 + factor.toInt(), target0)
        } else {
            max(current0 - factor.toInt(), target0)
        }
    }

    fun animate(target0: Float, current0: Float, speed0: Float): Float {
        if (current0 == target0) return current0
        var speed = speed0
        val larger = target0 > current0
        if (speed < 0f) speed = 0f
        else if (speed > 1f) speed = 1f
        val dif = max(current0, target0) - min(current0, target0)
        var factor = dif * speed
        if (factor < 0.1f) factor = 0.1f
        return if (larger) {
            min(current0 + factor, target0)
        } else {
            max(current0 - factor, target0)
        }
    }

    fun animate(target0: Double, current0: Double, speed0: Double): Double {
        if (current0 == target0) return current0
        var speed = speed0
        val larger = target0 > current0
        if (speed < 0.0) speed = 0.0
        else if (speed > 1.0) speed = 1.0
        val dif = max(current0, target0) - min(current0, target0)
        var factor = dif * speed
        if (factor < 0.1) factor = 0.1
        return if (larger) {
            min(current0 + factor, target0)
        } else {
            max(current0 - factor, target0)
        }
    }

    fun toDelta(start: Long): Int {
        return (System.currentTimeMillis() - start).coerceAtLeast(0).coerceAtMost(Int.MAX_VALUE.toLong()).toInt()
    }

    fun exponent(delta: Int, length: Int, from: Double, to: Double): Double {
        return when {
            from == to -> {
                from
            }
            from < to -> {
                exponentInc0(delta, length, from, to)
            }
            else -> {
                exponentDec0(delta, length, to, from)
            }
        }
    }

    fun exponentInc(delta: Int, length: Int): Double {
        return exponentInc0(delta, length, 0.0, 1.0)
    }

    fun exponentDec(delta: Int, length: Int): Double {
        return exponentDec0(delta, length, 0.0, 1.0)
    }

    fun exponentInc(delta: Int, length: Int, max: Double): Double {
        return if (max <= 0.0) {
            0.0
        } else {
            exponentInc0(delta, length, 0.0, max)
        }
    }

    fun exponentDec(delta: Int, length: Int, max: Double): Double {
        return if (max <= 0.0) {
            0.0
        } else {
            exponentDec0(delta, length, 0.0, max)
        }
    }

    fun exponentInc(delta: Int, length: Int, min: Double, max: Double): Double {
        return if (min == max) {
            min
        } else if (min < max) {
            exponentInc0(delta, length, min, max)
        } else {
            exponentInc0(delta, length, max, min)
        }
    }

    fun exponentDec(delta: Int, length: Int, min: Double, max: Double): Double {
        return if (min == max) {
            min
        } else if (min < max) {
            exponentDec0(delta, length, min, max)
        } else {
            exponentDec0(delta, length, max, min)
        }
    }

    // No min / max sanity check
    private fun exponentInc0(delta: Int, length: Int, min: Double, max: Double): Double {
        val result = deltaLengthCheck(delta, length, min, max)
        if (!java.lang.Double.isNaN(result)) return result
        val normalizedDelta = delta.toDouble() / length.toDouble() - 1.0
        val sqDelta = Math.pow(normalizedDelta, 2.0)
        return exponent0(sqDelta, min, max)
    }

    // No min / max sanity check
    private fun exponentDec0(delta: Int, length: Int, min: Double, max: Double): Double {
        val result = deltaLengthCheck(delta, length, max, min)
        if (!java.lang.Double.isNaN(result)) return result
        val normalizedDelta = (delta + length).toDouble() / length.toDouble() - 1.0
        val sqDelta = normalizedDelta.pow(2.0)
        return exponent0(sqDelta, min, max)
    }

    // No checks
    private fun exponent0(sqDelta: Double, min: Double, max: Double): Double {
        val range = max - min
        return sqrt(1.0 - sqDelta) * range + min
    }

    private fun deltaLengthCheck(delta: Int, length: Int, from: Double, to: Double): Double {
        if (length <= 0) {
            return from
        }
        if (delta < 0) {
            return from
        } else if (delta >= length) {
            return to
        }
        return Double.NaN
    }
}