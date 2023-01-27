package club.eridani.epsilon.client.util.graphics.shaders

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.events.ResolutionUpdateEvent
import club.eridani.epsilon.client.event.listener
import club.eridani.epsilon.client.util.graphics.GlStateUtils
import club.eridani.epsilon.client.util.graphics.MatrixUtils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.OpenGlHelper
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import org.joml.Matrix4f
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.GL_QUADS
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL32

object WindowBlurShader : Helper {

    private val framebuffer = Framebuffer(mc.displayWidth, mc.displayHeight, false)
    private val pass1 = Pass("/assets/minecraft/shaders/gui/WindowBlurH.vsh")
    private val pass2 = Pass("/assets/minecraft/shaders/gui/WindowBlurV.vsh")

    init {
        updateResolution(mc.displayWidth, mc.displayHeight)

        listener<ResolutionUpdateEvent>(true) {
            updateResolution(it.width, it.height)
        }
    }

    private fun updateResolution(width: Int, height: Int) {
        pass1.bind()
        pass1.updateResolution(width.toFloat(), height.toFloat())
        pass2.bind()
        pass2.updateResolution(width.toFloat(), height.toFloat())
        framebuffer.createBindFramebuffer(width, height)
    }

    fun render(x: Float, y: Float) {
        render(0.0f, 0.0f, x, y)
    }

    fun render(x1: Float, y1: Float, x2: Float, y2: Float) {
        GlStateUtils.alpha(false)
        GlStateUtils.depth(false)
        GL11.glEnable(GL32.GL_DEPTH_CLAMP)

        val tessellator = Tessellator.getInstance()
        val buffer = tessellator.buffer

        val x1D = x1.toDouble()
        val y1D = y1.toDouble()
        val x2D = x2.toDouble()
        val y2D = y2.toDouble()

        GlStateManager.enableTexture2D()
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit)
        GlStateManager.disableDepth()
        GlStateManager.depthMask(false)

        mc.framebuffer.bindFramebufferTexture()
        framebuffer.bindFramebuffer(false)
        GlStateUtils.blend(false)

        pass1.bind()
        pass1.updateMatrix()

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        buffer.pos(x1D, x1D, 1.0).endVertex()
        buffer.pos(x1D, y2D, -1.0).endVertex()
        buffer.pos(x2D, y2D, -1.0).endVertex()
        buffer.pos(x2D, y1D, 1.0).endVertex()
        tessellator.draw()

        framebuffer.bindFramebufferTexture()
        mc.framebuffer.bindFramebuffer(false)

        pass2.bind()
        pass2.updateMatrix()

        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION)
        buffer.pos(x1D, y1D, -1.0).endVertex()
        buffer.pos(x1D, y2D, -1.0).endVertex()
        buffer.pos(x2D, y2D, 1.0).endVertex()
        buffer.pos(x2D, y1D, 1.0).endVertex()
        tessellator.draw()

        framebuffer.unbindFramebufferTexture()
        mc.framebuffer.bindFramebuffer(false)
        GlStateUtils.blend(true)

        GL11.glDisable(GL32.GL_DEPTH_CLAMP)
        GlStateUtils.useProgram(0)
    }

    private open class Pass(vertShaderPath: String) :
        DrawShader(vertShaderPath, "/assets/minecraft/shaders/gui/WindowBlur.fsh") {
        val reverseProjectionUniform = glGetUniformLocation(id, "reverseProjection")
        val resolutionUniform = glGetUniformLocation(id, "resolution")

        init {
            use {
                updateResolution(mc.displayWidth.toFloat(), mc.displayHeight.toFloat())
                glUniform1i(glGetUniformLocation(id, "background"), 0)
            }
        }

        fun updateResolution(width: Float, height: Float) {
            glUniform2f(resolutionUniform, width, height)

            val matrix = Matrix4f()
                .ortho(0.0f, width, 0.0f, height, 1000.0f, 3000.0f)
                .invert()

            MatrixUtils.loadMatrix(matrix).uploadMatrix(reverseProjectionUniform)
        }
    }
}