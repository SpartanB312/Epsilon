package club.eridani.epsilon.client.module.movement

import baritone.api.utils.Helper
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.movement.Speed.getBaseMoveSpeed
import club.eridani.epsilon.client.util.EntityUtil
import club.eridani.epsilon.client.util.onTick
import net.minecraft.init.MobEffects
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt


object LongJump : Module(name = "LongJump", description = "Make you can jump a long distance", category = Category.Movement) {
    val mode by setting("Mode", Mode.Normal)
    val damage = setting("Damage", false)
    private val autoDisable = setting("AutoDisable", true)

    private var lastDif = 0.0
    private var moveSpeed = 0.0
    private var stage = 0
    private var groundTicks = 0
    private var disableNCP = false



    init {

        onTick {
            val player = Helper.mc.player

            if (mode == Mode.Hypixel) {
                if (player.onGround && player.collidedVertically) {
                    Helper.mc.player.posY += 7.435E-4
                }
            }

            val xDif = player.posX - player.prevPosX
            val zDif = player.posZ - player.prevPosZ
            lastDif = sqrt(xDif * xDif + zDif * zDif)

            if (EntityUtil.isMoving(Helper.mc.player) && player.onGround && player.collidedVertically && stage > 2) {
                ++groundTicks
            }
            if (groundTicks > 1) {
                if (autoDisable.value) toggle()
            }
        }

        listener<PlayerMoveEvent.Pre> { event ->
            if (mode == Mode.Normal) {
                if (stage == 1 && EntityUtil.isMoving(mc.player)) {
                    stage = 2
                    moveSpeed = 3.0 * getBaseMoveSpeed() - 0.01
                } else if (stage == 2) {
                    stage = 3
                    mc.player.motionY = 0.424
                    event.y = 0.424
                    moveSpeed *= 2.149802
                } else if (stage == 3) {
                    stage = 4
                    val difference = 0.66 * (lastDif - getBaseMoveSpeed())
                    moveSpeed = lastDif - difference
                } else if (stage == 4) {
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0)).size > 0 || mc.player.collidedVertically) {
                        stage = 5
                    }
                    moveSpeed = lastDif - lastDif / 159.0
                } else if (stage == 5) {
                    if (autoDisable.value) toggle()
                    stage = 1
                } else if (stage == 0) {
                    stage = 1
                }
                moveSpeed = max(moveSpeed, getBaseMoveSpeed())
                var forward = mc.player.movementInput.moveForward
                var strafe = mc.player.movementInput.moveStrafe
                var yaw = mc.player.rotationYaw
                if (forward == 0.0f && strafe == 0.0f) {
                    event.y = 0.0
                    event.z = 0.0
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
                val mx = cos(Math.toRadians((yaw + 90.0f).toDouble()))
                val mz = sin(Math.toRadians((yaw + 90.0f).toDouble()))
                event.x = forward.toDouble() * moveSpeed * mx + strafe.toDouble() * moveSpeed * mz
                event.z = forward.toDouble() * moveSpeed * mz - strafe.toDouble() * moveSpeed * mx
                if (forward == 0.0f && strafe == 0.0f) {
                    event.x = 0.0
                    event.z = 0.0
                }
            } else if (mode == Mode.NCP) {
                if (BigDecimal(mc.player.posY - mc.player.posY.toInt().toDouble()).setScale(3, RoundingMode.HALF_UP).toDouble() == BigDecimal("0.41").setScale(3, RoundingMode.HALF_UP).toDouble()) {
                    mc.player.motionY = 0.0
                }
                if (mc.player.moveStrafing < 0.0f && mc.player.moveForward < 0.0f) {
                    stage = 1
                }
                if (BigDecimal(mc.player.posY - mc.player.posY.toInt().toDouble()).setScale(3, RoundingMode.HALF_UP).toDouble() == BigDecimal("0.943").setScale(3, RoundingMode.HALF_UP).toDouble()) {
                    mc.player.motionY = 0.0
                }
                if (stage == 1 && (mc.player.moveForward != 0.0f || mc.player.moveStrafing != 0.0f) && mc.player.collidedVertically) {
                    if (autoDisable.value) disableNCP = true
                    if (mc.player.onGround) {
                        stage = 2
                    }
                    moveSpeed = 0.83285
                } else if (stage == 2) {
                    stage = 3
                    mc.player.motionY = 0.424
                    event.y = 0.424
                    moveSpeed *= 2.149802
                } else if (stage == 3) {
                    stage = 4
                    val difference = 0.66 * (lastDif - 0.1873)
                    moveSpeed = lastDif - difference
                } else if (stage == 4) {
                    moveSpeed = lastDif - lastDif / 159.0
                    if (autoDisable.value && disableNCP) {
                        toggle()
                    }
                    if (mc.world.getCollisionBoxes(mc.player, mc.player.entityBoundingBox.offset(0.0, mc.player.motionY, 0.0)).size > 0 || mc.player.collidedVertically) {
                        stage = 1
                    }
                    if (event.y < 0.0) {
                        event.y = event.y * 0.67
                    }
                } else if (stage == 0) {
                    stage = 1
                }
                moveSpeed = max(moveSpeed, 0.2873)
                EntityUtil.setMotion(moveSpeed, mc.player)
            } else {
                val player = mc.player
                if (EntityUtil.isMoving(mc.player)) {
                    when (stage) {
                        0, 1 -> {
                            moveSpeed = 0.0
                        }
                        2 -> {
                            if (!player.onGround || !player.collidedVertically) return@listener
                            player.motionY = getJumpBoostModifier(0.3999999463558197)
                            event.y = player.motionY
                            moveSpeed = getBaseMoveSpeed() * 2.0
                        }
                        3 -> {
                            moveSpeed = getBaseMoveSpeed() * 2.149f.toDouble()
                        }
                        4 -> {
                            moveSpeed *= 1.6
                        }
                        else -> {
                            if (player.motionY < 0.0) {
                                player.motionY *= 0.5
                            }
                            moveSpeed = lastDif - lastDif / 159.0
                        }
                    }
                    moveSpeed = max(moveSpeed, getBaseMoveSpeed())
                    ++stage
                }
                EntityUtil.setMotion(moveSpeed, mc.player)
            }
        }
    }

    override fun onEnable() {
        lastDif = 0.0
        moveSpeed = 0.0
        stage = 0
        groundTicks = 1
        disableNCP = false

        if (damage.value) {
            EntityUtil.damagePlayer(false, 1.0);
        }
    }

    override fun onDisable() {

    }

    private fun getJumpBoostModifier(baseJumpHeight: Double): Double {
        var baseJumpHeight = baseJumpHeight
        if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            val amplifier = Objects.requireNonNull(mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST))!!.amplifier
            baseJumpHeight += ((amplifier + 1).toFloat() * 0.1f).toDouble()
        }
        return baseJumpHeight
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode {
        NCP,
        Normal,
        Hypixel
    }
}