package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.event.SafeClientEvent
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.graphics.ProjectionUtils
import club.eridani.epsilon.client.util.math.MathUtils.scale
import club.eridani.epsilon.client.util.onRender2D
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.runSafe
import club.eridani.epsilon.client.util.world.getSelectedBox
import club.eridani.epsilon.client.common.extensions.*
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import net.minecraft.client.renderer.DestroyBlockProgress
import net.minecraft.client.renderer.Tessellator
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import org.lwjgl.opengl.GL11
import kotlin.math.pow

object BreakESP :
    Module(name = "BreakESP", category = Category.Render, description = "Display brock breaking progress") {

    private val mode by setting("Mode", Mode.Own)
    private val alpha by setting("Alpha", 127, 0..255, 1)
    private val showPercentage by setting("Show Percentage", true)
    private val showAnimation by setting("Show Animation", true)


    enum class Mode {
        Own, Other, All
    }

    init {
        onRender2D {
           runSafe {
                if (!showPercentage) return@runSafe

                for (progress in mc.renderGlobal.getDamagedBlocks.values) {
                    if (isInvalidBreaker(progress)) continue

                    val text = "${(progress.partialBlockDamage + 1) * 10} %"
                    val center = getBoundingBox(progress.position).center
                    val screenPos = ProjectionUtils.toScreenPosScaled(center)
                    val distFactor = (ProjectionUtils.distToCamera(center) - 1.0).coerceAtLeast(0.0)
                    val scale = (3.0f / 2.0.pow(distFactor).toFloat()).coerceAtLeast(0.5f)

                    GL11.glPushMatrix()
                    GL11.glTranslated(screenPos.x, screenPos.y, 0.0)
                    GL11.glScalef(scale, scale, 1.0f)
                    MainFontRenderer.drawCenteredString(text, 0.0f, 0.0f, GUIManager.white, 1f, true)
                    GL11.glPopMatrix()
                }
            }
        }

        onRender3D {
            runSafe {
                val buffer = Tessellator.getInstance().buffer
                buffer.setTranslation(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)

                for (progress in mc.renderGlobal.getDamagedBlocks.values) {
                    if (isInvalidBreaker(progress)) continue
                    val box = getBoundingBox(progress.position, progress.partialBlockDamage + 1)
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.drawBoundingFilledBox(box, GUIManager.firstColor.r, GUIManager.firstColor.g, GUIManager.firstColor.b, alpha)
                }

                buffer.setTranslation(0.0, 0.0, 0.0)
            }
        }
    }

    private fun isInvalidBreaker(progress: DestroyBlockProgress): Boolean {
        val breakerID = progress.entityID
        return when (mode) {
            Mode.Own -> breakerID != mc.player.entityId
            Mode.Other -> breakerID == mc.player.entityId
            else -> false
        }
    }

    private fun SafeClientEvent.getBoundingBox(pos: BlockPos, progress: Int): AxisAlignedBB {
        return if (!showAnimation) {
            return getBoundingBox(pos)
        } else {
            getBoundingBox(pos).scale(progress / 10.0)
        }
    }


    private fun SafeClientEvent.getBoundingBox(pos: BlockPos): AxisAlignedBB {
        return world.getSelectedBox(pos)
    }

}
