package club.eridani.epsilon.client.util.graphics.particles

import club.eridani.epsilon.client.util.Wrapper.mc
import club.eridani.epsilon.client.util.graphics.particles.Particle.Companion.generateParticle
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.math.MathHelper
import org.lwjgl.input.Mouse
import org.lwjgl.opengl.GL11

open class ParticleSystem(initAmount: Int) {
    private val particleList: MutableList<Particle> = ArrayList()
    private var LINE = true

    fun tick(delta: Int) {
        //if (Mouse.isButtonDown(0)) addParticles(1);
        for (particle in particleList) {
            particle.tick(delta, SPEED)
        }
    }

    fun render() {
        val screen = mc.currentScreen ?: return
        val resolution = ScaledResolution(mc)
        val scaleFactor = resolution.scaleFactor.toFloat()
        val mouseX = Mouse.getX() / scaleFactor - 1.0f
        val mouseY = (mc.displayHeight - 1 - Mouse.getY()) / scaleFactor
        prepareGL()
        val tessellator = Tessellator.getInstance()
        val bufferBuilder = tessellator.buffer
        bufferBuilder.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR)
        for (particle in particleList) {
            GL11.glColor4f(1.0f, 1.0f, 1.0f, particle.alpha / 255.0f)
            GL11.glPointSize(particle.size)
            GL11.glBegin(GL11.GL_POINTS)
            GL11.glVertex2f(particle.x, particle.y)
            GL11.glEnd()
            if (distanceSq(mouseX, mouseY, particle.x, particle.y) > LINE_RADIUS_SQ) {
                continue
            }
            var nearestDistance = Float.MAX_VALUE
            var nearestParticle: Particle? = null
            for (otherParticle in particleList) {
                if (otherParticle == particle) continue
                val distance = particle.getDistanceSqTo(otherParticle)
                if (distance > LINE_RADIUS_SQ) {
                    continue
                }
                if (distance >= nearestDistance) {
                    continue
                }
                if (distanceSq(mouseX, mouseY, otherParticle.x, otherParticle.y) > LINE_RADIUS_SQ) {
                    continue
                }
                nearestDistance = distance
                nearestParticle = otherParticle
            }
            if (nearestParticle == null || !LINE) {
                continue
            }
            val alpha = MathHelper.clamp(1.0f - MathHelper.sqrt(nearestDistance) / LINE_RADIUS, 0.0f, 1.0f)
            bufferBuilder.pos(particle.x.toDouble(), particle.y.toDouble(), 0.0).color(1.0f, 1.0f, 1.0f, alpha)
                .endVertex()
            bufferBuilder.pos(nearestParticle.x.toDouble(), nearestParticle.y.toDouble(), 0.0)
                .color(1.0f, 1.0f, 1.0f, alpha).endVertex()
        }
        tessellator.draw()
        releaseGL()
    }

    private fun prepareGL() {
        GlStateManager.disableTexture2D()
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glEnable(GL11.GL_POINT_SMOOTH)
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_NICEST)
        GlStateManager.glLineWidth(0.5f)
    }

    private fun releaseGL() {
        GlStateManager.enableTexture2D()
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_DONT_CARE)
        GL11.glDisable(GL11.GL_POINT_SMOOTH)
        GL11.glHint(GL11.GL_POINT_SMOOTH_HINT, GL11.GL_DONT_CARE)
        GL11.glPointSize(1.0f)
        GlStateManager.glLineWidth(1.0f)
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    }

    companion object {
        private const val SPEED = 0.1f
        private const val LINE_RADIUS = 100.0f
        private const val LINE_RADIUS_SQ = 10000.0f
        fun distanceSq(x1: Float, y1: Float, x2: Float, y2: Float): Float {
            val xDiff = x1 - x2
            val yDiff = y1 - y2
            return xDiff * xDiff + yDiff * yDiff
        }
    }

    init {
        for (i in 0 until initAmount) {
            particleList.add(generateParticle())
        }
    }
}