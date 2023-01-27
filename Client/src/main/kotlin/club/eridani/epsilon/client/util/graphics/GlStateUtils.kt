package club.eridani.epsilon.client.util.graphics

import club.eridani.epsilon.client.util.Wrapper
import club.eridani.epsilon.client.util.math.Quad
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL20

@Suppress("NOTHING_TO_INLINE")
object GlStateUtils {
    val mc = Wrapper.mc
    var lastScissor: Quad<Int, Int, Int, Int>? = null
    val scissorList = ArrayList<Quad<Int, Int, Int, Int>>()

    private var bindProgram = 0

    fun useProgramForce(id: Int) {
        GL20.glUseProgram(id)
        bindProgram = id
    }

    fun useProgram(id: Int) {
        if (id != bindProgram) {
            GL20.glUseProgram(id)
            bindProgram = id
        }
    }

    inline fun scissor(x: Int, y: Int, width: Int, height: Int) {
        lastScissor = Quad(x, y, width, height)
        glScissor(x, y, width, height)
    }

    inline fun pushScissor() {
        lastScissor?.let {
            scissorList.add(it)
        }
    }

    inline fun popScissor() {
        scissorList.removeLastOrNull()?.let {
            scissor(it.first, it.second, it.third, it.fourth)
        }
    }

    inline fun useVbo(): Boolean {
        return mc.gameSettings.useVbo
    }

    inline fun alpha(state: Boolean) {
        if (state) {
            GlStateManager.enableAlpha()
        } else {
            GlStateManager.disableAlpha()
        }
    }

    inline fun blend(state: Boolean) {
        if (state) {
            GlStateManager.enableBlend()
        } else {
            GlStateManager.disableBlend()
        }
    }

    inline fun smooth(state: Boolean) {
        if (state) {
            GlStateManager.shadeModel(GL_SMOOTH)
        } else {
            GlStateManager.shadeModel(GL_FLAT)
        }
    }

    inline fun lineSmooth(state: Boolean) {
        if (state) {
            glEnable(GL_LINE_SMOOTH)
            glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        } else {
            glDisable(GL_LINE_SMOOTH)
        }
    }

    inline fun depth(state: Boolean) {
        if (state) {
            GlStateManager.enableDepth()
        } else {
            GlStateManager.disableDepth()
        }
    }

    inline fun texture2d(state: Boolean) {
        if (state) {
            GlStateManager.enableTexture2D()
        } else {
            GlStateManager.disableTexture2D()
        }
    }

    inline fun cull(state: Boolean) {
        if (state) {
            GlStateManager.enableCull()
        } else {
            GlStateManager.disableCull()
        }
    }

    inline fun lighting(state: Boolean) {
        if (state) {
            GlStateManager.enableLighting()
        } else {
            GlStateManager.disableLighting()
        }
    }

    inline fun rescaleActual() {
        rescale(Wrapper.mc.displayWidth.toDouble(), Wrapper.mc.displayHeight.toDouble())
    }

    inline fun rescaleMc() {
        val resolution = ScaledResolution(Wrapper.mc)
        rescale(resolution.scaledWidth_double, resolution.scaledHeight_double)
    }

    inline fun pushMatrixAll() {
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
    }

    inline fun popMatrixAll() {
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glPopMatrix()
    }

    inline fun rescale(width: Double, height: Double) {
        GlStateManager.clear(256)
        GlStateManager.viewport(0, 0, mc.displayWidth, mc.displayHeight)
        GlStateManager.matrixMode(GL_PROJECTION)
        GlStateManager.loadIdentity()
        GlStateManager.ortho(0.0, width, height, 0.0, 1000.0, 3000.0)
        GlStateManager.matrixMode(GL_MODELVIEW)
        GlStateManager.loadIdentity()
        GlStateManager.translate(0.0f, 0.0f, -2000.0f)
    }
}