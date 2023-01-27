package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.combat.TargetStrafe
import club.eridani.epsilon.client.util.EntityUtil
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.WorldTimer
import club.eridani.epsilon.client.util.math.sq
import net.minecraft.block.Block
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.init.MobEffects
import net.minecraft.util.math.BlockPos
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


object Speed : Module(name = "Speed", category = Category.Movement, description = "Make you move faster") {
    val mode by setting("Mode", Mode.NCP)
    private val useTimer by setting("UseTimer", false)
    private val inWater by setting("WaterSpeed", false)
    private val bbtt by setting("2b2t", false) { mode == Mode.NCP }
    private val acc by setting("Acceleration", 2149, 1000..2500, 1) { mode == Mode.NCP }
    private val maxMoveSpeed by setting("MaxSpeed", 0.548, 0.1..2.0, 0.01) { mode == Mode.NCP }

    private val boost by setting("HurtBoost", false)
    private val hurtTime by setting("HurtTime", 1, 0..20, 1) { boost }
    private val multiplySpeed by setting("BoostSpeed", 1f, 1f..1.5f, 0.01f) { boost }
    private var lastDist = 0.0

    val timer = WorldTimer()
    private var level = 1
    private var speedStage = 0
    private var moveSpeed = 0.2873

    init {
        listener<PlayerMoveEvent.Post> {
            val xDist = mc.player.posX - mc.player.prevPosX
            val zDist = mc.player.posZ - mc.player.prevPosZ
            lastDist = sqrt(xDist * xDist + zDist * zDist)
            speedStage++
        }

        safeListener<PlayerMoveEvent.Pre> { event ->

            if (speedCheck()) {
                return@safeListener
            }

            if (useTimer) {
                timer.setOverrideSpeed(1.088f)
            }

            when (mode) {
                Mode.Hypixle -> {
                    when (speedStage) {
                        0, 3 -> lastDist = 0.0
                        4 -> if (isMoving() && mc.player.onGround) {
                            var motionY = ThreadLocalRandom.current().nextDouble(0.4000199999, 0.4001199999)
                            if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) motionY += (mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)!!.amplifier + 1) * 0.1f

                            event.y = motionY.also { mc.player.motionY = it }
                            moveSpeed *= 1.88
                        }
                        5 -> {
                            moveSpeed += Random().nextDouble() / 4800
                            val difference = 0.68 * (lastDist - getBaseMoveSpeed())
                            moveSpeed = lastDist - difference
                        }
                        else -> {
                            if ((mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0)).size > 0 || mc.player.collidedVertically) && speedStage > 0) speedStage = if (!isMoving()) 0 else 2
                            moveSpeed = lastDist - lastDist / 159.0
                        }
                    }
                    if (speedStage > 0) {
                        moveSpeed = (moveSpeed - 0.02 * lastDist).coerceAtLeast(getBaseMoveSpeed())


                        if (boost && mc.player.hurtTime > hurtTime) {
                            moveSpeed *= multiplySpeed
                        }

                        EntityUtil.setMotion(event, moveSpeed)
                    }
                }
                Mode.NCP -> {
                    val pos = BlockPos(mc.player.posX, mc.player.posY + 0.5, mc.player.posZ)
                    val burrowed = mc.world.getBlockState(pos).getCollisionBoundingBox(mc.world, pos) == Block.FULL_BLOCK_AABB

                    if (mc.player.onGround && isMoving()) {
                        level = 2
                    }

                    if (round(mc.player.posY - mc.player.posY.toInt().toDouble()) == round(0.138)) {
                        val player = mc.player
                        player.motionY -= 0.08
                        event.y = event.y - 0.09316090325960147
                        player.posY -= 0.09316090325960147
                    }

                    if (level > 0) {
                        if (level == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f)) {
                            level = 2
                            moveSpeed = getBaseMoveSpeed().sq - 0.01
                        } else if (level == 2) {
                            level = 3
                            var motionY = if (burrowed) 0.42 else 0.399399995803833
                            if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
                                motionY += ((Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST))!!.amplifier + 1).toFloat() * 0.1f).toDouble()
                            }
                            event.y = motionY.also { mc.player.motionY = it }
                            moveSpeed *= acc / 1000.0
                            if (moveSpeed > maxMoveSpeed) {
                                moveSpeed = maxMoveSpeed
                            }
                        } else if (level == 3) {
                            level = 4
                            val difference = (if (bbtt) 0.795 else 0.66) * (lastDist - getBaseMoveSpeed())
                            moveSpeed = lastDist - difference
                        } else {
                            if (mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0)).size > 0 || mc.player.collidedVertically) level = 1
                            moveSpeed = lastDist - lastDist / 159.0
                        }

                        moveSpeed = if (burrowed) {
                            getBaseMoveSpeed()
                        } else {
                            moveSpeed.coerceAtLeast(getBaseMoveSpeed())
                        }

                        val movementInput = mc.player.movementInput
                        var forward = movementInput.moveForward
                        var strafe = movementInput.moveStrafe
                        var yaw = mc.player.rotationYaw

                        if (TargetStrafe.isEnabled) {
                            forward = TargetStrafe.forward
                            strafe = TargetStrafe.direction
                            yaw = TargetStrafe.yaw
                        }

                        if (forward == 0.0f && strafe == 0.0f) {
                            event.x = 0.0
                            event.z = 0.0
                            return@safeListener
                        } else if (forward != 0.0f) {
                            if (strafe >= 1.0f) {
                                yaw += (if (forward > 0.0f) -45 else 45).toFloat()
                                strafe = 0.0f
                            } else if (strafe <= -1.0f) {
                                yaw += (if (forward > 0.0f) 45 else -45).toFloat()
                                strafe = 0.0f
                            }
                            if (forward > 0.0f) {
                                forward = 1.0f
                            } else if (forward < 0.0f) {
                                forward = -1.0f
                            }
                        }

                        val cos = cos(Math.toRadians((yaw + 90.0f).toDouble()))
                        val sin = sin(Math.toRadians((yaw + 90.0f).toDouble()))

                        if (boost && mc.player.hurtTime > hurtTime) {
                            moveSpeed *= multiplySpeed
                        }

                        event.x = forward.toDouble() * moveSpeed * cos + strafe.toDouble() * moveSpeed * sin
                        event.z = forward.toDouble() * moveSpeed * sin - strafe.toDouble() * moveSpeed * cos
                    }
                }
            }
        }
    }

    private fun round(value: Double): Double {
        var bigDecimal = BigDecimal(value)
        bigDecimal = bigDecimal.setScale(3, RoundingMode.HALF_UP)
        return bigDecimal.toDouble()
    }

    private fun speedCheck(): Boolean {
        return mc.player.capabilities.isFlying || mc.player.isOnLadder || mc.player.isEntityInsideOpaqueBlock || (mc.player.isInWater || mc.player.isInLava) && !inWater
    }

    private fun isMoving() = !(mc.player.moveForward == 0f && mc.player.moveStrafing == 0f)


    fun getBaseMoveSpeed(): Double {
        val player = mc.player
        var base = 0.2873
        val moveSpeed = player.getActivePotionEffect(MobEffects.SPEED)
        val moveSlowness = player.getActivePotionEffect(MobEffects.SLOWNESS)
        if (moveSpeed != null) base *= 1.0 + 0.19 * (moveSpeed.amplifier + 1)
        if (moveSlowness != null) base *= 1.0 - 0.13 * (moveSlowness.amplifier + 1)
        if (player.isInWater) {
            base *= 0.5203619984250619
            val depthStriderLevel = EnchantmentHelper.getDepthStriderModifier(mc.player)
            if (depthStriderLevel > 0) {
                val doubles = doubleArrayOf(1.0, 1.4304347400741908, 1.7347825295420374, 1.9217391028296074)
                base *= doubles[depthStriderLevel]
            }
        } else if (player.isInLava) {
            base *= 0.5203619984250619
        }
        return base
    }

//    private fun getBaseMoveSpeed(): Double {
//        val amplifier: Int
//        var defaultSpeed = 0.2873
//        if (mc.player.isPotionActive(MobEffects.SPEED)) {
//            amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.SPEED))!!.amplifier
//            defaultSpeed *= 1.0 + 0.2 * (amplifier + 1).toDouble()
//        }
//        return defaultSpeed
//    }

    override fun onEnable() {
        if (Utils.nullCheck()) {
            return
        }
        moveSpeed = getBaseMoveSpeed()
        level = 0
        speedStage = 0
    }

    override fun onDisable() {
        if (Utils.nullCheck()) {
            return
        }
        timer.resetTime()
        moveSpeed = getBaseMoveSpeed()
        level = 0
    }

    enum class Mode {
        NCP, Hypixle
    }

    override fun getHudInfo(): String {
        return mode.name
    }
}