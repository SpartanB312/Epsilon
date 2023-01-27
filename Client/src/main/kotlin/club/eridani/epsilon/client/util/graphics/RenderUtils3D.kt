package club.eridani.epsilon.client.util.graphics

import club.eridani.epsilon.client.common.interfaces.Helper
import club.eridani.epsilon.client.event.events.Render3DEvent
import club.eridani.epsilon.client.event.events.RunGameLoopEvent
import club.eridani.epsilon.client.event.safeListener
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.graphics.buffer.DynamicVAO
import club.eridani.epsilon.client.util.graphics.mask.EnumFacingMask
import club.eridani.epsilon.client.util.graphics.shaders.Shader
import club.eridani.epsilon.client.common.extensions.*
import net.minecraft.client.renderer.*
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.client.shader.Framebuffer
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.EXTFramebufferObject
import org.lwjgl.opengl.EXTPackedDepthStencil
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL32
import java.awt.Color
import kotlin.math.cos
import kotlin.math.sin


object RenderUtils3D : Helper {

    val camera: ICamera = Frustum()
    private val tessellator: Tessellator = Tessellator.getInstance()
    private val bufferbuilder: BufferBuilder = tessellator.buffer
    private var depth = GL11.glIsEnabled(GL11.GL_LIGHTING)
    private var texture = GL11.glIsEnabled(GL11.GL_BLEND)
    private var clean = GL11.glIsEnabled(GL11.GL_TEXTURE_2D)
    private var bind = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
    private var override = GL11.glIsEnabled(GL_LINE_SMOOTH)

    fun glPost() {
        glPost(depth, texture, clean, bind, override)
    }

    private fun glPost(depth: Boolean, texture: Boolean, clean: Boolean, bind: Boolean, override: Boolean) {
        GlStateManager.depthMask(true)
        if (!override) {
            glDisable(GL_LINE_SMOOTH)
        }
        if (bind) {
            glEnable(GL11.GL_DEPTH_TEST)
        }
        if (clean) {
            glEnable(GL11.GL_TEXTURE_2D)
        }
        if (!texture) {
            glDisable(GL11.GL_BLEND)
        }
        if (depth) {
            glEnable(2896)
        }
    }

    fun glPre(lineWidth: Float) {
        depth = GL11.glIsEnabled(GL11.GL_LIGHTING)
        texture = GL11.glIsEnabled(GL11.GL_BLEND)
        clean = GL11.glIsEnabled(GL11.GL_TEXTURE_2D)
        bind = GL11.glIsEnabled(GL11.GL_DEPTH_TEST)
        override = GL11.glIsEnabled(GL_LINE_SMOOTH)
        glPre(depth, texture, clean, bind, override, lineWidth)
    }

    private fun glPre(depth: Boolean, texture: Boolean, clean: Boolean, bind: Boolean, override: Boolean, lineWidth: Float) {
        if (depth) {
            glDisable(GL11.GL_LIGHTING)
        }
        if (!texture) {
            glEnable(GL11.GL_BLEND)
        }
        glLineWidth(lineWidth)
        if (clean) {
            glDisable(GL11.GL_TEXTURE_2D)
        }
        if (bind) {
            glDisable(GL11.GL_DEPTH_TEST)
        }
        if (!override) {
            glEnable(GL_LINE_SMOOTH)
        }
        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.depthMask(false)
    }


    fun jelloRender(entity: EntityLivingBase) {
        val drawTime = (System.currentTimeMillis() % 2000).toInt()
        val drawMode = drawTime > 1000
        var drawPercent = drawTime / 1000f
        //true when goes up
        if (!drawMode) {
            drawPercent = 1 - drawPercent
        } else {
            drawPercent -= 1
        }

        val bb = entity.entityBoundingBox
        val radius = bb.maxX - bb.minX
        val height = bb.maxY - bb.minY
        val posX = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * mc.timer.renderPartialTicks
        val posY = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * mc.timer.renderPartialTicks
        val posZ = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * mc.timer.renderPartialTicks
        val yPos = Easing.IN_OUT_QUART_POW.inc0(drawPercent) * height
        val baseMove = (if (drawPercent > 0.5) {
            1.0f - drawPercent
        } else {
            drawPercent
        }) * 2.0f
        mc.entityRenderer.disableLightmap()
        GL11.glPushMatrix()
        glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glShadeModel(GL_SMOOTH)
        glDisable(GL11.GL_TEXTURE_2D)
        glEnable(GL_LINE_SMOOTH)
        glDisable(GL11.GL_DEPTH_TEST)
        glDisable(GL11.GL_LIGHTING)
        GL11.glDepthMask(false)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)

