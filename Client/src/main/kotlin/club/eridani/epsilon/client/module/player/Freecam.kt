package club.eridani.epsilon.client.module.player

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.events.ConnectionEvent
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.events.PlayerPushEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.onPacketSend
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.common.extensions.*
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.entity.Entity
import net.minecraft.network.play.client.*
import net.minecraft.network.play.client.CPacketPlayer.PositionRotation


object Freecam :
    Module(name = "Freecam", category = Category.Player, description = "Leave your body and trascend into the realm of the gods") {

    private val speed by setting("Speed", 1f, 0.1f..10f, 0.1f)
    private val cancelPackets by setting("Cancel Packets", true)
    private val mode by setting("Mode", Mode.NCP)

    private var clonedPlayer: EntityOtherPlayerMP? = null
    private var posX = 0.0
    private var posY = 0.0
    private var posZ = 0.0
    private var pitch = 0f
    private var yaw = 0f
    private var isRidingEntity = false
    private var ridingEntity: Entity? = null

    override fun onEnable() {
        if (mc.world != null && mc.player != null) {
            isRidingEntity = mc.player.ridingEntity != null
            if (mc.player.ridingEntity == null) {
                posX = mc.player.posX
                posY = mc.player.posY
                posZ = mc.player.posZ
            } else {
                ridingEntity = mc.player.ridingEntity
                mc.player.dismountRidingEntity()
            }
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING))
            pitch = mc.player.rotationPitch
            yaw = mc.player.rotationYaw
            EntityOtherPlayerMP(mc.world, mc.session.profile).also { clonedPlayer = it }.copyLocationAndAnglesFrom(mc.player)
            clonedPlayer?.rotationYawHead = mc.player.rotationYawHead
            clonedPlayer?.inventory = mc.player.inventory
            mc.world.addEntityToWorld(-100, clonedPlayer)
        }
    }

    override fun onDisable() {
        if (!Utils.nullCheck()) {
            mc.player.setPositionAndRotation(posX, posY, posZ, yaw, pitch)
            mc.world.removeEntityFromWorld(-100)
            mc.player.noClip = false
            mc.player.motionZ = 0.0
            mc.player.motionY = 0.0
            mc.player.motionX = 0.0
            if (clonedPlayer != null)
                if (!mc.gameSettings.keyBindSneak.isKeyDown && clonedPlayer!!.isSneaking) {
                mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            }
            if (isRidingEntity) {
                mc.player.startRiding(ridingEntity, true)
            }
            mc.renderGlobal.loadRenderers()
        }
    }

    init {
        onTick {
            runSafe {
                mc.player.motionY = 0.0
                mc.player.motionX = 0.0
                mc.player.motionZ = 0.0
                if (mc.gameSettings.keyBindJump.isKeyDown) {
                    mc.player.motionY += speed
                }
                if (mc.gameSettings.keyBindSneak.isKeyDown) {
                    mc.player.motionY -= speed
                }
                club.eridani.epsilon.client.util.EntityUtil.setMotion(speed.toDouble(), mc.player)

                if (isRidingEntity && mc.player.ridingEntity != null) {
                    mc.player.ridingEntity?.motionY = 0.0
                }

                mc.player.noClip = true
                mc.player.onGround = false
                mc.player.fallDistance = 0.0f
            }
        }

        onPacketSend { event ->
            runSafe {
                clonedPlayer ?: return@runSafe
                if (mode == Mode.NCP) {
                    if (event.packet is CPacketEntityAction && (event.packet.action == CPacketEntityAction.Action.START_SNEAKING || event.packet.action == CPacketEntityAction.Action.STOP_SNEAKING)) {
                        event.cancel()
                    }
                    if (event.packet is CPacketPlayer || event.packet is CPacketPlayer.Rotation || event.packet is PositionRotation || event.packet is CPacketPlayer.Position) {
                        val packet = event.packet as CPacketPlayer
                        packet.x = clonedPlayer!!.posX
                        packet.y = clonedPlayer!!.posY
                        packet.z = clonedPlayer!!.posZ
                        packet.yaw = clonedPlayer!!.rotationYaw
                        packet.pitch = clonedPlayer!!.rotationPitch
                        event.cancel()
                    }
                    if (event.packet is CPacketInput) {
                        event.cancel()
                    }
                } else {
                    if (event.packet is CPacketPlayer || event.packet is CPacketInput) {
                        event.cancel()
                    }
                    if (cancelPackets && (event.packet is CPacketUseEntity || event.packet is CPacketVehicleMove)) {
                        event.cancel()
                    }
                }
            }
        }

        listener<PlayerMoveEvent.Pre> {
            runSafe {
                mc.player.noClip = true
            }
        }

        listener<ConnectionEvent.Disconnect>(Int.MAX_VALUE) {
            disable()
        }

        listener<PlayerPushEvent> { event ->
            if (event.type == PlayerPushEvent.Type.BLOCK) {
                event.cancel()
            }
        }
    }

    enum class Mode {
        Dupe, NCP
    }
}