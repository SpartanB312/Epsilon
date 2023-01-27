package club.eridani.epsilon.client.management

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.event.events.WorldEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.management.SpartanCore.addScheduledTask
import club.eridani.epsilon.client.util.TickTimer
import club.eridani.epsilon.client.util.combat.HoleInfo
import club.eridani.epsilon.client.util.combat.HoleUtils.checkHole
import club.eridani.epsilon.client.util.extension.betterPosition
import club.eridani.epsilon.client.util.extension.flooredPosition
import club.eridani.epsilon.client.util.math.vector.distanceSqTo
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.SpartanJob
import club.eridani.epsilon.client.util.threads.runSafe
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import it.unimi.dsi.fastutil.longs.LongSet
import it.unimi.dsi.fastutil.longs.LongSets
import net.minecraft.entity.Entity
import net.minecraft.util.math.BlockPos
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Predicate

object HoleManager : Helper {
    private val holeMap0 = ConcurrentHashMap<BlockPos, HoleInfo>()
    val holeMap: Map<BlockPos, HoleInfo> get() = holeMap0
    private var holeSet: LongSet = LongSets.EMPTY_SET
    var holeInfos = emptyList<HoleInfo>(); private set

    private val mainTimer = TickTimer()
    private val updateTimer = TickTimer()
    private val removeTimer = TickTimer()

    private const val RANGE = 16
    private const val RANGE_SQ = 256
    private const val MAX_RANGE_SQ = 1024

    private var lastJob: SpartanJob? = null
    private var dirty = false

    init {
        onRender3D {
            runSafe {
                if (mainTimer.tickAndReset(100) && (lastJob == null || (lastJob != null && lastJob!!.isFinished))) {
                    lastJob = addScheduledTask {
                        if (removeTimer.tickAndReset(200L)) {
                            removeInvalidPos()
                        }

                        updatePos(updateTimer.tickAndReset(250L))

                        updateHoleInfoList()
                    }
                }
            }
        }

        listener<WorldEvent.BlockUpdate> {
            addScheduledTask {
                runSafe {
                    val playerPos = player.flooredPosition
                    val mutablePos = BlockPos.MutableBlockPos()

                    val sequence = sequence {
                        for (x in it.pos.x + 2 downTo it.pos.x - 1) {
                            for (y in it.pos.y + 1 downTo it.pos.y - 2) {
                                for (z in it.pos.z + 2 downTo it.pos.z - 1) {
                                    if (playerPos.distanceSqTo(x, y, z) > RANGE_SQ) continue
                                    yield(mutablePos.setPos(x, y, z))
                                }
                            }
                        }
                    }

                    updatePosSequence(sequence)
                }
            }

        }

        listener<WorldEvent.RenderUpdate> {
            addScheduledTask {
                runSafe {
                    val playerPos = player.flooredPosition
                    val mutablePos = BlockPos.MutableBlockPos()

                    val sequence = sequence {
                        for (x in it.x2 + 2 downTo it.x1 - 2) {
                            for (y in it.y2 + 1 downTo it.y1 - 2) {
                                for (z in it.z2 + 2 downTo it.z1 - 2) {
                                    if (playerPos.distanceSqTo(x, y, z) > RANGE_SQ) continue
                                    yield(mutablePos.setPos(x, y, z))
                                }
                            }
                        }
                    }

                    updatePosSequence(sequence)
                }
            }
        }
    }

    fun getHoleBelow(pos: BlockPos, yRange: Int) = getHoleBelow(pos, yRange) { true }

    private fun getHoleBelow(pos: BlockPos, yRange: Int, predicate: Predicate<HoleInfo>): HoleInfo? {
        for (yOffset in 0..yRange) {
            val offsetPos = pos.down(yOffset)
            val info = getHoleInfo(offsetPos)
            if (info.isHole && predicate.test(info)) return info
        }

        return null
    }

    fun getHoleInfo(entity: Entity): HoleInfo {
        return getHoleInfo(entity.betterPosition)
    }

    fun getHoleInfo(pos: BlockPos) = holeMap0.computeIfAbsent(pos) {
        runSafe {
            val holeInfo = checkHole(it)

            holeInfo
        } ?: HoleInfo.empty(it)
    }

    private fun SafeClientEvent.removeInvalidPos() {
        val playerPos = player.flooredPosition
        var modified = false

        val iterator = holeMap0.keys.iterator()
        while (iterator.hasNext()) {
            val pos = iterator.next()
            if (playerPos.distanceSqTo(pos) > MAX_RANGE_SQ) {
                iterator.remove()
                modified = true
            }
        }

        if (modified) {
            synchronized(HoleManager) {
                dirty = true
            }
        }
    }

    private fun SafeClientEvent.updatePos(force: Boolean) {
        val playerPos = player.flooredPosition
        val checked = LongOpenHashSet()
        val mutablePos = BlockPos.MutableBlockPos()

        var modified = false

        for (x in RANGE downTo -RANGE) {
            for (y in RANGE downTo -RANGE) {
                for (z in RANGE downTo -RANGE) {
                    mutablePos.setPos(playerPos.x + x, playerPos.y + y, playerPos.z + z)
                    if (mutablePos.y !in 0..255) continue
                    if (!force && holeSet.contains(mutablePos.toLong())) continue
                    modified = updatePos(playerPos, checked, mutablePos) || modified
                }
            }
        }

        if (modified) {
            synchronized(HoleManager) {
                dirty = true
            }
        }
    }

    private fun SafeClientEvent.updatePosSequence(sequence: Sequence<BlockPos.MutableBlockPos>) {
        val playerPos = player.flooredPosition
        val checked = LongOpenHashSet()

        var modified = false

        sequence.forEach {
            modified = updatePos(playerPos, checked, it) || modified
        }

        if (modified) {
            synchronized(HoleManager) {
                dirty = true
            }
        }
    }

    private fun SafeClientEvent.updatePos(playerPos: BlockPos, checked: LongSet, pos: BlockPos.MutableBlockPos): Boolean {
        val long = pos.toLong()
        if (checked.contains(long)) return false
        if (pos.distanceSq(playerPos) > RANGE_SQ) return false

        val holeInfo = checkHole(pos)
        return if (!holeInfo.isHole) {
            val prev = holeMap0.put(holeInfo.origin, holeInfo)
            checked.add(long)
            prev == null || prev.isHole
        } else {
            var modified = false

            for (holePos in holeInfo.holePos) {
                val prev = holeMap0.put(holePos, holeInfo)
                checked.add(holePos.toLong())

                modified = modified || (prev == null || !prev.isHole)
            }

            modified
        }
    }

    private fun updateHoleInfoList() {
        holeSet = LongOpenHashSet(holeMap0.size).apply {
            holeMap0.keys.forEach {
                add(it.toLong())
            }
        }

        holeInfos = holeMap0.values.asSequence()
            .filter { it.isHole }
            .distinct()
            .toList()
    }
}