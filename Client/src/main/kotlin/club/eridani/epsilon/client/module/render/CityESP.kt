package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.boundingBox
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.combat.CrystalUtils
import club.eridani.epsilon.client.util.flooredPosition
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.init.Blocks
import net.minecraft.util.EnumFacing
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos

object CityESP : Module(
    name = "CityESP",
    category = Category.Render,
    description = "Render obsidian around player"
) {
    private val range by setting("Range", 6.0, 0.0..50.0, 0.1)
    private val underBlock by setting("UnderBlock", true)
    private val color by setting("Color", ColorRGB(255, 0, 0, 70))
    private val check by setting("CheckCrystal", true)
    private val rMode by setting("Render", RenderMode.Solid)
    private var cityPos = emptyList<BlockPos>()


    init {
        onTick {
            runSafe {
                val newList = ArrayList<BlockPos>()

                for (player in mc.world.playerEntities) {
                    if (player.getDistance(mc.player) > range) continue
                    val doubleTargetPos = player.flooredPosition
                    val posList =
                        if (underBlock)
                            listOf(doubleTargetPos.add(1, 0, 0),
                                doubleTargetPos.add(0, 0, 1),
                                doubleTargetPos.add(-1, 0, 0),
                                doubleTargetPos.add(0, -1, 0),
                                doubleTargetPos.add(0, 0, -1))
                        else
                            listOf(doubleTargetPos.add(1, 0, 0),
                                doubleTargetPos.add(0, 0, 1),
                                doubleTargetPos.add(-1, 0, 0),
                                doubleTargetPos.add(0, 0, -1))
                    for (pos in posList) {
                        if (mc.world.getBlockState(pos).block != Blocks.OBSIDIAN) continue
                        if (check && !ableToPlaceCrystalNearby(pos)) continue
                        newList.add(pos)
                    }
                }

                cityPos = newList
            }
        }

        onRender3D {
            cityPos.forEach {
                drawBlock(it)
            }
        }

    }

    private fun ableToPlaceCrystalNearby(pos: BlockPos): Boolean {
        EnumFacing.HORIZONTALS.forEach {
            if (CrystalUtils.isPlaceable(mc.world, pos.offset(it).down(), false)) {
                return true
            }
        }
        return false
    }

    private fun drawBlock(blockPos: BlockPos) {
        if (rMode == RenderMode.Solid || rMode == RenderMode.SolidFlat) {
            if (rMode == RenderMode.SolidFlat) {
                val bb = blockPos.up().boundingBox
                RenderUtils3D.drawBoundingFilledBox(AxisAlignedBB(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY - 1, bb.maxZ), color.toArgb())
            } else {
                RenderUtils3D.drawBoundingFilledBox(blockPos, color)
            }
        } else {
            if (rMode == RenderMode.Full) {
                RenderUtils3D.drawFullBox(blockPos, 1f, color.toArgb())
            } else if (rMode == RenderMode.Outline) {
                RenderUtils3D.drawBoundingBox(blockPos, 1f, color.toArgb())
            }
        }
    }

    override fun onDisable() {
        cityPos = emptyList()
    }

    enum class RenderMode {
        Solid, SolidFlat, Full, Outline
    }

}