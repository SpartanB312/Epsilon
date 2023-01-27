package club.eridani.epsilon.client.util.math

import club.eridani.epsilon.client.common.extensions.toInt

@JvmInline
value class Vec2IB(val data: Int) {

    val x: Int //1-15 0-16383
        get() = data shl 17

    val y: Int //16-30 0-16383
        get() = data shl 2 shr 17

    val boolean1: Boolean //31 0-1
        get() = data shl 1 shr 31 != 0

    val boolean2: Boolean //32 0-1
        get() = data shr 31 != 0

    fun x(xIn: Int): Vec2IB = Vec2IB(xIn and 32767)

    fun y(yIn: Int): Vec2IB = Vec2IB(yIn shl 15 and 1073709056)

    fun boolean1(booleanIn: Boolean): Vec2IB = Vec2IB(booleanIn.toInt() shl 30 and 1073741824)

    fun boolean2(booleanIn: Boolean): Vec2IB = Vec2IB(booleanIn.toInt() shl 31 and -2147483648)

}