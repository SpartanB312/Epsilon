package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.*
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import net.minecraft.network.play.client.CPacketUseEntity
import org.lwjgl.opengl.GL11

object Crosshair : Module(
    name = "Crosshair",
    category = Category.Render,
    description = "Re-Render the crosshair in your screen"
) {

    //TODO: Sniper: render lines across the screen when bow is charged
    //      DeadESP: render last post player before he die
    val corsshair by setting("Crosshair", true)
    private val crosshairColor by setting("Crosshair Color", ColorRGB(0, 255, 0)) { corsshair }
    private val dot by setting("Dot", false) { corsshair }
    private val crosshairGap by setting("Crosshair Gap", 5f, 0.25f..15f, 0.01f)  { corsshair }
    private val size by setting("Size", 7f, 0.25f..15f, 0.01f)  { corsshair }
    private val width by setting("Width", 0.5f, 0.25f..10f, 0.01f)  { corsshair }

    private val hitMarker by setting("HitMarker", false)
    private val coolDown by setting("CoolDown Tick", 30, 1..200, 1) { hitMarker }
    private val gap by setting("Gap",  5f, 0.25f..15f, 0.01f) { hitMarker }
    val length by setting("Length", 7f, 0.25f..15f, 0.01f) { hitMarker }

    private var coolDownTicks = 0f

    init {
        onPacketSend { event ->
            if (event.packet is CPacketUseEntity && event.packet.action == CPacketUseEntity.Action.ATTACK) {
                val target = event.packet.getEntityFromWorld(mc.world) ?: return@onPacketSend
                if (target == mc.player) {
                    return@onPacketSend
                }
                coolDownTicks = coolDown.toFloat()
            }
        }
        onTick {
            if (coolDownTicks > 0) {
                coolDownTicks--
            }
        }
        onRender2D {
            val scaledWidth = ScaleHelper.scaledResolution.scaledWidth
            val scaledHeight = ScaleHelper.scaledResolution.scaledHeight
            val x = scaledWidth / 2f
            val y = scaledHeight / 2f
            if (corsshair) {
                val sizeGap = size + crosshairGap
                if (width >= .5) {
                    RenderUtils2D.drawBorderedRect(x - sizeGap, y - width, x - crosshairGap, y + width, .5F, ColorRGB(0, 0, 0), crosshairColor)
                    RenderUtils2D.drawBorderedRect(x + crosshairGap, y - width, x + sizeGap, y + width, .5F, ColorRGB(0, 0, 0), crosshairColor)
                    RenderUtils2D.drawBorderedRect(x - width, y - sizeGap, x + width, y - crosshairGap, .5F, ColorRGB(0, 0, 0), crosshairColor)
                    RenderUtils2D.drawBorderedRect(x - width, y + crosshairGap, x + width, y + sizeGap, .5F, ColorRGB(0, 0, 0), crosshairColor)
                    if (dot) RenderUtils2D.drawBorderedRect(x - width, y - width, x + width, y + width, .5f, ColorRGB(0, 0, 0), crosshairColor)
                } else {
                    RenderUtils2D.drawRectFilled(x - sizeGap, y - width, x - crosshairGap, y + width, crosshairColor)
                    RenderUtils2D.drawRectFilled(x + crosshairGap, y - width, x + sizeGap, y + width, crosshairColor)
                    RenderUtils2D.drawRectFilled(x - width, y - sizeGap, x + width, y - crosshairGap, crosshairColor)
                    RenderUtils2D.drawRectFilled(x - width, y + crosshairGap, x + width, y + sizeGap, crosshairColor)
                    if (dot) RenderUtils2D.drawRectFilled(x - width, y - width, x + width, y + width, crosshairColor)
                }
            }
            if (coolDownTicks > 0 && hitMarker) {
                RenderUtils2D.prepareGl()
                GL11.glColor4f(1f, 1f, 1f, (coolDownTicks / coolDown))
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glLineWidth(1f)
                GL11.glBegin(3)
                GL11.glVertex2f(x + gap, y + gap)
                GL11.glVertex2f(x + gap + length, y + gap + length)
                GL11.glEnd()
                GL11.glBegin(3)
                GL11.glVertex2f(x - gap, y - gap)
                GL11.glVertex2f(x - gap - length, y - gap - length)
                GL11.glEnd()
                GL11.glBegin(3)
                GL11.glVertex2f(x - gap, y + gap)
                GL11.glVertex2f(x - gap - length, y + gap + length)
                GL11.glEnd()
                GL11.glBegin(3)
                GL11.glVertex2f(x + gap, y - gap)
                GL11.glVertex2f(x + gap + length, y- gap - length)
                GL11.glEnd()
                RenderUtils2D.releaseGl()
            }
        }
    }
}