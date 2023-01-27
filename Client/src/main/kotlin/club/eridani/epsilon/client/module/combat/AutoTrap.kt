package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.rightClickDelay
import club.eridani.epsilon.client.common.extensions.runSafeTask
import club.eridani.epsilon.client.management.FriendManager
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.PlayerPacketManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ItemUtil.findBlockInHotBar
import club.eridani.epsilon.client.util.extension.flooredPosition
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.math.Vec2f
import club.eridani.epsilon.client.util.math.vector.toBlockPos
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockLiquid
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Blocks
import net.minecraft.network.play.client.CPacketEntityAction
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameType
import java.util.*


internal object AutoTrap :
    Module(name = "AutoTrap", category = Category.Combat, description = "Place obsidian to trap your enemies") {

    var cage by setting("Cage", Cage.Trap)
    val range by setting("Range", 4.3, 0.0..10.0, .1)
    private val instant by setting("Instant", false)
    private val blocksPerTick by setting("BlocksPerTick", 2, 1..23, 1)
    private val tickDelay by setting("TickDelay", 2, 0..10, 1)
    private val rotate by setting("Rotate", true)
    private val camper by setting("HoleCheck", false)
    private val preview by setting("Preview", false)
    private val toggleable by setting("Toggleable", false)
    private val noGlitchBlocks by setting("NoGlitchBlocks", true)

    private var currentPos: BlockPos? = null
    private var closestTarget: EntityPlayer? = null
    private var lastTickTargetName: String? = null
    private var playerHotbarSlot = -1
    private var lastHotbarSlot = -1
    private var delayStep = 0
    private var isSneaking = false
    private var offsetStep = 0
    private var firstRun = false
    private var currentCage = mutableListOf<Vec3d>()

    private val TRAP: Array<Vec3d> = arrayOf(
        Vec3d(0.0, 0.0, -1.0),
        Vec3d(1.0, 0.0, 0.0),
        Vec3d(0.0, 0.0, 1.0),
        Vec3d(-1.0, 0.0, 0.0),
        Vec3d(0.0, 1.0, -1.0),
        Vec3d(1.0, 1.0, 0.0),
        Vec3d(0.0, 1.0, 1.0),
        Vec3d(-1.0, 1.0, 0.0),
        Vec3d(0.0, 2.0, -1.0),
        Vec3d(1.0, 2.0, 0.0),
        Vec3d(0.0, 2.0, 1.0),
        Vec3d(-1.0, 2.0, 0.0),
        Vec3d(0.0, 3.0, -1.0),
        Vec3d(0.0, 3.0, 0.0))
    private val TRAPFULLROOF: Array<Vec3d> = arrayOf(Vec3d(0.0, 0.0, -1.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(0.0, 1.0, -1.0), Vec3d(1.0, 1.0, 0.0), Vec3d(0.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 0.0), Vec3d(0.0, 2.0, -1.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(-1.0, 2.0, 0.0), Vec3d(0.0, 3.0, -1.0), Vec3d(1.0, 3.0, 0.0), Vec3d(0.0, 3.0, 1.0), Vec3d(-1.0, 3.0, 0.0), Vec3d(0.0, 3.0, 0.0))
    private val TRAPFEET: Array<Vec3d> = arrayOf(Vec3d(0.0, 0.0, -1.0), Vec3d(0.0, 1.0, -1.0), Vec3d(0.0, 2.0, -1.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(-1.0, 2.0, 0.0), Vec3d(-1.0, 2.0, -1.0), Vec3d(1.0, 2.0, 1.0), Vec3d(1.0, 2.0, -1.0), Vec3d(-1.0, 2.0, 1.0), Vec3d(0.0, 3.0, -1.0), Vec3d(0.0, 3.0, 0.0))
    private val CRYSTAL: Array<Vec3d> = arrayOf(Vec3d(0.0, 0.0, -1.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(-1.0, 0.0, 1.0), Vec3d(1.0, 0.0, -1.0), Vec3d(-1.0, 0.0, -1.0), Vec3d(1.0, 0.0, 1.0), Vec3d(-1.0, 1.0, -1.0), Vec3d(1.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 1.0), Vec3d(1.0, 1.0, -1.0), Vec3d(0.0, 2.0, -1.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(-1.0, 2.0, 0.0), Vec3d(-1.0, 2.0, 1.0), Vec3d(1.0, 2.0, -1.0), Vec3d(0.0, 3.0, -1.0), Vec3d(0.0, 3.0, 0.0))
    private val CRYSTALFULLROOF: Array<Vec3d> = arrayOf(Vec3d(0.0, 0.0, -1.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(-1.0, 0.0, 1.0), Vec3d(1.0, 0.0, -1.0), Vec3d(-1.0, 0.0, -1.0), Vec3d(1.0, 0.0, 1.0), Vec3d(-1.0, 1.0, -1.0), Vec3d(1.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 1.0), Vec3d(1.0, 1.0, -1.0), Vec3d(0.0, 2.0, -1.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(-1.0, 2.0, 0.0), Vec3d(-1.0, 2.0, 1.0), Vec3d(1.0, 2.0, -1.0), Vec3d(0.0, 3.0, -1.0), Vec3d(1.0, 3.0, 0.0), Vec3d(0.0, 3.0, 1.0), Vec3d(-1.0, 3.0, 0.0), Vec3d(0.0, 3.0, 0.0))
    private val HEADBLOCK: Array<Vec3d> = arrayOf(Vec3d(0.0, 0.0, -1.0), Vec3d(1.0, 0.0, 0.0), Vec3d(0.0, 0.0, 1.0), Vec3d(-1.0, 0.0, 0.0), Vec3d(0.0, 1.0, -1.0), Vec3d(1.0, 1.0, 0.0), Vec3d(0.0, 1.0, 1.0), Vec3d(-1.0, 1.0, 0.0), Vec3d(0.0, 2.0, -1.0), Vec3d(1.0, 2.0, 0.0), Vec3d(0.0, 2.0, 1.0), Vec3d(-1.0, 2.0, 0.0), Vec3d(0.0, 3.0, -1.0), Vec3d(0.0, 3.0, 0.0), Vec3d(0.0, 4.0, 0.0))


    init {
        onTick {
            runSafe {
                if (!firstRun) {
                    delayStep = if (delayStep < tickDelay) {
                        delayStep++
                        return@runSafe
                    } else {
                        0
                    }
                }

                findClosestTarget()

                if (closestTarget == null) {
                    if (firstRun) {
                        firstRun = false
                    }
                    return@runSafe
                }

                if (firstRun) {
                    firstRun = false
                    lastTickTargetName = closestTarget!!.name
                } else if (lastTickTargetName != null && lastTickTargetName != closestTarget!!.name) {
                    lastTickTargetName = closestTarget!!.name
                    offsetStep = 0
                }

                currentPos = null

                val placeTargets = arrayListOf<Vec3d>()

                when (cage) {
                    Cage.Trap -> placeTargets.addAll(TRAP)
                    Cage.HeadBlock -> placeTargets.addAll(HEADBLOCK)
                    Cage.TrapFull -> placeTargets.addAll(TRAPFULLROOF)
                    Cage.TrapFeet -> placeTargets.addAll(TRAPFEET)
                    Cage.Crystal -> placeTargets.addAll(CRYSTAL)
                    Cage.CrystalFull -> placeTargets.addAll(CRYSTALFULLROOF)
                }

                currentCage = placeTargets

                if (closestTarget != null) {
                    if (isTrapped()) {
//                    if (announceUsage.getValue()) {
//                        ChatUtil.sendNoSpamMessage("[AutoTrap] " + "Trapped target: " + ChatUtil.SECTION_SIGN + "d" + closestTarget!!.name, 312348)
//                    }
                        if (toggleable) {
                            toggle()
                        }
                        return@runSafe
                    }
                }

                // TODO: dont use static bridging in offset but calculate them on the fly
                //  based on view direction (or relative direction of target to player)
                //  (add full/half n/e/s/w patterns to append dynamically)

                // TODO: sort offsetList by optimal caging success factor ->
                // sort them by pos y up AND start building behind target
                var blocksPlaced = 0

                while (blocksPlaced < blocksPerTick) {
                    if (offsetStep >= placeTargets.size) {
                        offsetStep = 0
                        break
                    }
                    if (instant) {
                        for (vecPos in placeTargets) {
                            val pos: BlockPos = BlockPos(closestTarget!!.positionVector).down().add(vecPos.x, vecPos.y, vecPos.z)
                            if (placeBlockInRange(pos, range)) {
                                blocksPlaced++
                                currentPos = pos
                            }
                        }
                    } else {
                        val offsetPos = BlockPos(placeTargets[offsetStep])
                        val targetPos: BlockPos = BlockPos(closestTarget!!.positionVector).down().add(offsetPos.x, offsetPos.y, offsetPos.z)
                        if (placeBlockInRange(targetPos, range)) {
                            blocksPlaced++
                            currentPos = targetPos
                        }
                    }
                    offsetStep++
                }

                if (blocksPlaced > 0) {
//                if (announceUsage.getValue()) {
//                ChatUtil.sendNoSpamMessage("[AutoTrap] " + "Trapping target: " + ChatUtil.SECTION_SIGN + "c" + closestTarget!!.name, 312348)
//                }
                    if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
                        mc.player.inventory.currentItem = playerHotbarSlot
                        lastHotbarSlot = playerHotbarSlot
                    }
                    if (isSneaking) {
                        mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
                        isSneaking = false
                    }
                }
            }
        }

        onRender3D {
            runSafeTask {
                if (preview) {
                    if (closestTarget != null) {
                        val unPlacedPos: MutableList<Vec3d> = ArrayList()
                        for (vecPos in currentCage) {
                            val pos = closestTarget!!.positionVector.add(0.0, -1.0, 0.0).add(vecPos.x, vecPos.y, vecPos.z)
                            if (mc.world.getBlockState(BlockPos(pos)).material.isSolid) {
                                continue
                            }
                            unPlacedPos.add(pos)
                        }
                        for (vecPos in unPlacedPos) {
                            val pos = BlockPos(vecPos)
                            RenderUtils3D.drawBoundingFilledBox(pos, GUIManager.firstColor.alpha(70).toArgb())
                        }
                    }
                } else {
                    if (currentPos != null) {
                        RenderUtils3D.drawBoundingFilledBox(currentPos!!, GUIManager.firstColor.alpha(70).toArgb())
                    }
                }
            }
        }
    }

    private fun findClosestTarget() {
        val playerList = mc.world.playerEntities
        closestTarget = null
        for (target in playerList) {
            if (target == mc.player) {
                continue
            }
            if (FriendManager.isFriend(target)) {
                continue
            }
            if (target.isDead) {
                continue
            }
            if (target.health <= 0) {
                continue
            }
            if (camper) {
                if (!Utils.isInHole(target.flooredPosition, false)) continue
            }
            if (closestTarget == null) {
                closestTarget = target
                continue
            }
            if (mc.player.getDistance(target) < mc.player.getDistance(closestTarget)) {
                closestTarget = target
            }
        }
    }

    private fun placeBlockInRange(pos: BlockPos, range: Double): Boolean {
        // check if block is already placed
        val block = mc.world.getBlockState(pos).block
        if (block !is BlockAir && block !is BlockLiquid) {
            return false
        }

        // check if target blocks placing
        for (entity in mc.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB(pos))) {
            if (entity !is EntityItem && entity !is EntityXPOrb) {
                return false
            }
        }
        val side: EnumFacing = Utils.getPlaceableSide(pos) ?: return false

        // check if we have a block adjacent to blockpos to click at
        val neighbour = pos.offset(side)
        val opposite = side.opposite

        // check if neighbor can be right clicked
        if (!Utils.canBeClicked(neighbour)) {
            return false
        }
        val hitVec = Vec3d(neighbour).add(0.5, 0.5, 0.5).add(Vec3d(opposite.directionVec).scale(0.5))
        val neighbourBlock = mc.world.getBlockState(neighbour).block
        if (mc.player.positionVector.distanceTo(hitVec) > range) {
            return false
        }
        val obiSlot = findBlockInHotBar(Blocks.OBSIDIAN)
        if (obiSlot == -1) {
            disable()
        }
        if (lastHotbarSlot != obiSlot) {
            mc.player.inventory.currentItem = obiSlot
            lastHotbarSlot = obiSlot
        }
        if (!isSneaking && ItemUtil.blackList.contains(neighbourBlock) || ItemUtil.shulkersList.contains(neighbourBlock)) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SNEAKING))
            isSneaking = true
        }
        if (rotate) {
            if (instant) RotationUtil.faceVectorPacketInstant(hitVec)
            else {
                PlayerPacketManager.sendPacket(801) {
                    val rotation = RotationUtil.getLegitRotations(hitVec)
                    rotate(Vec2f(rotation[0], rotation[1]))
                }
            }
        }
        mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND)
        mc.player.swingArm(EnumHand.MAIN_HAND)
        mc.rightClickDelay = 4
        if (noGlitchBlocks && !mc.playerController.currentGameType.equals(GameType.CREATIVE)) {
            mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, neighbour, opposite))
//            if (ModuleManager.getModule(NoBreakAnimation::class.java).isEnabled()) {
//                ModuleManager.getModule(NoBreakAnimation::class.java).resetMining()
//            }
        }
        return true
    }

    override fun onEnable() {
        if (mc.player == null) {
            this.disable()
            return
        }
        firstRun = true
        playerHotbarSlot = mc.player.inventory.currentItem
        lastHotbarSlot = -1
    }

    override fun onDisable() {
        if (mc.player == null) {
            return;
        }

        if (lastHotbarSlot != playerHotbarSlot && playerHotbarSlot != -1) {
            mc.player.inventory.currentItem = playerHotbarSlot
        }

        if (isSneaking) {
            mc.player.connection.sendPacket(CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING))
            isSneaking = false
        }

        playerHotbarSlot = -1
        lastHotbarSlot = -1

    }

    private fun isTrapped(): Boolean {
        val placedPos: MutableList<Vec3d> = ArrayList()
        if (closestTarget != null) {
            for (vecPos in currentCage) {
                val pos = closestTarget!!.positionVector.add(0.0, -1.0, 0.0).add(vecPos.x, vecPos.y, vecPos.z)
                if (!mc.world.isAirBlock(pos.toBlockPos())) {
                    continue
                }
                placedPos.add(pos)
            }
            return placedPos.size == currentCage.size
        }
        return false
    }

    override fun getHudInfo(): String? {
        return if (closestTarget == null) "Finding..." else closestTarget!!.name
    }


    enum class Cage {
        Trap, HeadBlock, TrapFull, TrapFeet, Crystal, CrystalFull
    }
}