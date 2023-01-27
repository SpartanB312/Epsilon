package club.eridani.epsilon.client.util

import club.eridani.epsilon.client.common.interfaces.Helper
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import kotlin.math.*


object RotationUtil : Helper {

    fun faceEntitySmooth(
        curYaw: Double,
        curPitch: Double,
        intendedYaw: Double,
        intendedPitch: Double,
        yawSpeed: Double,
        pitchSpeed: Double
    ): FloatArray {
        val yaw: Float = updateRotation(
            curYaw.toFloat(),
            intendedYaw.toFloat(), yawSpeed.toFloat()
        )
        val pitch: Float = updateRotation(
            curPitch.toFloat(),
            intendedPitch.toFloat(), pitchSpeed.toFloat()
        )
        return floatArrayOf(yaw, pitch)
    }


    fun updateRotation(current: Float, intended: Float, factor: Float): Float {
        var var4 = MathHelper.wrapDegrees(intended - current)
        if (var4 > factor) {
            var4 = factor
        }
        if (var4 < -factor) {
            var4 = -factor
        }
        return current + var4
    }

    fun faceVectorPacketInstant(vec: Vec3d) {
        val rotations: FloatArray = getLegitRotations(vec)
        mc.player.connection.sendPacket(
            CPacketPlayer.Rotation(
                rotations[0],
                rotations[1],
                mc.player.onGround
            )
        )
    }

    fun rayCast(entity: Entity, yaw: Float, pitch: Float, range: Double) : Boolean {
        val otherBB: AxisAlignedBB = entity.entityBoundingBox
        val collisionBorderSize = entity.collisionBorderSize
        val targetHitbox = otherBB.expand(collisionBorderSize.toDouble(), collisionBorderSize.toDouble(), collisionBorderSize.toDouble())
        val eyePos = mc.player.getPositionEyes(1.0f)
        val d = cos(-yaw * 0.017453292 - Math.PI)
        val d1 = sin(-yaw * 0.017453292 - Math.PI)
        val d2 = -cos(-pitch * 0.017453292)
        val d3 = sin(-pitch * 0.017453292)
        val lookPos = Vec3d((d1 * d2), d3, (d * d2))
        val adjustedPos = eyePos.add(lookPos.x * range, lookPos.y * range, lookPos.z * range)
        val rayTrace = targetHitbox.calculateIntercept(eyePos, adjustedPos)
        return rayTrace != null
    }

    fun getLegitRotations(vec: Vec3d): FloatArray {
        val eyesPos: Vec3d = getEyesPos()
        val diffX = vec.x - eyesPos.x
        val diffY = vec.y - eyesPos.y
        val diffZ = vec.z - eyesPos.z
        val diffXZ = sqrt(diffX * diffX + diffZ * diffZ)
        val yaw = Math.toDegrees(atan2(diffZ, diffX)).toFloat() - 90f
        val pitch = (-Math.toDegrees(atan2(diffY, diffXZ))).toFloat()
        return floatArrayOf(
            mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw),
            mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)
        )
    }

    fun getBlockRotations(paramBlockPos: BlockPos, paramEnumFacing: EnumFacing): FloatArray {
        val d1: Double = paramBlockPos.x + 0.5 - mc.player.posX + paramEnumFacing.xOffset / 2.0
        val d2: Double = paramBlockPos.z + 0.5 - mc.player.posZ + paramEnumFacing.zOffset / 2.0
        val d3: Double = mc.player.posY + mc.player.getEyeHeight() - (paramBlockPos.y + 0.5)
        val d4: Double = sqrt(d1 * d1 + d2 * d2)
        var f1 = (atan2(d2, d1) * 180.0 / Math.PI).toFloat() - 90.0f
        val f2 = (atan2(d3, d4) * 180.0 / Math.PI).toFloat()
        if (f1 < 0.0f) {
            f1 += 360.0f
        }
        f1 = normalizeAngle(f1)
        return floatArrayOf(f1, f2)
    }

    fun getRotationsGucel(entity: Entity?): FloatArray {
        entity ?: return floatArrayOf()
        var xDiff = entity.posX - Wrapper.mc.player.posX
        val yDiff = entity.posY - Wrapper.mc.player.posY
        var zDiff = entity.posZ - Wrapper.mc.player.posZ

        var newYaw = Math.toDegrees(-atan(xDiff / zDiff)).toFloat()
        val toDegrees = Math.toDegrees(atan(zDiff / xDiff))
        if (zDiff < 0.0 && xDiff < 0.0) {
            newYaw = (90.0 + toDegrees).toFloat()
        } else if (zDiff < 0.0 && xDiff > 0.0) {
            newYaw = (-90.0 + toDegrees).toFloat()
        }
        var newPitch = (-atan2(
            entity.posY - (Wrapper.mc.player.posY + Wrapper.mc.player.getEyeHeight()
                .toDouble()), hypot(xDiff, zDiff)
        ) * 180.0 / Math.PI).toFloat()
        if (yDiff > -0.25 && yDiff < 0.25) {
            newPitch = (-atan2(
                ((entity.posY + entity.eyeHeight / HitLocation.CHEST.offset
                        - (Wrapper.mc.player.posY + Wrapper.mc.player.getEyeHeight()))),
                hypot(xDiff, zDiff)
            ) * 180.0 / Math.PI).toFloat()
        } else if (yDiff > -0.25) {
            newPitch = (-atan2(
                ((entity.posY + entity.eyeHeight / HitLocation.FEET.offset
                        - (Wrapper.mc.player.posY + Wrapper.mc.player.getEyeHeight()))),
                hypot(xDiff, zDiff)
            ) * 180.0 / Math.PI).toFloat()
        } else if (yDiff < 0.25) {
            newPitch = (-atan2(
                ((entity.posY + entity.eyeHeight / HitLocation.HEAD.offset
                        - (Wrapper.mc.player.posY + Wrapper.mc.player.getEyeHeight()))),
                hypot(xDiff, zDiff)
            ) * 180.0 / Math.PI).toFloat()
        }
        return floatArrayOf(newYaw, newPitch)
    }

    private fun getEyesPos(): Vec3d {
        return Vec3d(
            mc.player.posX,
            mc.player.posY + mc.player.getEyeHeight(),
            mc.player.posZ
        )
    }

    fun EntityPlayerSP.legitYaw(yaw: Float): Float {
        return this.rotationYaw + normalizeAngle(yaw - this.rotationYaw)
    }

    val EnumFacing.yaw: Float
        get() = when (this) {
            EnumFacing.NORTH -> -180.0f
            EnumFacing.SOUTH -> 0.0f
            EnumFacing.EAST -> -90.0f
            EnumFacing.WEST -> 90.0f
            else -> 0.0f
        }

    fun normalizeAngle(angleIn: Float): Float {
        var angle = angleIn
        angle %= 360.0f

        if (angle >= 180.0f) {
            angle -= 360.0f
        } else if (angle < -180.0f) {
            angle += 360.0f
        }

        return angle
    }

    internal enum class HitLocation(val offset: Double) {
        HEAD(1.0), CHEST(1.5), FEET(3.5);
    }
}