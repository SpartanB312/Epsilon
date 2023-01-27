@file:Suppress("NOTHING_TO_INLINE")

package club.eridani.epsilon.client.util.math

import club.eridani.epsilon.client.common.extensions.renderPosX
import club.eridani.epsilon.client.common.extensions.renderPosY
import club.eridani.epsilon.client.common.extensions.renderPosZ
import club.eridani.epsilon.client.common.interfaces.Helper
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.Vec3d
import java.awt.Color
import kotlin.math.*
import kotlin.random.Random

const val WORLD_BOARDER_D = 30_000_000.0
const val WORLD_BOARDER_F = 30_000_000.0f
const val WORLD_BOARDER_I = 30_000_000
private const val SMALL_NUMBER = 1e-8f
private const val KINDA_SMALL_NUMBER = 1e-4f

inline fun Double.fastFloor() = (this + WORLD_BOARDER_D).toInt() - WORLD_BOARDER_I
inline fun Float.fastFloor() = (this + WORLD_BOARDER_F).toInt() - WORLD_BOARDER_I

inline fun Double.fastCeil() = WORLD_BOARDER_I - (WORLD_BOARDER_D - this).toInt()
inline fun Float.fastCeil() = WORLD_BOARDER_I - (WORLD_BOARDER_F - this).toInt()

inline val Double.sq: Double get() = this * this
inline val Float.sq: Float get() = this * this
inline val Int.sq: Int get() = this * this

inline val Double.cubic: Double get() = this * this * this
inline val Float.cubic: Float get() = this * this * this
inline val Int.cubic: Int get() = this * this * this

inline val Double.quart: Double get() = this * this * this * this
inline val Float.quart: Float get() = this * this * this * this
inline val Int.quart: Int get() = this * this * this * this

inline val Double.quint: Double get() = this * this * this * this * this
inline val Float.quint: Float get() = this * this * this * this * this
inline val Int.quint: Int get() = this * this * this * this * this

@Suppress("NOTHING_TO_INLINE")
object MathUtils : Helper {
    @JvmStatic
    fun Entity.getInterpolatedRenderPos(ticks: Float): Vec3d {
        return getInterpolatedPos(this, ticks).subtract(mc.renderManager.renderPosX, mc.renderManager.renderPosY, mc.renderManager.renderPosZ)
    }

    @JvmStatic
    fun getInterpolateVec3dPos(pos: Vec3d, renderPartialTicks: Float): Vec3d {
        return Vec3d(calculateDistanceWithPartialTicks(pos.x, pos.x, renderPartialTicks) - mc.renderManager.renderPosX, calculateDistanceWithPartialTicks(pos.y, pos.y - 0.021, renderPartialTicks) - mc.renderManager.renderPosY, calculateDistanceWithPartialTicks(pos.z, pos.z, renderPartialTicks) - mc.renderManager.renderPosZ)
    }

    @JvmStatic
    fun getInterpolateEntityClose(entity: Entity, renderPartialTicks: Float): Vec3d {
        return Vec3d(calculateDistanceWithPartialTicks(entity.posX, entity.lastTickPosX, renderPartialTicks) - mc.renderManager.renderPosX, calculateDistanceWithPartialTicks(entity.posY, entity.lastTickPosY, renderPartialTicks) - mc.renderManager.renderPosY, calculateDistanceWithPartialTicks(entity.posZ, entity.lastTickPosZ, renderPartialTicks) - mc.renderManager.renderPosZ)
    }

    @JvmStatic
    fun getInterpolatedPos(entity: Entity, ticks: Float): Vec3d {
        return Vec3d(entity.lastTickPosX, entity.lastTickPosY, entity.lastTickPosZ).add(getInterpolatedAmount(entity, ticks))
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, vec: Vec3d): Vec3d {
        return getInterpolatedAmount(entity, vec.x, vec.y, vec.z)
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, ticks: Float): Vec3d {
        return getInterpolatedAmount(entity, ticks.toDouble(), ticks.toDouble(), ticks.toDouble())
    }

    @JvmStatic
    fun getInterpolatedAmount(entity: Entity, x: Double, y: Double, z: Double): Vec3d {
        return Vec3d((entity.posX - entity.lastTickPosX) * x, (entity.posY - entity.lastTickPosY) * y, (entity.posZ - entity.lastTickPosZ) * z)
    }