        GL11.glTranslated(-mc.renderManager.viewerPosX, -mc.renderManager.viewerPosY, -mc.renderManager.viewerPosZ)
        GL11.glBegin(GL11.GL_QUAD_STRIP)
        for (i in 0..360) {
            var moveFace = height * 0.4f * baseMove
            if (drawMode) {
                moveFace = -moveFace
            }
            val calc = i * Math.PI / 180
            val posX2 = posX - sin(calc) * radius
            val posZ2 = posZ + cos(calc) * radius
            GL11.glColor4f(1f, 1f, 1f, 0f)
            GL11.glVertex3d(posX2, posY + yPos + moveFace + 1e-9, posZ2)
            GL11.glColor4f(1f, 1f, 1f, 0.4F)
            GL11.glVertex3d(posX2, posY + yPos, posZ2)
        }
        GL11.glEnd()

        glLineWidth(1f)
        GL11.glBegin(GL11.GL_LINE_LOOP)
        GL11.glColor4f(1f, 1f, 1f, 1f)
        var i = 0
        while (i <= 360) {
            val x = posX - sin(i * Math.PI / 180) * radius
            val z = posZ + cos(i * Math.PI / 180) * radius
            GL11.glVertex3d(x, posY + yPos, z)
            i += 1
        }
        GL11.glEnd()

