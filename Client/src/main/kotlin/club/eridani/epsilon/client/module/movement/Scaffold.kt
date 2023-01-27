package club.eridani.epsilon.client.module.movement

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.boundingBox
import club.eridani.epsilon.client.common.extensions.isPlaceable
import club.eridani.epsilon.client.common.extensions.isReplaceable
import club.eridani.epsilon.client.event.events.OnUpdateWalkingPlayerEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.management.PlayerPacketManager.sendPlayerPacket
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.RotationUtil.legitYaw
import club.eridani.epsilon.client.util.RotationUtil.yaw
import club.eridani.epsilon.client.util.extension.AxisAlignedBB.interp
import club.eridani.epsilon.client.util.extension.block
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.threads.runSafe
import club.eridani.epsilon.client.util.world.getBlock
import net.minecraft.block.*
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.item.ItemBlock
import net.minecraft.item.ItemStack
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d


object Scaffold : Module(name = "Scaffold", category = Category.Movement, description = "Places blocks under you") {
    val mode by setting("Mode", Mode.NCP)
    private val desyncPlacement by setting("Desync Placement", true)
    private val delay by setting("Delay", 2.0, 0.0..20.0, .1)
    private val smoothRotation by setting("Smooth Rotation", true) { mode == Mode.Hypixel }
    private val yawSpeed by setting("YawSpeed", 90.0, 0.0..360.0, .1) { mode == Mode.Hypixel && smoothRotation }
    private val pitchSpeed by setting("PitchSpeed", 20.0, 0.0..90.0, .1) { mode == Mode.Hypixel && smoothRotation }
    val safeWalk by setting("SafeWalk", true)
    private val swing by setting("Swing", false)
    private val tower by setting("Tower", true)
    private val placeTimeout by setting("Place Timeout", 4, 1..20, 1)
    private val rotationTimeout by setting("Rotation Timeout", 10, 1..20, 1)
    private val keepRotation by setting("KeepRotation", true)
    private val render by setting("Render Block", true)

    private var lastPos: BlockPos? = null
    private var lastRotation: Vec2f? = null
    private var targetRotation: Vec2f? = null
    private var placing = false
    private var downwards = false
    private var ticks = 0
    private var currentHeldItem = 0
    private var placingPos: BlockPos? = null
    private var blockBelowData: BlockData? = null
    private var playerUnderPos: BlockPos? = null

