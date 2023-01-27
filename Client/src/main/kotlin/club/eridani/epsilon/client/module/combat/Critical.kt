package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.isInWeb
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.Timer
import club.eridani.epsilon.client.util.onPacketSend
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.EntityLivingBase
import net.minecraft.item.ItemSword
import net.minecraft.network.play.client.CPacketPlayer
import net.minecraft.network.play.client.CPacketUseEntity
import java.util.*
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random.Default.nextBoolean

object Critical : Module(
    name = "Critical",
    category = Category.Combat,
    description = "Automatically does critical attacks"
) {
    private val swordOnly by setting("SwordOnly", true)
    private val syncKillAura by setting("SyncKillAura", false)
    private val mode by setting("Mode", Mode.Packet)
    private val packets by setting("Packets", 2, 1..4, 1) { mode == Mode.Packet }
    private val delay by setting("Delay", 1000, 0..5000, 1) { mode == Mode.NCP || mode == Mode.Hypixel || mode == Mode.AAC }
    private val timer = Timer()

    init {
        onPacketSend { event ->
            runSafe {
                if (swordOnly) {
                    if (mc.player.heldItemMainhand.item !is ItemSword) {
                        return@runSafe
                    }
                }
                if (event.packet is CPacketUseEntity && event.packet.action == CPacketUseEntity.Action.ATTACK) {
                    if (canCrit() && event.packet.getEntityFromWorld(mc.world) is EntityLivingBase) {
                        if (syncKillAura) {
                            if (KillAura.currentTarget == null || Objects.requireNonNull(event.packet.getEntityFromWorld(mc.world)) != KillAura.currentTarget) {
                                return@runSafe
                            }
                        }
                        when (mode) {
                            club.eridani.epsilon.client.module.combat.Critical.Mode.Packet -> {
                                when (packets) {
                                    1 -> {
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1f.toDouble(), mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                    }
                                    2 -> {
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.1E-5, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                    }
                                    3 -> {
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0625101, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0125, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                    }
                                    4 -> {
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1625, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 4.0E-6, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.0E-6, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false))
                                        mc.player.connection.sendPacket(CPacketPlayer())
                                        mc.player.onCriticalHit(Objects.requireNonNull(event.packet.getEntityFromWorld(mc.world)))
                                    }
                                }
                            }
                            club.eridani.epsilon.client.module.combat.Critical.Mode.NCP -> {
                                if (club.eridani.epsilon.client.module.combat.Critical.timer.passed(delay)) {
                                    club.eridani.epsilon.client.module.combat.Critical.timer.reset()
                                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.11, mc.player.posZ, false))
                                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.1100013579, mc.player.posZ, false))
                                    mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.3579E-6, mc.player.posZ, false))
                                    mc.player.onCriticalHit(Objects.requireNonNull(event.packet.getEntityFromWorld(mc.world)))
                                }
                            }
                            club.eridani.epsilon.client.module.combat.Critical.Mode.Hypixel -> {
                                if (club.eridani.epsilon.client.module.combat.Critical.timer.passed(delay)) {
                                    club.eridani.epsilon.client.module.combat.Critical.timer.reset()
                                    var packets: DoubleArray
                                    val size = doubleArrayOf(0.06 + ThreadLocalRandom.current().nextDouble(0.008),
                                        (if (nextBoolean()) 0.00925 else 0.006725) * (if (nextBoolean()) 0.98 else 0.99) + mc.player.ticksExisted % 0.00715 * 0.94).also { packets = it }.size
                                    var i = 0
                                    while (i < size) {
                                        val index = packets[i]
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + index, mc.player.posZ, false))
                                        ++i
                                    }
                                }
                            }
                            club.eridani.epsilon.client.module.combat.Critical.Mode.AAC -> {
                                if (club.eridani.epsilon.client.module.combat.Critical.timer.passed(delay)) {
                                    club.eridani.epsilon.client.module.combat.Critical.timer.reset()
                                    var packets: DoubleArray
                                    val size = doubleArrayOf(0.05250000001304, 0.00150000001304, 0.01400000001304, 0.00150000001304).also { packets = it }.size
                                    var i = 0
                                    while (i < size) {
                                        val index = packets[i]
                                        mc.player.connection.sendPacket(CPacketPlayer.Position(mc.player.posX, mc.player.posY + index, mc.player.posZ, false))
                                        ++i
                                    }
                                }
                            }
                            club.eridani.epsilon.client.module.combat.Critical.Mode.Jump -> {
                                mc.player.motionY = 0.1
                                mc.player.fallDistance = 0.1f
                                mc.player.onGround = false
                            }
                        }
                    }
                }
            }
        }
    }

    private fun canCrit(): Boolean {
        return (mc.player.onGround && !mc.player.isInWeb && !mc.player.isInWater && !mc.player.isInLava && mc.player.ridingEntity == null)
    }

    override fun getHudInfo(): String {
        return mode.name
    }


    enum class Mode {
        Packet, NCP, AAC, Hypixel, Jump
    }
}