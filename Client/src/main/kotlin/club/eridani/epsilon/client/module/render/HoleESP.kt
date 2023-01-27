package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.HoleManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.combat.HoleType
import club.eridani.epsilon.client.util.extension.AxisAlignedBB.interp
import club.eridani.epsilon.client.util.extension.eyesPosition
import club.eridani.epsilon.client.util.extension.flooredPosition
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.math.sq
import club.eridani.epsilon.client.util.math.vector.toBlockPos
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.onTick
import net.minecraft.util.math.AxisAlignedBB

object HoleESP :
    Module(name = "HoleESP", category = Category.Render, description = "Render hole with bedrock or obsidian") {
    private val doubleHole by setting("2x1 Hole", true)
    private val quadHole by setting("2x2 Hole", true)

    val range by setting("Range", 6.0, 0.0..15.0, 0.1)
    val count by setting("Hole Count", 10, 0..50, 1)
    val mode by setting("Mode", RenderMode.Box)
    private val page by setting("Page", Page.Obby)

    private val bedRockColor by setting("Bedrock Color", ColorRGB(130, 104, 0, 39)) { page == Page.BedRock }
    private val obbyColor by setting("Obby Color", ColorRGB(255, 0, 0, 39)) { page == Page.Obby }
    private val twoColor by setting("2x1 Color", ColorRGB(255, 0, 0, 39)) { page == Page.Double }
    private val fourColor by setting("2x2 Color", ColorRGB(255, 0, 0, 39)) { page == Page.Quad }

    private val hideOwn by setting("HideOwn", false)
    private val low by setting("LowHole", false) { mode != RenderMode.Glow }
    private val outline by setting("Outline", true) { mode != RenderMode.Glow }
    val width by setting("Width", 1.0f, 0.1f..10.0f, .1f) { outline && mode != RenderMode.Glow }
    val y by setting("Y", 0.0, 0.0..1.0, 0.1) { mode != RenderMode.Glow }
    private val highlight by setting("Highlight", HighLight.WireFrame) { mode == RenderMode.Glow }
    private val glowHeight by setting("Glow Height", 2.0, .25..4.0, 0.01) { mode == RenderMode.Glow }
    private val outlineHeight by setting("Outline Height", 0.0, -1.0..3.0, 0.1) { mode == RenderMode.Glow }

    private var safeHoles = ArrayList<Pair<AxisAlignedBB, ColorRGB>>()


    init {
        onTick {
            updateRenderer()
        }

        onRender3D {
            if (safeHoles.isEmpty()) return@onRender3D
            safeHoles.forEach { (v, k) ->
                drawHoleESP(v, k)
            }
        }
    }

    private fun updateRenderer() {
        val eyesPos = mc.player.eyesPosition
        val flooredPosition = mc.player.flooredPosition
        val rangeSq = range.sq
        val tempSafeHoles = ArrayList<Pair<AxisAlignedBB, ColorRGB>>()
        for (holeInfo in HoleManager.holeInfos) {
            val pos = holeInfo.origin
            if (!doubleHole && holeInfo.isTwo) continue
            if (!quadHole && holeInfo.isFour) continue

            if (!holeInfo.isTwo && !holeInfo.isFour) {
                if (!mc.world.isAirBlock(holeInfo.center.add(0.0, 2.0, 0.0).toBlockPos())) continue
            }

            if (hideOwn && pos == flooredPosition) continue
            if (eyesPos.squareDistanceTo(holeInfo.center) > rangeSq) continue
            if (count == 0 || tempSafeHoles.size < count) {
                tempSafeHoles.add(holeInfo.boundingBox.offset(0.0, if (low) -1.0 else 0.0, 0.0) to (when (holeInfo.type) {
                    HoleType.BEDROCK -> bedRockColor
                    HoleType.OBBY -> obbyColor
                    HoleType.TWO -> twoColor
                    HoleType.FOUR -> fourColor
                    else -> fourColor
                }))
            }
        }

        safeHoles = tempSafeHoles
    }

    private fun drawHoleESP(bound: AxisAlignedBB, color: ColorRGB) {
        if (mode == RenderMode.Glow) {
            RenderUtils3D.drawSelectionGlowFilledBox(bound.interp(), glowHeight - 1, color.toColor(), color.alpha(0).toColor())
            when (highlight) {
                HighLight.WireFrame -> RenderUtils3D.drawChainedBox(bound.interp(), outlineHeight - 1, color.alpha(255).toColor())
                HighLight.Claw -> RenderUtils3D.drawClawBox(bound.interp(), outlineHeight - 1, color.alpha(255).toColor())
                else -> {
                }
            }
        } else {
            if (outline) {
                RenderUtils3D.drawFullBox(AxisAlignedBB(bound.minX, bound.minY, bound.minZ, bound.maxX, bound.maxY - (1 - y), bound.maxZ).interp(), width, color.toArgb())
            } else {
                RenderUtils3D.drawBoundingFilledBox(AxisAlignedBB(bound.minX, bound.minY, bound.minZ, bound.maxX, bound.maxY - (1 - y), bound.maxZ).interp(), color.toArgb())
            }
        }
    }

    enum class Page {
        Obby, BedRock, Double, Quad
    }

    enum class RenderMode {
        Box, Glow
    }

    enum class HighLight {
        WireFrame, Claw, None
    }
}