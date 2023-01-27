package club.eridani.epsilon.client.module.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.id
import club.eridani.epsilon.client.common.extensions.isReplaceable
import club.eridani.epsilon.client.common.extensions.packetAction
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.event.safeParallelListener
import club.eridani.epsilon.client.management.CombatManager
import club.eridani.epsilon.client.management.HoleManager
import club.eridani.epsilon.client.management.HotbarManager.spoofHotbar
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.player.PacketMine
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.combat.CalcContext
import club.eridani.epsilon.client.util.combat.CrystalUtils
import club.eridani.epsilon.client.util.combat.CrystalUtils.canPlaceCrystal
import club.eridani.epsilon.client.util.extension.betterPosition
import club.eridani.epsilon.client.util.extension.eyePosition
import club.eridani.epsilon.client.util.inventory.slot.firstBlock
import club.eridani.epsilon.client.util.inventory.slot.firstItem
import club.eridani.epsilon.client.util.inventory.slot.hotbarSlots
import club.eridani.epsilon.client.util.math.sq
import club.eridani.epsilon.client.util.spoofSneak
import club.eridani.epsilon.client.util.world.FastRayTraceAction
import club.eridani.epsilon.client.util.world.fastRaytrace
import club.eridani.epsilon.client.event.events.*
import club.eridani.epsilon.client.util.world.getBlock
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.network.play.client.CPacketAnimation
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock
import net.minecraft.network.play.client.CPacketUseEntity
import net.minecraft.network.play.server.SPacketBlockChange
import net.minecraft.util.EnumFacing
import net.minecraft.util.EnumHand
import net.minecraft.util.math.BlockPos

