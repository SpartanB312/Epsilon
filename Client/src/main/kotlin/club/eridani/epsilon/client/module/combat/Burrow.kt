package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.*
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.PlayerMoveEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.EntityManager
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.management.PlayerPacketManager.sendPlayerPacket
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ItemUtil
import club.eridani.epsilon.client.util.WorldTimer
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.math.sq
import club.eridani.epsilon.client.util.onPacketReceive
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.SPacketExplosion
import net.minecraft.network.play.server.SPacketPlayerPosLook
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

object Burrow :
    Module(name = "Burrow", alias = arrayOf("AutoSelfFill"), category = Category.Combat, description = "Flag yourself inside a block") {

    private val mode0 = setting("Mode", Mode.Instant)
    private val mode by mode0
    private val timer by setting("Timer", 4.0f, 1.0f..8.0f, 0.5f) { mode == Mode.Jump }
    private val breakCrystal by setting("Break Crystal", true)
    private val autoCenter by setting("Auto Center", true)
    private val rubberY by setting("Rubber Y", 20.0f, -20.0f..20.0f, 0.5f) { mode == Mode.Instant }
    private val timeoutTicks by setting("Timeout Ticks", 10, 0..100, 5)
    private val blocks by setting("Block", Block.Obby)


    var override: BlockPos? = null
    private var postBlockPos: BlockPos? = null
    private var timeout: Long = 0L

    private var blockPos: BlockPos? = null
    private var position: Vec3d? = null
    private var rotation: Vec2f? = null
    private var cancelMotion = false
    private var enabledTicks = 0

    private var velocityTime = 0L

    private val worldTimer = WorldTimer()

    init {
        listener<PlayerMoveEvent.Pre>(-3000, true) {
            runSafe {
                if (isEnabled) {
                    runTick()
                } else if (cancelMotion) {
                    it.x = 0.0
                    it.z = 0.0
                    player.motionX = 0.0
                    player.motionZ = 0.0
                    cancelMotion = false
                }
            }
        }

        onPacketReceive {
            runSafe {
                when (it.packet) {
                    is SPacketPlayerPosLook -> {
                        if (autoCenter && System.currentTimeMillis() < timeout) {
                            val pos = postBlockPos

                            if (pos != null && it.packet.y.toInt() == pos.y) {
                                club.eridani.epsilon.client.util.EntityUtil.centerPlayer(pos)
                            }

                            postBlockPos = null
                            timeout = 0L
                        }
                    }
                    is SPacketExplosion -> {
                        if (it.packet.y > 2.0 && velocityTime <= System.currentTimeMillis()) {
                            velocityTime = System.currentTimeMillis() + 3000L
                        }
                    }
                }
            }
        }

        onPacketReceive {
            runSafeTask {
                if (it.packet is SPacketPlayerPosLook) {
                    it.packet.rotationYaw = mc.player.rotationYaw
                    it.packet.rotationPitch = mc.player.rotationPitch
                }
            }
        }

    }

    override fun onDisable() {
        worldTimer.resetTime()

        override = null
        blockPos = null
        position = null
        rotation = null
        enabledTicks = 0

        velocityTime = 0L
    }

    private fun SafeClientEvent.runTick() {
        if (!tryRunTick() && enabledTicks++ >= timeoutTicks) {
            disable()
        }
    }

    private fun SafeClientEvent.tryRunTick(): Boolean {
        if (!player.onGround || (player.posY - player.prevPosY).sq > 0.01) {
            return false
        }

        val blockPos = override ?: world.getGroundPos(player).up()

        if (!canPlace(blockPos)) {
            return false
        }

        postBlockPos = null
        timeout = 0L

        val position = Vec3d(player.posX, player.posY, player.posZ)
        val rotation = PlayerPacketManager.rotation

        Burrow.blockPos = blockPos
        Burrow.position = position
        Burrow.rotation = rotation

        when (mode) {
            Mode.Instant -> instantMode(blockPos, position, rotation)
            Mode.Jump -> jumpMode(blockPos, position, rotation)
        }

        player.motionX = 0.0
        player.motionZ = 0.0

        cancelMotion = true
        enabledTicks = 0
        return true
    }

    private fun SafeClientEvent.canPlace(pos: BlockPos): Boolean {
        return kotlin.math.abs(player.posX - (pos.x + 0.5)) < 0.79 && kotlin.math.abs(player.posZ - (pos.z + 0.5)) < 0.79 && world.getBlockState(pos).isReplaceable && !world.getBlockState(pos.down()).isReplaceable && pos.up(2).let {
            world.getBlockState(it).getCollisionBoundingBox(world, it) == null
        } && AxisAlignedBB(pos).let { box ->
            EntityManager.entity.none { entity ->
                entity.isEntityAlive && (!breakCrystal || entity !is EntityEnderCrystal) && (entity !is EntityPlayer || entity != mc.player) && entity.collisionBoundingBox?.intersects(box) ?: false
            }
        }
    }


    private fun SafeClientEvent.jumpMode(blockPos: BlockPos, position: Vec3d, rotation: Vec2f) {
        val slot = getSlot() ?: run {
            disable()
            return
        }

        if (player.onGround) {
            player.jump()
        } else if (player.posY - position.y > 1.0) {
            if (breakCrystal) breakCrystal(blockPos)
            cancelPacket()

            placeBlock(slot, blockPos)

            connection.sendPacket(CPacketPlayer.PositionRotation(position.x, position.y + rubberY, position.z, rotation.x, rotation.y, false))
            connection.sendPacket(CPacketPlayer.PositionRotation(position.x, position.y, position.z, rotation.x, rotation.y, false))

            disable()
            return
        }

        worldTimer.setOverrideSpeed(timer)
        sendPlayerPacket {
            rotate(Vec2f(PlayerPacketManager.rotation.x, 90.0f))
        }
    }


    private fun SafeClientEvent.instantMode(blockPos: BlockPos, position: Vec3d, rotation: Vec2f) {
        getSlot()?.let {
            if (breakCrystal) breakCrystal(blockPos)
            cancelPacket()

            connection.sendPacket(CPacketPlayer.PositionRotation(position.x, position.y + 0.41999808688698, position.z, rotation.x, 90.0f, false))
            connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 0.7500019, position.z, false))
            connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 0.9999962, position.z, false))
            connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 1.17000380178814, position.z, false))
            connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 1.17001330178815, position.z, false))

            placeBlock(it, blockPos)

            connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 1.2426308013947485, position.z, false))
            if (velocityTime > System.currentTimeMillis()) {
                connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 3.3400880035762786, position.z, false))
                connection.sendPacket(CPacketPlayer.Position(position.x, position.y - 1.0, position.z, false))
            } else {
                connection.sendPacket(CPacketPlayer.Position(position.x, position.y + 2.3400880035762786, position.z, false))
            }
        }

        disable()
    }


    private fun getSlot(): Int? {
        val slot = if (blocks == Block.Obby) ItemUtil.findBlockInHotBar(Blocks.OBSIDIAN)
        else ItemUtil.findBlockInHotBar(Blocks.ENDER_CHEST)


        if (slot == -1) if (blocks == Block.Obby) ItemUtil.findBlockInHotBar(Blocks.ENDER_CHEST)
        else ItemUtil.findBlockInHotBar(Blocks.OBSIDIAN)

        return if (slot == -1) {
            ChatUtil.sendNoSpamErrorMessage("No obsidian in hotbar!")
            null
        } else {
            slot
        }
    }

    private fun cancelPacket() {
        sendPlayerPacket {
            cancelAll()
        }
    }

    private fun breakCrystal(blockPos: BlockPos) {
        val placeBB = AxisAlignedBB(blockPos)
        for (entity in mc.world.loadedEntityList) {
            if (entity is EntityEnderCrystal) {
                if (entity.getEntityBoundingBox().intersects(placeBB)) {
                    mc.player.connection.sendPacket(CPacketUseEntity(entity))
                    mc.player.swingArm(if (mc.player.heldItemOffhand.item == Items.END_CRYSTAL) EnumHand.OFF_HAND else EnumHand.MAIN_HAND)
                    mc.player.resetCooldown()
                    break
                }
            }
        }
    }

    private fun SafeClientEvent.placeBlock(slot: Int, blockPos: BlockPos) {
        val target = blockPos.down()
        val sneak = !player.isSneaking
        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

        val packet = CPacketPlayerTryUseItemOnBlock(target, EnumFacing.UP, EnumHand.MAIN_HAND, 0.5f, 1.0f, 0.5f)

        val lastSlot = mc.player.inventory.currentItem
        ItemUtil.swapToSlot(slot)
        connection.sendPacket(packet)

        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

        if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

        ItemUtil.swapToSlot(lastSlot)

        timeout = System.currentTimeMillis() + 100L
        postBlockPos = blockPos
    }

    private enum class Mode {
        Instant, Jump
    }

    private enum class Block {
        Obby, EnderChest
    }

}