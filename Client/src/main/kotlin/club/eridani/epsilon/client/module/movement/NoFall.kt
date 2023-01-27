package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.isBlockUnder
import club.eridani.epsilon.client.event.decentralized.events.player.OnUpdateWalkingPlayerDecentralizedEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.EntityUtil
import net.minecraft.init.Items
import net.minecraft.init.MobEffects
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.util.EnumHand
import net.minecraft.util.math.RayTraceResult


object NoFall : Module(name = "NoFall", category = Category.Misc, description = "Prevents fall damage") {
    val mode by setting("Mode", Mode.Packet)
    private var last = 0L
    private var fall = 0.0

    init {
        listener<OnUpdateWalkingPlayerDecentralizedEvent.OnUpdateWalkingPlayerData> {
            when (mode) {
                Mode.Packet -> {
                    val f = if (Speed.isEnabled) 2.85f else 2.25f
                    if (mc.player.fallDistance > f + getActivePotionEffect().toFloat()) {
                        if (mc.player.isBlockUnder) {
                            mc.player.connection.sendPacket(CPacketPlayer(true))
                            mc.player.fallDistance = 0.0f
                        } else if (!mc.player.onGround && mc.player.fallDistance <= 8.25f) {
                            mc.player.connection.sendPacket(CPacketPlayer(true))
                        }
                    }
                }
                Mode.Bucket -> {
                    val posVec = mc.player.positionVector
                    val result = mc.world.rayTraceBlocks(posVec, posVec.add(0.0, -5.33, 0.0), true, true, false)
                    if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
                        var hand = EnumHand.MAIN_HAND
                        if (mc.player.heldItemOffhand.item === Items.WATER_BUCKET) hand = EnumHand.OFF_HAND else if (mc.player.heldItemMainhand.item != Items.WATER_BUCKET) {
                            for (i in 0..8) if (mc.player.inventory.getStackInSlot(i).item === Items.WATER_BUCKET) {
                                mc.player.inventory.currentItem = i
                                mc.player.rotationPitch = 90f
                                last = System.currentTimeMillis()
                                return@listener
                            }
                            return@listener
                        }
                        mc.player.rotationPitch = 90f
                        mc.playerController.processRightClick(mc.player, mc.world, hand)
                        last = System.currentTimeMillis()
                    }
                }
                Mode.Hypixel -> {
                    if (!EntityUtil.isOnGround(0.001)) {
                        if (mc.player.motionY < -0.08) fall -= mc.player.motionY;
                        if (fall > 2) {
                            fall = 0.0
                            mc.player.onGround = true
                        }
                    } else {
                        fall = 0.0
                    }
                }
                Mode.AAC -> {
                    if (mc.player.ticksExisted == 1) {
                        val p = CPacketPlayer.Position(mc.player.posX, Double.NaN, mc.player.posZ, true)
                        mc.player.connection.sendPacket(p)
                    }
                }
                Mode.AAC2 -> {
                    if (mc.player.fallDistance > 2.0f) {
                        mc.player.motionZ = 0.0
                        mc.player.motionX = 0.0
                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.001, mc.player.posZ, mc.player.onGround))
                        mc.player.connection.sendPacket(CPacketPlayer(true))
                    }
                }
            }
        }
    }


    private fun getActivePotionEffect(): Int {
        return if (mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST)!!.amplifier + 1
        } else 0
    }

    override fun onEnable() {
        if (mc.world != null && mode == Mode.AAC) {
            val p = CPacketPlayer.Position(mc.player.posX, Double.NaN, mc.player.posZ, true)
            mc.player.connection.sendPacket(p)
        }
        fall = 0.0
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    enum class Mode {
        Bucket, Packet, AAC, AAC2, Hypixel
    }

}