    private val blockInHotbar: Int
        get() {
            if (mc.player.heldItemMainhand.item is Block && !ItemUtil.blackList.contains(mc.player.heldItemMainhand.item.block)) return mc.player.inventory.currentItem
            else for (i in 0..8) {
                if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || mc.player.inventory.getStackInSlot(i).item !is ItemBlock || !Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).item).defaultState.isFullBlock || ItemUtil.blackList.contains((mc.player.inventory.getStackInSlot(i).item as ItemBlock).block)) continue
                return i
            }
            return -1
        }
    private val countBlockInHotbar: Int
        get() {
            var count = 0
            for (i in 0..8) {
                if (mc.player.inventory.getStackInSlot(i) == ItemStack.EMPTY || mc.player.inventory.getStackInSlot(i).item !is ItemBlock || !Block.getBlockFromItem(mc.player.inventory.getStackInSlot(i).item).defaultState.isFullBlock || ItemUtil.blackList.contains((mc.player.inventory.getStackInSlot(i).item as ItemBlock).block)) continue
                count += mc.player.inventory.getStackInSlot(i).count
            }
            return count
        }


    init {
        onPacketPostReceive {
            if (it.packet is SPacketBlockChange && it.packet.blockPosition == lastPos && !it.packet.getBlockState().isReplaceable) {
                lastPos = null
            }
        }

        safeListener<OnUpdateWalkingPlayerEvent.Pre> {
            runSafe {
                club.eridani.epsilon.client.module.movement.Scaffold.ticks++

                val underPos = BlockPos(mc.player.posX, mc.player.posY - 1, mc.player.posZ)
                val blockData = find(Vec3d(0.0, if (mc.gameSettings.keyBindSneak.isKeyDown && !mc.gameSettings.keyBindJump.isKeyDown) -1.0 else 0.0, 0.0))

                if (blockData != null) {
                    targetRotation = if (mode == club.eridani.epsilon.client.module.movement.Scaffold.Mode.Hypixel) {
                        Vec2f(player.legitYaw(blockData.face.opposite.yaw), 82.545698881269785867f)
                    } else {
                        val rotationTo = club.eridani.epsilon.client.util.RotationUtil.getBlockRotations(blockData.position, blockData.face)
                        Vec2f(rotationTo[0], rotationTo[1])
                    }.also {
                        if (lastRotation == null) lastRotation = Vec2f(mc.player.rotationYaw, mc.player.rotationPitch)
                        //let last rotation smoothly change to target rotation
                        val smooth = if (mode == club.eridani.epsilon.client.module.movement.Scaffold.Mode.Hypixel) club.eridani.epsilon.client.util.RotationUtil.faceEntitySmooth(
                            lastRotation!!.x.toDouble(), lastRotation!!.y.toDouble(), it.x.toDouble(), it.y.toDouble(), yawSpeed, pitchSpeed
                        ) else floatArrayOf(it.x, it.y)
                        lastRotation = if (smoothRotation) Vec2f(smooth[0], smooth[1]) else it

                        if (tower) {
                            if (club.eridani.epsilon.client.module.movement.Scaffold.blockInHotbar != -1 && mc.gameSettings.keyBindJump.isKeyDown && !(mc.player.moveForward != 0f && mc.player.moveStrafing != 0f)) {
                                mc.player.motionX *= 0.8
                                mc.player.motionZ *= 0.8
                                mc.player.motionY = 0.41999976
                            }
                        }
                    }
                }

                lastRotation?.let {
                    if (blockData != null || club.eridani.epsilon.client.module.movement.Scaffold.ticks <= rotationTimeout) {
                        sendPlayerPacket {
                            rotate(it)
                        }
                    }
                }

                club.eridani.epsilon.client.module.movement.Scaffold.blockBelowData = blockData
                club.eridani.epsilon.client.module.movement.Scaffold.playerUnderPos = underPos.also { club.eridani.epsilon.client.module.movement.Scaffold.placingPos = it }
            }
        }

        safeListener<OnUpdateWalkingPlayerEvent.Post> {
            runSafe {
                if (mc.gameSettings.keyBindSneak.isPressed) {
                    if (!club.eridani.epsilon.client.module.movement.Scaffold.downwards) {
                        mc.player.isSneaking = false
                        club.eridani.epsilon.client.module.movement.Scaffold.downwards = true
                    }
                } else club.eridani.epsilon.client.module.movement.Scaffold.downwards = false

                val underPos = club.eridani.epsilon.client.module.movement.Scaffold.playerUnderPos ?: return@runSafe
                lastRotation ?: return@runSafe
                //cast the rotation is correct
                if (if (mode == club.eridani.epsilon.client.module.movement.Scaffold.Mode.Hypixel) (PlayerPacketManager.prevRotation.y == 82.545698881269785867f && PlayerPacketManager.rotation.y == 82.545698881269785867f)
                    else mc.world.getBlockState(underPos).material.isReplaceable
                ) {
                    club.eridani.epsilon.client.module.movement.Scaffold.blockBelowData?.let { blockData ->
                        if (club.eridani.epsilon.client.module.movement.Scaffold.ticks < delay) return@let
                        if (!desyncPlacement && club.eridani.epsilon.client.module.movement.Scaffold.lastPos == blockData.position && club.eridani.epsilon.client.module.movement.Scaffold.ticks < placeTimeout) return@let
                        if (!world.isPlaceable(underPos)) return@let

                        club.eridani.epsilon.client.module.movement.Scaffold.placing = true

                        if (club.eridani.epsilon.client.module.movement.Scaffold.blockInHotbar != -1) {
                            club.eridani.epsilon.client.module.movement.Scaffold.currentHeldItem = mc.player.inventory.currentItem
                            mc.player.inventory.currentItem = club.eridani.epsilon.client.module.movement.Scaffold.blockInHotbar
                            val itemStack = mc.player.getItemStackFromSlot(EntityEquipmentSlot.MAINHAND)
                            val block = (itemStack.item as? ItemBlock?)?.block ?: return@let
                            val metaData = itemStack.metadata
                            val vec = getHitVec(blockData.position, blockData.face)
                            val blockState = block.getStateForPlacement(world, blockData.position, blockData.face, vec.x.toFloat(), vec.y.toFloat(), vec.z.toFloat(), metaData, player, EnumHand.MAIN_HAND)
                            val sneak = !player.isSneaking && ItemUtil.blackList.contains(world.getBlock(blockData.position))

                            if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.START_SNEAKING))

                            mc.playerController.processRightClickBlock(mc.player, mc.world, blockData.position, blockData.face, vec, EnumHand.MAIN_HAND)

                            if (swing) mc.player.swingArm(EnumHand.MAIN_HAND)
                            else mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))

                            if (sneak) connection.sendPacket(CPacketEntityAction(player, CPacketEntityAction.Action.STOP_SNEAKING))

                            if (desyncPlacement) world.setBlockState(blockData.position, blockState)

                            mc.player.inventory.currentItem =
                                club.eridani.epsilon.client.module.movement.Scaffold.currentHeldItem
                            club.eridani.epsilon.client.module.movement.Scaffold.ticks = 0
                        }
                    }
                } else {
                    club.eridani.epsilon.client.module.movement.Scaffold.placing = false
                    if (keepRotation) lastRotation?.let {
                        sendPlayerPacket {
                            rotate(it)
                        }
                    }
                }
            }
        }

        onRender3D {
            if (!render) return@onRender3D
            val color = GUIManager.firstColor.alpha(63).toArgb()
            placingPos ?: return@onRender3D
            if (placing) {
                val iBlockState = mc.world.getBlockState(placingPos!!)
                RenderUtils3D.drawFullBox(iBlockState.getSelectedBoundingBox(mc.world, placingPos!!).interp(), 1f, color)
            }
        }

        onRender2D { event ->
            MainFontRenderer.drawStringWithShadow(countBlockInHotbar.toString(), event.resolution.scaledWidth / 2f + 4, event.resolution.scaledHeight / 2f + 4, GUIManager.firstColor)
        }
    }

    override fun onEnable() {
        if (Utils.nullCheck()) return
        lastRotation = Vec2f(mc.player.rotationYaw, mc.player.rotationPitch)
    }

    override fun onDisable() {
        ticks = 0
        lastPos = null
        lastRotation = null
        targetRotation = null
        placing = false
        downwards = false
        placingPos = null
        blockBelowData = null
        playerUnderPos = null
    }

    private fun getHitVec(pos: BlockPos, facing: EnumFacing): Vec3d {
        val vec = facing.directionVec
        return Vec3d(vec.x * 0.5 + 0.5 + pos.x, vec.y * 0.5 + 0.5 + pos.y, vec.z * 0.5 + 0.5 + pos.z)
    }

    private fun isPlaceable(pos: BlockPos): Boolean {
        val block = mc.world.getBlockState(pos).block
        val material = mc.world.getBlockState(pos).material
        return (material.isSolid || block is BlockLadder || block is BlockCarpet || block is BlockSnow || block is BlockSkull) && !material.isLiquid && block !is BlockContainer
    }

    private fun rayTrace(origin: Vec3d, position: Vec3d): Boolean {
        val difference = position.subtract(origin)
        val steps = 10
        val x = difference.x / steps.toDouble()
        val y = difference.y / steps.toDouble()
        val z = difference.z / steps.toDouble()
        var point = origin
        for (i in 0 until steps) {
            val blockPosition = BlockPos(point.add(x, y, z).also { point = it })
            val blockState = mc.world.getBlockState(blockPosition)
            if (blockState.block is BlockLiquid || blockState.block is BlockAir) continue
            if (!blockPosition.boundingBox.offset(blockPosition.x.toDouble(), blockPosition.y.toDouble(), blockPosition.z.toDouble()).contains(point)) continue
            return true
        }
        return false
    }

    private fun find(offset3: Vec3d): BlockData? {
        val x = mc.player.posX
        val y = mc.player.posY
        val z = mc.player.posZ
        val invert = arrayOf(EnumFacing.UP, EnumFacing.DOWN, EnumFacing.SOUTH, EnumFacing.NORTH, EnumFacing.EAST, EnumFacing.WEST)
        val position = BlockPos(Vec3d(x, y, z).add(offset3)).offset(EnumFacing.DOWN)
        for (facing in EnumFacing.values()) {
            val offset = position.offset(facing)
            if (mc.world.getBlockState(offset).block is BlockAir || rayTrace(mc.player.getLook(0.0f), getPositionByFace(offset, invert[facing.ordinal]))) continue
            return BlockData(offset, invert[facing.ordinal])
        }
        val offsets = arrayOf(BlockPos(-1, 0, 0), BlockPos(1, 0, 0), BlockPos(0, 0, -1), BlockPos(0, 0, 1), BlockPos(0, 0, 2), BlockPos(0, 0, -2), BlockPos(2, 0, 0), BlockPos(-2, 0, 0))
        for (offset in offsets) {
            val offsetPos = position.add(offset.x, 0, offset.z)
            if (!mc.world.isAirBlock(offsetPos)) continue
            for (facing in EnumFacing.values()) {
                val offset2 = offsetPos.offset(facing)
                if (mc.world.isAirBlock(offset2) || rayTrace(mc.player.getLook(0.01f), getPositionByFace(offset, invert[facing.ordinal])) || !isPlaceable(offset2)) continue
                return BlockData(offset2, invert[facing.ordinal])
            }
        }
        return null
    }

    private fun getPositionByFace(position: BlockPos, facing: EnumFacing): Vec3d {
        val offset = Vec3d(facing.directionVec.x.toDouble() / 2.0, facing.directionVec.y.toDouble() / 2.0, facing.directionVec.z.toDouble() / 2.0)
        val point = Vec3d(position.x.toDouble() + 0.5, position.y.toDouble() + 0.5, position.z.toDouble() + 0.5)
        return point.add(offset)
    }

    override fun getHudInfo(): String {
        return mode.name
    }

    class BlockData(var position: BlockPos, var face: EnumFacing)

    enum class Mode {
        NCP, Hypixel
    }
}