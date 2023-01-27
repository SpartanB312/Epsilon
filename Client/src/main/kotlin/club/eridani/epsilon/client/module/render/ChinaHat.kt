package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.onRender3D
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.Cylinder
import org.lwjgl.util.glu.GLU

object ChinaHat : Module(name = "ChinaHat", category = Category.Render, description = "緊跟偉大領袖毛主席奮勇前進") {

    private val syncGUI by setting("SyncGui", true)
    private val display by setting("Display", false)
    val color by setting("Color", ColorRGB(255, 0, 0, 255))
    val mode by setting("Mode", Mode.ChinaHat)

    init {
        onRender3D {
            if (Utils.nullCheck()) return@onRender3D
            if (!display) if (mc.gameSettings.thirdPersonView == 0) return@onRender3D
            GL11.glPushMatrix()
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glShadeModel(GL11.GL_SMOOTH)
            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_LINE_SMOOTH)
            GL11.glDisable(GL11.GL_DEPTH_TEST)
            GL11.glDepthMask(false)
            GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
            GL11.glDisable(GL11.GL_CULL_FACE)
            GL11.glColor4f((if (syncGUI) GUIManager.firstColor else color).r / 255f, (if (syncGUI) GUIManager.firstColor else color).g / 255f, (if (syncGUI) GUIManager.firstColor else color).b / 255f, color.a / 255f)
            GL11.glTranslatef(0f, mc.player.height + 0.4f, 0f)
            GL11.glRotatef(90f, 1f, 0f, 0f)

            //30 for circle
            val shaft = Cylinder()
            when(mode) {
                Mode.Umbrella -> {
                    shaft.drawStyle = GLU.GLU_LINE
                    shaft.draw(0f, 0.7f, 0.3f, 8, 1)
                    shaft.drawStyle = GLU.GLU_FILL
                    shaft.draw(0f, 0.7f, 0.3f, 8, 1)
                }
                Mode.FullChinaHat -> {
                    shaft.drawStyle = GLU.GLU_LINE
                    shaft.draw(0f, 0.7f, 0.3f, 30, 1)
                    shaft.drawStyle = GLU.GLU_FILL
                    shaft.draw(0f, 0.7f, 0.3f, 30, 1)
                }
                Mode.ChinaHat -> {
                    shaft.drawStyle = GLU.GLU_FILL
                    shaft.draw(0f, 0.7f, 0.3f, 60, 1)
                }
            }

            GlStateManager.resetColor()
            GL11.glEnable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_DEPTH_TEST)
            GL11.glDisable(GL11.GL_LINE_SMOOTH)
            GL11.glDepthMask(true)
            GL11.glDisable(GL11.GL_BLEND)
            GL11.glEnable(GL11.GL_CULL_FACE)
            GL11.glPopMatrix()
        }
    }

    enum class Mode {
        Umbrella,
        ChinaHat,
        FullChinaHat
    }

}