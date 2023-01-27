package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.combat.CrystalUtils
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.extension.flooredPosition
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.text.ChatUtil
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.item.EntityEnderCrystal
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerDigging
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import java.awt.Color


object AutoCev : Module(
    name = "AutoCev",
    category = Category.Combat,
    description = "Auto put crystal or obsidian and break it"
) {
    private val mode by setting("Mode", Mode.Head)
    private val speed by setting("SpeedMode", true)
    private val range by setting("Range", 4f, 0f..10f, 0.1f)
    private val switchDelay by setting("SwitchDelay", 5, 1..20, 1)
    private val renderBlock by setting("Render", true)
    private val autoTrap by setting("AutoTrap", false)

    private var lastPos: BlockPos? = null
    private var isPaused = false
    private var shouldSwitch = false
    private var switchTimer = Timer()
    private var explodeTimer = Timer()
    private var isTrapEnabled = false
    private var lastCage: AutoTrap.Cage? = null
    private var crystalBreak = false
    private var initial = false

    init {
        onRender3D {
            if (lastPos != null && renderBlock) RenderUtils3D.drawFullBox(lastPos!!, 1f, if (mc.world.isAirBlock(lastPos)) Color(255, 0, 0, 70).rgb else Color(0, 255, 0, 70).rgb)
        }

        onTick {
            runSafe {
                var target: EntityLivingBase? = null
                for (player in world.loadedEntityList) {
                    if (player is EntityLivingBase) {
                        if (player == mc.player) continue
                        if (player.getDistance(mc.player) > 6) continue
                        target = player
                    }
                }

                if (target == null) return@runSafe

                val pos = target.flooredPosition.up()
                val haveBlockSupport: Boolean

                val crystalPos = if (mode == Mode.Slant) {
                    val crystalPos = lastPos ?: findPlaceablePos(pos)
                    haveBlockSupport = !world.isAirBlock(crystalPos.down()) || crystalPos == pos
                    crystalPos
                } else {
                    val crystalPos = pos.up()
                    haveBlockSupport = EnumFacing.HORIZONTALS.any {
                        !world.isAirBlock(crystalPos.offset(it))
                    }
                    crystalPos
                }

                lastPos = crystalPos


                if (!haveBlockSupport) {
                    if (autoTrap) {
                        AutoTrap.enable()
                        lastCage = AutoTrap.cage
                        AutoTrap.cage = AutoTrap.Cage.Trap
                        isTrapEnabled = true
                    } else {
                        disable()
                        ChatUtil.printErrorChatMessage("No block supported")
                    }
                } else if (isTrapEnabled) {
                    AutoTrap.disable()
                    if (lastCage != null) {
                        AutoTrap.cage = lastCage!!
                    }
                    isTrapEnabled = false
                }

                val facing = getLegitPlaceSide(crystalPos) ?: return@runSafe

                val crystal = ItemUtil.findItemInHotBar(Items.END_CRYSTAL)
                val pickaxe = ItemUtil.getPickaxeInHotBar()
                val obby = ItemUtil.findBlockInHotBar(Blocks.OBSIDIAN)

                if (obby == -1 || pickaxe == -1) {
                    disable()
                    ChatUtil.printErrorChatMessage("Cant not find needed item in your hotbar")
                    return@runSafe
                }

                if (crystal == -1 && mc.player.heldItemOffhand.item !== Items.END_CRYSTAL) {
                    disable()
                    ChatUtil.printErrorChatMessage("Cant not find End Crystal")
                    return@runSafe
                }

                if (initial) {
                    mc.player.swingArm(EnumHand.MAIN_HAND)
                    mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, crystalPos, facing))
                    mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, crystalPos, facing))
                }

                mc.player.connection.sendPacket(CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, crystalPos, facing))

                when {
                    CrystalUtils.isPlaceable(mc.world, crystalPos, false) && mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(crystalPos.up())).isEmpty() -> {
                        if (mc.player.heldItemOffhand.item == Items.END_CRYSTAL) {
                            mc.player.connection.sendPacket(CPacketAnimation(EnumHand.OFF_HAND))
                            mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(crystalPos, EnumFacing.UP, EnumHand.OFF_HAND, 0f, 0f, 0f))
                        } else if (crystal != -1) {
                            ItemUtil.switchToSlot(crystal)
                            mc.player.connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
                            mc.player.connection.sendPacket(CPacketPlayerTryUseItemOnBlock(crystalPos, EnumFacing.UP, EnumHand.MAIN_HAND, 0f, 0f, 0f))
                        }
                        resetSwitch()
                    }

                    world.isAirBlock(crystalPos) -> {
                        world.loadedEntityList.asSequence().filterIsInstance<EntityEnderCrystal>().minByOrNull {
                            player.getDistanceSq(it)
                        }?.takeIf {
                            player.getDistanceSq(it) <= 36.0
                        }?.let {
                            explodeCrystal(it)
                        }

                        if (obby != -1 && mc.world.getEntitiesWithinAABB(Entity::class.java, AxisAlignedBB(if (speed) crystalPos.up() else crystalPos)).isEmpty()) {
                            ItemUtil.switchToSlot(obby)
                            club.eridani.epsilon.client.util.EntityUtil.placeBlockScaffold(crystalPos)
//                            BlockInteractionHelper.place(crystalPos, range.value, true, true, true)
                        }
                    }

                    !isPaused -> {
                        if (shouldSwitch) {
                            switchToPickaxe()
                            initial = false
                        }
                    }
                }
            }
        }

    }

    override fun onEnable() {
        initial = true
    }

    override fun onDisable() {
        lastPos = null
        if (isTrapEnabled) {
            AutoTrap.disable()
            if (lastCage != null) {
                AutoTrap.cage = lastCage!!
            }
            isTrapEnabled = false
        }
    }


    private fun findPlaceablePos(pos: BlockPos): BlockPos {
        EnumFacing.HORIZONTALS.forEach {
            val faceDirection = pos.offset(it)
            if (mc.world.getBlockState(faceDirection).block == Blocks.BEDROCK) return@forEach
            if (mc.player.getDistance(faceDirection.x.toDouble(), faceDirection.y.toDouble(), faceDirection.z.toDouble()) > range) return@forEach
            if (CrystalUtils.isPlaceable(mc.world, faceDirection, false)) {
                return faceDirection
            }
        }
        return pos
    }

    private fun switchToPickaxe() {
        val slot = ItemUtil.findItemInHotBar(Items.DIAMOND_PICKAXE)
        ItemUtil.switchToSlot(slot)
        if (switchTimer.passed(switchDelay * 50)) {
            switchTimer.reset()
            shouldSwitch = false
        }
    }

    private fun explodeCrystal(crystal: EntityEnderCrystal) {
        if (explodeTimer.passed(50)) {
            mc.playerController.attackEntity(mc.player, crystal)
            mc.player.swingArm(EnumHand.OFF_HAND)
            mc.player.resetCooldown()
            explodeTimer.reset()
            crystalBreak = true
        }
    }

    private fun getLegitPlaceSide(pos: BlockPos): EnumFacing? {
        val centerPos = Vec3d(pos.add(.5, .5, .5))
        return EnumFacing.values().filter {
            (!mc.world.getBlockState(pos.offset(it)).block.isCollidable || mc.world.getBlockState(pos.offset(it)).material.isReplaceable)
        }.minWithOrNull(Comparator.comparing {
            (mc.player.getPositionEyes(1f).distanceTo(centerPos.add(Vec3d(it.directionVec).scale(.5))))
        })
    }

    private fun resetSwitch() {
        isPaused = false
        shouldSwitch = true
        switchTimer.reset()
    }


    enum class Mode {
        Slant, Head
    }
}