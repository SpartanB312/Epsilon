package club.eridani.epsilon.client.util.math

import kotlin.math.PI
import kotlin.math.ceil
import kotlin.math.floor

const val PI_FLOAT = 3.14159265358979323846f


fun Float.toRadian() = this / 180.0f * PI_FLOAT

fun Double.toRadian() = this / 180.0 * PI

fun Float.toDegree() = this * 180.0f / PI_FLOAT

fun Double.toDegree() = this * 180.0 / PI


fun Double.floorToInt() = floor(this).toInt()

fun Float.floorToInt() = floor(this).toInt()

fun Double.ceilToInt() = ceil(this).toInt()

fun Float.ceilToInt() = ceil(this).toInt()


fun Int.square() = this * this

fun Long.square() = this * this

fun Float.square() = this * this

fun Double.square() = this * this