        GL11.glDepthMask(true)
        glEnable(GL11.GL_DEPTH_TEST)
        glDisable(GL_LINE_SMOOTH)
        glEnable(GL11.GL_TEXTURE_2D)
        glDisable(GL11.GL_BLEND)
        GL11.glPopMatrix()
    }


    fun drawFullBox(pos: BlockPos, width: Float, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawFullBox(pos, width, r, g, b, a)
    }

    fun drawFullBox(bb: AxisAlignedBB, width: Float, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawFullBox(bb, width, r, g, b, a)
    }

    fun drawFullBox(pos: BlockPos, width: Float, red: Int, green: Int, blue: Int, alpha: Int) {
        drawBoundingFilledBox(pos.boundingBox, red, green, blue, alpha)
        drawBoundingBox(pos.boundingBox, width, red, green, blue, 255)
    }

    fun drawFullBox(bb: AxisAlignedBB, width: Float, red: Int, green: Int, blue: Int, alpha: Int) {
        drawBoundingFilledBox(bb, red, green, blue, alpha)
        drawBoundingBox(bb, width, red, green, blue, 255)
    }

    fun drawFullBox(bb: AxisAlignedBB, width: Float, fill: Int, outline: Int) {
        val a = fill ushr 24 and 255
        val r = fill ushr 16 and 255
        val g = fill ushr 8 and 255
        val b = fill and 255

        val oa = outline ushr 24 and 255
        val or = outline ushr 16 and 255
        val og = outline ushr 8 and 255
        val ob = outline and 255
        drawBoundingFilledBox(bb, r, g, b, a)
        drawBoundingBox(bb, width, or, og, ob, oa)
    }

    fun drawBoundingFilledBox(bb: AxisAlignedBB, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawBoundingFilledBox(bb, r, g, b, a)
    }

    fun drawBoundingFilledBox(pos: BlockPos, argb: ColorRGB) {
        drawBoundingFilledBox(pos.boundingBox, argb.r, argb.g, argb.b, argb.a)
    }

    fun drawBoundingFilledBox(pos: BlockPos, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawBoundingFilledBox(pos.boundingBox, r, g, b, a)
    }

    fun drawBoundingFilledBox(pos: BlockPos, red: Int, green: Int, blue: Int, alpha: Int) {
        drawBoundingFilledBox(pos.boundingBox, red, green, blue, alpha)
    }

    fun drawBoundingFilledBox(bb: AxisAlignedBB, red: Int, green: Int, blue: Int, alpha: Int) {
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
        drawFilledBox(bb)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawBoundingFilledBox(bb: AxisAlignedBB, color: Color) {
        GlStateManager.color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), color.alpha.toFloat())
        drawFilledBox(bb)
        GlStateManager.color(1f, 1f, 1f, 1f)
    }

    fun drawSelectionGlowFilledBox(axisAlignedBB: AxisAlignedBB, height: Double, startColor: Color, endColor: Color) {
        GlStateManager.disableCull()
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        drawSelectionGlowFilledBox(axisAlignedBB.setMaxY(axisAlignedBB.maxY + height), startColor, endColor)
        GlStateManager.enableCull()
        GlStateManager.enableAlpha()
        GlStateManager.shadeModel(GL_FLAT)
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawBoundingBox(bb, width, r, g, b, a)
    }

    fun drawBoundingBox(bb: BlockPos, width: Float, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        drawBoundingBox(bb, width, r, g, b, a)
    }

    fun drawBoundingBox(pos: BlockPos, width: Float, red: Int, green: Int, blue: Int, alpha: Int) {
        drawBoundingBox(pos.boundingBox, width, red, green, blue, alpha)
    }

    fun drawBoundingBox(bb: AxisAlignedBB, width: Float, red: Int, green: Int, blue: Int, alpha: Int) {
        glLineWidth(width)
        glEnable(GL_LINE_SMOOTH)
        GlStateManager.color(red / 255f, green / 255f, blue / 255f, alpha / 255f)
        drawBoundingBox(bb)
        GlStateManager.color(1f, 1f, 1f, 1f)
        glDisable(GL_LINE_SMOOTH)
    }

    fun drawBoundingBox(boundingBox: AxisAlignedBB) {
        bufferbuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION)
        run {
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex()
            bufferbuilder.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex()
        }
        tessellator.draw()
    }

    fun drawSelectionGlowFilledBox(axisAlignedBB: AxisAlignedBB, startColor: Color, endColor: Color) {
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR)
        run {
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(startColor.red, startColor.green, startColor.blue, startColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(endColor.red, endColor.green, endColor.blue, endColor.alpha).endVertex()
        }
        tessellator.draw()
    }

    fun drawClawBox(axisAlignedBB: AxisAlignedBB, height: Double, color: Color) {
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        GL11.glDepthMask(false)
        addChainedClawBoxVertices(axisAlignedBB.setMaxY(axisAlignedBB.maxY + height), color)
        glDisable(GL_LINE_SMOOTH)
        GL11.glDepthMask(true)
    }

    fun addChainedClawBoxVertices(axisAlignedBB: AxisAlignedBB, color: Color) {
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        run {
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ - 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ + 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ - 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ + 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX - 0.8, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX - 0.8, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX + 0.8, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX + 0.8, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY + 0.2, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY + 0.2, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY + 0.2, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY + 0.2, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ - 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ + 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ - 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ + 0.8).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX - 0.8, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX - 0.8, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX + 0.8, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX + 0.8, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY - 0.2, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY - 0.2, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY - 0.2, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY - 0.2, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
        }
        tessellator.draw()
    }

    fun drawChainedBox(axisAlignedBB: AxisAlignedBB, height: Double, color: Color) {
        glEnable(GL_LINE_SMOOTH)
        GL11.glDepthMask(false)
        addChainedBoundingBoxVertices(axisAlignedBB.setMaxY(axisAlignedBB.maxY + height), color)
        glDisable(GL_LINE_SMOOTH)
        GL11.glDepthMask(true)
    }

    fun addChainedBoundingBoxVertices(axisAlignedBB: AxisAlignedBB, color: Color) {
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)
        run {
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red, color.green, color.blue, color.alpha).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).color(color.red.toFloat(), color.green.toFloat(), color.blue.toFloat(), 0.0f).endVertex()
        }
        tessellator.draw()
    }

    fun drawFilledBox(axisAlignedBB: AxisAlignedBB) {
        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_NORMAL)
        run {
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(-1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(-1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(-1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(-1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(1.0f, 0.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(0.0f, -1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(0.0f, -1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(0.0f, -1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(0.0f, -1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(0.0f, 1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(0.0f, 1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(0.0f, 1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(0.0f, 1.0f, 0.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(0.0f, 0.0f, -1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ).normal(0.0f, 0.0f, -1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(0.0f, 0.0f, -1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ).normal(0.0f, 0.0f, -1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(0.0f, 0.0f, 1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ).normal(0.0f, 0.0f, 1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(0.0f, 0.0f, 1.0f).endVertex()
            bufferbuilder.pos(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ).normal(0.0f, 0.0f, 1.0f).endVertex()
        }
        tessellator.draw()
    }

    fun drawCircleESP(entity: Entity, partialTicks: Float, rad: Double, argb: Int) {
        val a = argb ushr 24 and 255
        val r = argb ushr 16 and 255
        val g = argb ushr 8 and 255
        val b = argb and 255
        glEnable(GL_LINE_SMOOTH)
        glLineWidth(1f)
        GL11.glDepthMask(false)
        val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.renderManager.viewerPosX
        val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.renderManager.viewerPosY
        val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.renderManager.viewerPosZ
        bufferbuilder.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR)


        for (i in 0..360) {
            bufferbuilder.pos(x - sin(i * Math.PI / 180F) * rad,
                y,
                z + cos(i * Math.PI / 180F) * rad).color(r, g, b, a).endVertex()
        }
        tessellator.draw()
        glDisable(GL_LINE_SMOOTH)
        GL11.glDepthMask(true)
    }

    var vertexSize = 0
    var translationX = 0.0; private set
    var translationY = 0.0; private set
    var translationZ = 0.0; private set

    fun setTranslation(x: Double, y: Double, z: Double) {
        translationX = x
        translationY = y
        translationZ = z
    }

    fun resetTranslation() {
        translationX = 0.0
        translationY = 0.0
        translationZ = 0.0
    }

    @JvmStatic
    var partialTicks = 0.0f; private set
    var camPos: Vec3d = Vec3d.ZERO; private set

    init {
        safeListener<RunGameLoopEvent.Tick> {
            partialTicks = if (mc.isGamePaused) mc.renderPartialTicksPaused else mc.renderPartialTicks
        }

        safeListener<Render3DEvent> {
            setTranslation(-mc.renderManager.renderPosX, -mc.renderManager.renderPosY, -mc.renderManager.renderPosZ)
        }

        safeListener<Render3DEvent>(Int.MAX_VALUE, true) {
            val entity = mc.renderViewEntity ?: player
            val ticks = partialTicks
            val x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * ticks
            val y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * ticks
            val z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * ticks
            val camOffset = ActiveRenderInfo.getCameraPosition()

            camPos = Vec3d(x + camOffset.x, y + camOffset.y, z + camOffset.z)
        }
    }

    fun drawBox(box: AxisAlignedBB, color: ColorRGB, sides: Int) {
        if (sides and EnumFacingMask.DOWN != 0) {
            putVertex(box.minX, box.minY, box.maxZ, color)
            putVertex(box.minX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.maxZ, color)
        }

        if (sides and EnumFacingMask.UP != 0) {
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
        }

        if (sides and EnumFacingMask.NORTH != 0) {
            putVertex(box.minX, box.minY, box.minZ, color)
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.minY, box.minZ, color)
        }

        if (sides and EnumFacingMask.SOUTH != 0) {
            putVertex(box.maxX, box.minY, box.maxZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.minY, box.maxZ, color)
        }

        if (sides and EnumFacingMask.WEST != 0) {
            putVertex(box.minX, box.minY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.maxZ, color)
            putVertex(box.minX, box.maxY, box.minZ, color)
            putVertex(box.minX, box.minY, box.minZ, color)
        }

        if (sides and EnumFacingMask.EAST != 0) {
            putVertex(box.maxX, box.minY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.minZ, color)
            putVertex(box.maxX, box.maxY, box.maxZ, color)
            putVertex(box.maxX, box.minY, box.maxZ, color)
        }
    }

    fun drawLineTo(position: Vec3d, color: ColorRGB) {
        putVertex(camPos.x, camPos.y, camPos.z, color)
        putVertex(position.x, position.y, position.z, color)
    }

    fun drawOutline(box: AxisAlignedBB, color: ColorRGB) {
        putVertex(box.minX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.minZ, color)

        putVertex(box.minX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.minZ, color)

        putVertex(box.minX, box.minY, box.minZ, color)
        putVertex(box.minX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.minZ, color)
        putVertex(box.maxX, box.maxY, box.minZ, color)
        putVertex(box.maxX, box.minY, box.maxZ, color)
        putVertex(box.maxX, box.maxY, box.maxZ, color)
        putVertex(box.minX, box.minY, box.maxZ, color)
        putVertex(box.minX, box.maxY, box.maxZ, color)
    }

    fun putVertex(posX: Double, posY: Double, posZ: Double, color: ColorRGB) {
        DynamicVAO.buffer.apply {
            putFloat((posX + translationX).toFloat())
            putFloat((posY + translationY).toFloat())
            putFloat((posZ + translationZ).toFloat())
            putInt(color.rgba)
        }
        vertexSize++
    }

    fun draw(mode: Int) {
        if (vertexSize == 0) return

        DynamicVAO.POS3_COLOR.upload(vertexSize)

        DrawShader.bind()
        DynamicVAO.POS3_COLOR.useVao {
            glDrawArrays(mode, 0, vertexSize)
        }

        vertexSize = 0
    }


    fun renderOne(lineWidth: Float) {
        checkSetupFBO()
        glPushAttrib(GL_ALL_ATTRIB_BITS)
        glDisable(GL_ALPHA_TEST)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_LIGHTING)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glLineWidth(lineWidth)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL_STENCIL_TEST)
        glClear(GL_STENCIL_BUFFER_BIT)
        glClearStencil(0xF)
        glStencilFunc(GL_NEVER, 1, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    fun renderTwo() {
        glStencilFunc(GL_NEVER, 0, 0xF)
        glStencilOp(GL_REPLACE, GL_REPLACE, GL_REPLACE)
        glPolygonMode(GL_FRONT_AND_BACK, GL_FILL)
    }

    fun renderThree() {
        glStencilFunc(GL_EQUAL, 1, 0xF)
        glStencilOp(GL_KEEP, GL_KEEP, GL_KEEP)
        glPolygonMode(GL_FRONT_AND_BACK, GL_LINE)
    }

    fun renderFour(color: ColorRGB) {
        glColor4f(color.rFloat, color.gFloat, color.bFloat, color.aFloat)
        glDepthMask(false)
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_POLYGON_OFFSET_LINE)
        glPolygonOffset(1.0f, -2000000f)
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0f, 240.0f)
    }

    fun renderFive() {
        glPolygonOffset(1.0f, 2000000f)
        glDisable(GL_POLYGON_OFFSET_LINE)
        glEnable(GL_DEPTH_TEST)
        glDepthMask(true)
        glDisable(GL_STENCIL_TEST)
        glDisable(GL_LINE_SMOOTH)
        glHint(GL_LINE_SMOOTH_HINT, GL_DONT_CARE)
        glEnable(GL_BLEND)
        glEnable(GL_LIGHTING)
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_ALPHA_TEST)
        glPopAttrib()
    }

    fun checkSetupFBO() {
        // Gets the FBO of Minecraft
        val fbo = mc.framebuffer

        // Check if FBO isn't null
        if (fbo != null) {
            // Checks if screen has been resized or new FBO has been created
            if (fbo.depthBuffer > -1) {
                // Sets up the FBO with depth and stencil extensions (24/8 bit)
                setupFBO(fbo)
                // Reset the ID to prevent multiple FBO's
                fbo.depthBuffer = -1
            }
        }
    }

    private fun setupFBO(fbo: Framebuffer) {
        // Deletes old render buffer extensions such as depth
        // Args: Render Buffer ID
        EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer)
        // Generates a new render buffer ID for the depth and stencil extension
        val stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT()
        // Binds new render buffer by ID
        // Args: Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glBindRenderbufferEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        // Adds the depth and stencil extension
        // Args: Target (GL_RENDERBUFFER_EXT), Extension (GL_DEPTH_STENCIL_EXT),
        // Width, Height
        EXTFramebufferObject.glRenderbufferStorageEXT(EXTFramebufferObject.GL_RENDERBUFFER_EXT, EXTPackedDepthStencil.GL_DEPTH_STENCIL_EXT, mc.displayWidth, mc.displayHeight)
        // Adds the stencil attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_STENCIL_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_STENCIL_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
        // Adds the depth attachment
        // Args: Target (GL_FRAMEBUFFER_EXT), Attachment
        // (GL_DEPTH_ATTACHMENT_EXT), Target (GL_RENDERBUFFER_EXT), ID
        EXTFramebufferObject.glFramebufferRenderbufferEXT(EXTFramebufferObject.GL_FRAMEBUFFER_EXT, EXTFramebufferObject.GL_DEPTH_ATTACHMENT_EXT, EXTFramebufferObject.GL_RENDERBUFFER_EXT, stencilDepthBufferID)
    }


    fun prepareGL() {
        GlStateManager.pushMatrix()
        glLineWidth(1f)
        glEnable(GL_LINE_SMOOTH)
        glEnable(GL32.GL_DEPTH_CLAMP)
        glHint(GL_LINE_SMOOTH_HINT, GL_NICEST)
        GlStateManager.disableAlpha()
        GlStateManager.shadeModel(GL_SMOOTH)
        GlStateManager.enableBlend()
        GlStateManager.depthMask(false)
        GlStateManager.disableTexture2D()
        GlStateManager.disableLighting()
    }

    fun releaseGL() {
        GlStateManager.enableTexture2D()
        GlStateManager.enableDepth()
        GlStateManager.disableBlend()
        GlStateManager.shadeModel(GL_FLAT)
        GlStateManager.enableAlpha()
        GlStateManager.depthMask(true)
        glDisable(GL32.GL_DEPTH_CLAMP)
        glDisable(GL_LINE_SMOOTH)
        GlStateManager.color(1f, 1f, 1f)
        glLineWidth(1f)
        GlStateManager.popMatrix()
    }

    private object DrawShader : Shader("/assets/minecraft/shaders/general/Pos3Color.vsh", "/assets/minecraft/shaders/general/Pos3Color.fsh")


}