    private inline fun calculateDistanceWithPartialTicks(n: Double, n2: Double, renderPartialTicks: Float): Double {
        return n2 + (n - n2) * renderPartialTicks
    }


    fun direction(yaw: Float): Vec2f {
        return Vec2f(cos(degToRad(yaw + 90.0)).toFloat(), sin(degToRad(yaw + 90.0)).toFloat())
    }


    fun radToDeg(rad: Double): Double {
        return rad * 57.295780181884766
    }

    fun degToRad(deg: Double): Double {
        return deg * 0.01745329238474369
    }

    @JvmStatic
    fun directionSpeed(speed: Double): DoubleArray {
        val mc = Minecraft.getMinecraft()
        var forward: Float
        forward = mc.player.movementInput.moveForward
        var side = mc.player.movementInput.moveStrafe
        var yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.renderPartialTicks
        if (forward != 0.0f) {
            if (side > 0.0f) {
                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
            } else if (side < 0.0f) {
                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
            }
            side = 0.0f
            if (forward > 0.0f) {
                forward = 1.0f
            } else if (forward < 0.0f) {
                forward = -1.0f
            }
        }
        val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))
        val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
        val posX = forward * speed * cos + side * speed * sin
        val posZ = forward * speed * sin - side * speed * cos
        return doubleArrayOf(posX, posZ)
    }

    @JvmStatic
    inline fun ceilToPOT(valueIn: Int): Int {
        // Magical bit shifting
        var i = valueIn
        i--
        i = i or (i shr 1)
        i = i or (i shr 2)
        i = i or (i shr 4)
        i = i or (i shr 8)
        i = i or (i shr 16)
        i++
        return i
    }

    @JvmStatic
    inline fun round(value: Float, places: Int): Float {
        val scale = 10.0f.pow(places)
        return round(value * scale) / scale
    }

    @JvmStatic
    inline fun round(value: Double, places: Int): Double {
        val scale = 10.0.pow(places)
        return round(value * scale) / scale
    }

    @JvmStatic
    inline fun decimalPlaces(value: Double) = value.toString().split('.').getOrElse(1) { "0" }.length

    @JvmStatic
    inline fun decimalPlaces(value: Float) = value.toString().split('.').getOrElse(1) { "0" }.length

    @JvmStatic
    inline fun isNumberEven(i: Int): Boolean {
        return i and 1 == 0
    }

    @JvmStatic
    inline fun reverseNumber(num: Int, min: Int, max: Int): Int {
        return max + min - num
    }

    @JvmStatic
    inline fun convertRange(valueIn: Int, minIn: Int, maxIn: Int, minOut: Int, maxOut: Int): Int {
        return convertRange(valueIn.toDouble(), minIn.toDouble(), maxIn.toDouble(), minOut.toDouble(), maxOut.toDouble()).toInt()
    }

    @JvmStatic
    inline fun convertRange(valueIn: Float, minIn: Float, maxIn: Float, minOut: Float, maxOut: Float): Float {
        return convertRange(valueIn.toDouble(), minIn.toDouble(), maxIn.toDouble(), minOut.toDouble(), maxOut.toDouble()).toFloat()
    }

    @JvmStatic
    inline fun convertRange(valueIn: Double, minIn: Double, maxIn: Double, minOut: Double, maxOut: Double): Double {
        val rangeIn = maxIn - minIn
        val rangeOut = maxOut - minOut
        val convertedIn = (valueIn - minIn) * (rangeOut / rangeIn) + minOut
        val actualMin = min(minOut, maxOut)
        val actualMax = max(minOut, maxOut)
        return min(max(convertedIn, actualMin), actualMax)
    }

    @JvmStatic
    inline fun lerp(from: Double, to: Double, delta: Double): Double {
        return from + (to - from) * delta
    }

    @JvmStatic
    inline fun lerp(from: Float, to: Float, delta: Float): Float {
        return from + (to - from) * delta
    }

    @JvmStatic
    inline fun Float.randomPolarity(): Float {
        return if (Random.nextBoolean()) -this else +this
    }

    @JvmStatic
    fun isNearlyZero(value: Double): Boolean {
        return isNearlyZero(value, SMALL_NUMBER.toDouble())
    }

    @JvmStatic
    inline fun isNearlyZero(value: Double, tolerance: Double): Boolean {
        return abs(value) <= tolerance
    }

    @JvmStatic
    fun isNearlyZero(value: Float): Boolean {
        return isNearlyZero(value, SMALL_NUMBER)
    }

    @JvmStatic
    inline fun isNearlyZero(value: Float, tolerance: Float): Boolean {
        return abs(value) <= tolerance
    }

    @JvmStatic
    fun isNearlyEqual(a: Double, b: Double): Boolean {
        return isNearlyEqual(a, b, KINDA_SMALL_NUMBER.toDouble())
    }

    @JvmStatic
    inline fun isNearlyEqual(a: Double, b: Double, tolerance: Double): Boolean {
        return abs(a - b) <= tolerance
    }

    @JvmStatic
    fun isNearlyEqual(a: Float, b: Float): Boolean {
        return isNearlyEqual(a, b, KINDA_SMALL_NUMBER)
    }

    @JvmStatic
    inline fun isNearlyEqual(a: Float, b: Float, tolerance: Float): Boolean {
        return abs(a - b) <= tolerance
    }

    @JvmStatic
    fun vDistance(v1: IntArray, v2: IntArray): Float {
        var current = 0.0
        for (i in v1.indices) {
            current += (v2[i] - v1[i]).toDouble().pow(2.0)
        }
        return sqrt(current).toFloat()
    }

    @JvmStatic
    fun vMul(v1: IntArray, v2: IntArray): IntArray {
        val out = IntArray(v1.size)
        for (i in v1.indices) {
            out[i] = v1[i] * v2[i]
        }
        return out
    }

    @JvmStatic
    fun vMul(v1: IntArray, c2: Float): IntArray {
        val out = IntArray(v1.size)
        for (i in v1.indices) {
            out[i] = (v1[i] * c2).toInt()
        }
        return out
    }

    @JvmStatic
    fun vSub(v1: IntArray, v2: IntArray): IntArray {
        val out = IntArray(v1.size)
        for (i in v1.indices) {
            out[i] = v1[i] - v2[i]
        }
        return out
    }

    @JvmStatic
    fun vAdd(v1: IntArray, v2: IntArray): IntArray {
        val out = IntArray(v1.size)
        for (i in v1.indices) {
            out[i] = v1[i] + v2[i]
        }
        return out
    }

    @JvmStatic
    fun cToV4(color: Color): IntArray {
        return intArrayOf(color.red, color.green, color.blue, color.alpha)
    }

    @JvmStatic
    inline fun cToV4(rgb: Int): IntArray {
        return intArrayOf(rgb shr 16 and 0xFF, rgb shr 8 and 0xFF, rgb shr 0 and 0xFF, rgb shr 24 and 0xff)
    }

    @JvmStatic
    inline fun v4ToC(v4: IntArray): Color {
        return Color(v4[0], v4[1], v4[2], v4[3])
    }


    inline val AxisAlignedBB.xCenter get() = minX + xLength * 0.5

    inline val AxisAlignedBB.yCenter get() = minY + yLength * 0.5

    inline val AxisAlignedBB.zCenter get() = minZ + zLength * 0.5

    inline val AxisAlignedBB.xLength get() = maxX - minX

    inline val AxisAlignedBB.yLength get() = maxY - minY

    inline val AxisAlignedBB.zLength get() = maxY - minY

    inline val AxisAlignedBB.lengths get() = Vec3d(xLength, yLength, zLength)

    fun AxisAlignedBB.scale(multiplier: Double): AxisAlignedBB {
        return this.scale(multiplier, multiplier, multiplier)
    }

    fun AxisAlignedBB.scale(x: Double, y: Double, z: Double): AxisAlignedBB {
        val halfXLength = this.xLength * 0.5
        val halfYLength = this.yLength * 0.5
        val halfZLength = this.zLength * 0.5

        return this.grow(halfXLength * (x - 1.0), halfYLength * (y - 1.0), halfZLength * (z - 1.0))
    }
}