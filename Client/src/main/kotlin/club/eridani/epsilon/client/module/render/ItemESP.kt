package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.extension.AxisAlignedBB.interp
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import club.eridani.epsilon.client.util.graphics.ProjectionUtils
import club.eridani.epsilon.client.util.graphics.RenderUtils3D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.onRender2D
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityEnderPearl
import net.minecraft.entity.item.EntityExpBottle
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.item.EntityXPOrb
import net.minecraft.entity.projectile.EntityArrow
import org.lwjgl.opengl.GL11.*


object ItemESP :
    Module(name = "ItemESP", category = Category.Render, description = "Highlighting items through walls") {


    val exp by setting("EXP", false)
    private val orb by setting("XP Orb", false)
    private val EPearl by setting("EnderPearl", true)
    val mode by setting("Mode", Mode.Box)
    private val renderText by setting("RenderText", false)
    private val textSize by setting("TextSize", 1.0F, 0.0F..4.0F, 0.1F)
    val width by setting("Width", 1.5f, 0.1f..5f, 0.1f)
    val alpha by setting("Transparency", 255, 0..255, 1)

    val camera: ICamera = Frustum()

    init {
        onRender3D {
            runSafe {
                camera.setPosition(mc.renderViewEntity!!.posX, mc.renderViewEntity!!.posY, mc.renderViewEntity!!.posZ)
                for (entity in mc.world.loadedEntityList) {
                    if (!camera.isBoundingBoxInFrustum(entity.renderBoundingBox)) {
                        continue
                    }
                    if (entity is EntityItem || entity is EntityArrow) drawBox(entity)
                    if (exp) (entity as? EntityExpBottle)?.let { drawBox(it) }
                    if (orb) (entity as? EntityXPOrb)?.let { drawBox(it) }
                    if (EPearl) (entity as? EntityEnderPearl)?.let { drawBox(it) }
                }
            }
        }

        onRender2D {
            runSafe {
                if (renderText) {
                    drawItemText()
                }
            }
        }
    }

    private fun drawBox(entity: Entity) {
        val alpha: Int = alpha
        when (mode) {
            Mode.Box -> RenderUtils3D.drawBoundingFilledBox(entity.entityBoundingBox.interp(), GUIManager.firstColor.r, GUIManager.firstColor.g, GUIManager.firstColor.b, alpha)
            Mode.FullBox -> RenderUtils3D.drawFullBox(entity.entityBoundingBox.interp(), width, GUIManager.firstColor.r, GUIManager.firstColor.g, GUIManager.firstColor.b, alpha)
            Mode.Line -> RenderUtils3D.drawBoundingBox(entity.entityBoundingBox.interp(), width, GUIManager.firstColor.r, GUIManager.firstColor.g, GUIManager.firstColor.b, alpha)
        }
    }

    private fun drawItemText() {
        GlStateUtils.pushMatrixAll()
        GlStateUtils.blend(true)
        GlStateUtils.texture2d(true)
        GlStateUtils.depth(false)

        mc.world.loadedEntityList
            .filter { EntityItem::class.java.isInstance(it) }
            .map { EntityItem::class.java.cast(it) }
            .filter { it.ticksExisted > 1 }
            .forEach {
                GlStateUtils.rescale(mc.displayWidth.toDouble(), mc.displayHeight.toDouble())

                val bottomPos = MathUtils.getInterpolatedPos(it, mc.renderPartialTicks)
                val topPos = bottomPos.add(0.0, it.renderBoundingBox.maxY - it.posY, 0.0)

                val top = ProjectionUtils.toAbsoluteScreenPos(topPos)
                val bot = ProjectionUtils.toAbsoluteScreenPos(bottomPos)

                val offX = bot.x - top.x
                val offY = bot.y - top.y

                GlStateUtils.pushMatrixAll()

                glTranslated(top.x - offX / 2.0, bot.y, 0.0)
                glScalef(textSize * 2.0f, textSize * 2.0f, 1.0f)

                val stack = it.item
                val text = stack.displayName + if (stack.isStackable) " x" + stack.count else ""

                MainFontRenderer.drawStringWithShadow(text, (offX / 2.0 - MainFontRenderer.getWidth(text) / 2.0).toFloat(),
                    (-(offY - MainFontRenderer.getHeight()/ 2.0)).toInt() - 1.toFloat(), GUIManager.white)
                GlStateUtils.popMatrixAll()
                GlStateUtils.rescaleMc()
            }

        GlStateUtils.depth(true)
        GlStateUtils.texture2d(true)
        GlStateUtils.blend(false)
        GlStateUtils.popMatrixAll()
    }


    enum class Mode {
        Box,
        FullBox,
        Line
    }
}