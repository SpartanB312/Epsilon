package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.decentralized.decentralizedListener
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.management.FriendManager.isFriend
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.RotationUtil.getRotationsGucel
import club.eridani.epsilon.client.util.Utils
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceResult
import kotlin.math.abs


object AimAssist : Module(
    name = "AimAssist",
    category = Category.Combat,
    description = "Assist your aim"
) {

    private val clickAim by setting("ClickToAim", false)
    private val invisible by setting("Invisible", false)
    private val speed by setting("Speed", 1f, 0f..5f, 0.1f)
    private val range by setting("Range", 3.67f, 0f..10f, 0.1f)
    private val angleMin by setting("AngleMin", 0f, 0f..1f, 0.1f)
    private val angleMax by setting("AngleMax", 100.0f, 0f..360f, 1f)
    private val fov by setting("FOV", 90.0f, 0f..180f, 1f)

    init {
        decentralizedListener(OnUpdateWalkingPlayerDecentralizedEvent.Pre) {
            if (Utils.nullCheck()) return@decentralizedListener

            if (clickAim && !mc.gameSettings.keyBindAttack.isKeyDown)
                return@decentralizedListener

            if (mc.objectMouseOver?.typeOfHit == RayTraceResult.Type.ENTITY) {
                val entity = mc.objectMouseOver.entityHit
                if (entity is EntityPlayer) {
                    return@decentralizedListener
                }
            }

            val target = findTargetInFov() ?: return@decentralizedListener

            repeat(10) {
                val rotations = smoothAngle(
                    target.rotations,
                    floatArrayOf(mc.player.rotationYaw, mc.player.rotationPitch)
                )

                val yawDiff = getDistanceBetweenAngles(rotations[0], mc.player.rotationYaw)
                val pitchDiff = getDistanceBetweenAngles(rotations[1], mc.player.rotationPitch)

                if (pitchDiff <= angleMax && yawDiff >= angleMin && yawDiff <= angleMax) {
                    mc.player.rotationYaw = getRotation(mc.player.rotationYaw, rotations[0], speed)
                    mc.player.rotationPitch = getRotation(mc.player.rotationPitch, rotations[1], speed)
                }
            }
        }
    }


    private fun getRotation(currentRotation: Float, targetRotation: Float, maxIncrement: Float): Float {
        var deltaAngle: Float = MathHelper.wrapDegrees(targetRotation - currentRotation)
        if (deltaAngle > maxIncrement) {
            deltaAngle = maxIncrement
        }
        if (deltaAngle < -maxIncrement) {
            deltaAngle = -maxIncrement
        }
        return currentRotation + deltaAngle / 2.0f
    }

    private fun smoothAngle(dst: FloatArray, src: FloatArray): FloatArray {
        var smoothedAngle = FloatArray(2)
        smoothedAngle[0] = src[0] - dst[0]
        smoothedAngle[1] = src[1] - dst[1]
        smoothedAngle = constrainAngle(smoothedAngle)
        smoothedAngle[0] = src[0] - smoothedAngle[0] / 100 * (14..24).random()
        smoothedAngle[1] = src[1] - smoothedAngle[1] / 100 * (3..8).random()
        return smoothedAngle
    }

    private fun constrainAngle(vector: FloatArray): FloatArray {
        vector[0] %= 360.0f
        vector[1] %= 360.0f
        while (vector[0] <= -180.0f) {
            vector[0] += 360.0f
        }
        while (vector[1] <= -180.0f) {
            vector[1] += 360.0f
        }
        while (vector[0] > 180.0f) {
            vector[0] -= 360.0f
        }
        while (vector[1] > 180.0f) {
            vector[1] -= 360.0f
        }
        return vector
    }

    private fun getDistanceBetweenAngles(angle1: Float, angle2: Float): Float {
        var distance = abs(angle1 - angle2) % 360.0f
        if (distance > 180.0f) {
            distance = 360.0f - distance
        }
        return distance
    }

    private fun findTargetInFov(): AimAssistTarget? {
        var closestTarget: AimAssistTarget? = null
        for (target in mc.world.playerEntities) {
            if (target == mc.player) continue
            if (isFriend(target)) continue
            if (target.health <= 0) continue
            if (mc.player.getDistance(target) > range) continue
            if (invisible && target.isInvisible) continue

            val rotations = getRotationsGucel(target)
            val inFovX = MathHelper.abs(MathHelper.wrapDegrees(rotations[0] - mc.player.rotationYaw)) <= fov
            val inFovY = MathHelper.abs(MathHelper.wrapDegrees(rotations[1] - mc.player.rotationPitch)) <= fov
            if (!inFovX || !inFovY) continue
            if (closestTarget == null) {
                closestTarget = AimAssistTarget(target, rotations)
                continue
            }
            if (mc.player.getDistance(target) < mc.player.getDistance(closestTarget.player)) closestTarget =
                AimAssistTarget(target, rotations)

        }
        return closestTarget
    }

    internal class AimAssistTarget(val player: EntityPlayer, val rotations: FloatArray)
}