@CombatManager.CombatModule
internal object AnvilCity : Module(
    name = "AnvilCity",
    category = Category.Combat,
    description = "City enemy with anvil",
    priority = 1500
) {

    private val placeDelay by setting("Place Delay", 100, 0..1000, 5)
    private val breakDelay by setting("Break Delay", 100, 0..1000, 5)
    private val minDamage by setting("Min Damage", 4.0f, 1.0f..10.0f, 0.1f)
    private val minUpdateDamage by setting("Min Update Damage", 8.0f, 1.0f..10.0f, 0.1f)
    private val updateDelay by setting("Update Delay", 200, 0..1000, 5)
    private val packetBreakDelay by setting("Packet Break Delay", 100, 0..1000, 5)
    private val range by setting("Range", 4.5f, 1.0f..6.0f, 0.1f)

    private val placeTimer = TickTimer()
    private val breakTimer = TickTimer()
    private val updateTimer = TickTimer()
    private val packetBreakTimer = TickTimer()

    private var anvilPos: BlockPos? = null
    private var crystalPos: BlockPos? = null

    private var anvilPlaced = false
    private var crystalID = -1

    override fun isActive(): Boolean {
        return isEnabled && anvilPos != null
    }

    override fun onDisable() {
        placeTimer.reset(-114514L)
        breakTimer.reset(-114514L)

        anvilPos = null
        anvilPlaced = false
        crystalID = -1
    }

    init {
        safeParallelListener<TickEvent> {
            updateTargetPos()

            val anvilPos = anvilPos
            val crystalPos = crystalPos

            if (anvilPos == null || crystalPos == null) {
                PacketMine.reset(AnvilCity)
                return@safeParallelListener
            }

            val posUp = anvilPos.up()
            val block = world.getBlock(posUp)

            anvilPlaced = block != Blocks.AIR

            val crystalPosUp = crystalPos.up()
            crystalID = CombatManager.crystalList
                .find {
                    !it.first.isDead
                            && CrystalUtils.placeBoxIntersectsCrystalBox(it.second.crystalPos, crystalPosUp)
                }?.first?.entityId ?: -1
            println(crystalID)

            if (block != Blocks.AIR) {
                PacketMine.mineBlock(AnvilCity, posUp)
            }
        }

        safeListener<CrystalSpawnEvent> {
            val anvilPos = anvilPos ?: return@safeListener
            val crystalPos = crystalPos ?: return@safeListener

            if (CrystalUtils.placeBoxIntersectsCrystalBox(crystalPos, it.crystalDamage.blockPos)) {
                crystalID = it.entityID
                if (!anvilPlaced && breakTimer.tick(0)) {
                    breakCrystal(it.entityID)
                    placeAnvil(anvilPos)
                }
            }
        }

        safeListener<PacketEvent.Receive> {
            val entityID = crystalID

            val anvilPos = anvilPos ?: return@safeListener
            val posUp = anvilPos.up()

            if (it.packet is SPacketBlockChange
                && it.packet.blockPosition == posUp
            ) {
                if (it.packet.blockState.block == Blocks.AIR) {
                    anvilPlaced = false

                    if (entityID != -1
                        && packetBreakTimer.tick(packetBreakDelay)
                        && breakTimer.tick(0)
                    ) {
                        breakCrystal(entityID)
                        placeAnvil(anvilPos)
                        packetBreakTimer.reset()
                    }
                } else {
                    PacketMine.mineBlock(AnvilCity, posUp)
                }
            }
        }

        safeListener<CrystalSetDeadEvent> {
            val anvilPos = anvilPos ?: return@safeListener
            val crystalPos = crystalPos ?: return@safeListener

            crystalID = -1

            if (placeTimer.tick(0)) {
                placeAnvil(anvilPos)
                placeCrystal(crystalPos)
            }
        }

        safeListener<RunGameLoopEvent.Tick> {
            val anvilPos = anvilPos ?: return@safeListener
            val crystalPos = crystalPos ?: return@safeListener
            val entityID = crystalID

            if (!anvilPlaced && entityID != -1 && breakTimer.tick(breakDelay)) {
                breakCrystal(entityID)
                placeAnvil(anvilPos)
                placeTimer.reset()
            }

            if (placeTimer.tick(placeDelay)) {
                if (!anvilPlaced) {
                    placeAnvil(anvilPos)
                } else {
                    val anvilPosUp = anvilPos.up()
                    if (world.getBlock(anvilPosUp) != Blocks.ANVIL
                        && world.getBlockState(anvilPosUp.up()).isReplaceable
                    ) {
                        placeAnvil(anvilPosUp)
                    }
                }
                if (entityID == -1) {
                    placeCrystal(crystalPos)
                }
            }
        }
    }

    private fun SafeClientEvent.updateTargetPos() {
        val flag = anvilPos != null
        val rangeSq = range.sq

        val result = CombatManager.target
            ?.takeIf {
                flag || HoleManager.getHoleInfo(it).isHole
            }
            ?.let { target ->
                val targetPos = target.betterPosition
                val playerPos = player.betterPosition
                val eyePos = player.eyePosition
                val mutableBlockPos = BlockPos.MutableBlockPos()

                val anvilPos = anvilPos
                val crystalPos = crystalPos

                CombatManager.contextTarget?.let { context ->
                    if (anvilPos != null && crystalPos != null) {
                        val damage = calcDamage(context, anvilPos.up(), crystalPos, mutableBlockPos)
                        if (damage > minUpdateDamage && !updateTimer.tick(updateDelay)) return
                    }

                    val sequence = EnumFacing.HORIZONTALS.asSequence()
                        .flatMap { mainSide ->
                            val opposite = mainSide.opposite
                            val pos1 = targetPos.offset(mainSide)
                            EnumFacing.HORIZONTALS.asSequence()
                                .filter {
                                    it != opposite
                                }.map {
                                    pos1 to pos1.offset(it).down()
                                }
                        }.filter {
                            playerPos.distanceSq(it.first) <= rangeSq
                        }
                        .filter {
                            val dist = playerPos.distanceSq(it.second)
                            dist <= rangeSq
                                    && (dist <= 9
                                    || !world.fastRaytrace(
                                eyePos,
                                it.second.x + 0.5,
                                it.second.y + 2.7,
                                it.second.z + 0.5,
                                20,
                                mutableBlockPos
                            ) { rayTracePos, blockState ->
                                if (rayTracePos != it.first && blockState.block != Blocks.AIR && CrystalUtils.isResistant(
                                        blockState
                                    )
                                ) {
                                    FastRayTraceAction.CALC
                                } else {
                                    FastRayTraceAction.SKIP
                                }
                            })
                        }
                        .filter { (anvilPos, crystalPos) ->
                            world.getBlock(anvilPos) != Blocks.BEDROCK
                                    && canPlaceCrystal(crystalPos)
                        }

                    var maxDamage = minDamage
                    var result: Pair<BlockPos, BlockPos>? = null

                    for (pair in sequence) {
                        val damage = calcDamage(context, pair.first, pair.second, mutableBlockPos)
                        if (damage > maxDamage) {
                            maxDamage = damage
                            result = pair
                        }
                    }

                    result
                }
            }

        if (result != null) {
            anvilPos = result.first.down()
            crystalPos = result.second
        } else {
            anvilPos = null
            crystalPos = null
        }
    }

    private fun calcDamage(
        context: CalcContext,
        anvilPos: BlockPos,
        pos: BlockPos,
        mutableBlockPos: BlockPos.MutableBlockPos
    ): Float {
        return context.calcDamage(
            pos.x + 0.5,
            pos.y + 1.0,
            pos.z + 0.5,
            false,
            6.0f,
            mutableBlockPos
        ) { rayTracePos, blockState ->
            if (rayTracePos != anvilPos && blockState.block != Blocks.AIR && CrystalUtils.isResistant(blockState)) {
                FastRayTraceAction.CALC
            } else {
                FastRayTraceAction.SKIP
            }
        }
    }

    private fun SafeClientEvent.placeCrystal(targetPos: BlockPos) {
        player.hotbarSlots.firstItem(Items.END_CRYSTAL)?.let {
            spoofHotbar(it) {
                connection.sendPacket(
                    CPacketPlayerTryUseItemOnBlock(
                        targetPos,
                        EnumFacing.UP,
                        EnumHand.MAIN_HAND,
                        0.5f,
                        1.0f,
                        0.5f
                    )
                )
            }
            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
            placeTimer.reset()
        }
    }

    private fun SafeClientEvent.placeAnvil(targetPos: BlockPos) {
        player.hotbarSlots.firstBlock(Blocks.ANVIL)?.let {
            player.spoofSneak {
                spoofHotbar(it) {
                    connection.sendPacket(
                        CPacketPlayerTryUseItemOnBlock(
                            targetPos,
                            EnumFacing.UP,
                            EnumHand.MAIN_HAND,
                            0.5f,
                            1.0f,
                            0.5f
                        )
                    )
                }
            }
            connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
            placeTimer.reset()
        }
    }

    private fun SafeClientEvent.breakCrystal(entityID: Int) {
        connection.sendPacket(
            CPacketUseEntity().apply {
                packetAction = CPacketUseEntity.Action.ATTACK
                id = entityID
            }
        )
        connection.sendPacket(CPacketAnimation(EnumHand.MAIN_HAND))
        breakTimer.reset()
    }
}