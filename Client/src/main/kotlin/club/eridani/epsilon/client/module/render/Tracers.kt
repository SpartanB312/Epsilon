package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.misc.AntiBot.isBot
import club.eridani.epsilon.client.util.EntityUtil
import club.eridani.epsilon.client.util.onRender3D
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11

object Tracers :
    Module(name = "Tracers", category = Category.Render, description = "Traces a line to the players.") {

    val range by setting("Range", 200, 0..300, 1)
    private val players by setting("Player", true)
    private val animals by setting("Animals", false)
    private val mobs by setting("Mobs", false)
    private val hurtTime by setting("HurtTime", false)
    val height by setting("Height", 0.5f, 0f..1f, 0.1f)
    val width by setting("Width", 2f, 0.1f..5f, 0.1f)
    val alpha by setting("Alpha", 255, 0..255, 1)

    init {
        onRender3D {
            for (e in mc.world.loadedEntityList) {
                if (e is EntityLivingBase) {
                    if (e == mc.player || e.isDead) {
                        continue
                    }
                    if (mc.player.getDistance(e) > range) continue
                    render(e)
                }
            }
        }
    }

    private fun render(entity: EntityLivingBase) {
        val alpha = alpha.toFloat() / 255f
        if (entity.hurtTime > 0 && hurtTime) {
            drawTracer(entity, 1.0f, 0.0f, 0.0f, alpha, width, height)
            return
        }
        if (entity is EntityPlayer && players && !isBot(entity)) {
            if (entity.isInvisible) {
                drawTracer(entity, 0.0f, 0.0f, 0.0f, alpha, width, height)
                return
            }
//            else if (FriendManager.INSTANCE.isFriend(player.name)) {
//                val hue = floatArrayOf((System.currentTimeMillis() % 11520L).toFloat() / 11520.0f)
//                val rgb = Color.HSBtoRGB(hue[0], 1.0f, 1.0f)
//                val colorRed = rgb shr 16 and 255
//                val colorGreen = rgb shr 8 and 255
//                val colorBlue = rgb and 255
//                EpsilonRenderer.drawTracer(entity, colorRed / 255f, colorGreen / 255f, colorBlue / 255f, alpha, width.getValue(), height.getValue())
//                return
//            }
            else {
                var dist = (mc.player.getDistance(entity) * 2).toInt()
                if (dist > 255) {
                    dist = 255
                }
                drawTracer(entity, (255 - dist) / 255f, dist / 255f, 0f, alpha, width, height)
            }
        }
        if (animals && EntityUtil.isPassive(entity) || mobs && !EntityUtil.isPassive(entity) && entity !is EntityPlayer) {
            drawTracer(entity, 1f, 1f, 1f, alpha, width, height)
        }
    }

    private fun drawTracer(entity: Entity, red: Float, green: Float, blue: Float, alpha: Float, width: Float, height: Float) {
        val renderPosX = mc.renderManager.viewerPosX
        val renderPosY = mc.renderManager.viewerPosY
        val renderPosZ = mc.renderManager.viewerPosZ
        val xPos = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) - renderPosX
        val yPos = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) + entity.height * height - renderPosY
        val zPos = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) - renderPosZ
        drawLine(xPos, yPos, zPos, red, green, blue, alpha, width)
    }

    fun drawLine(x: Double, y: Double, z: Double, red: Float, green: Float, blue: Float, alpha: Float, width: Float) {
        val viewEntity: Entity = if (mc.renderViewEntity == null) mc.player else mc.renderViewEntity!!
        val eyes = Vec3d(0.0, 0.0, 1.0).rotatePitch((-Math.toRadians(viewEntity.rotationPitch.toDouble())).toFloat()).rotateYaw((-Math.toRadians(viewEntity.rotationYaw.toDouble())).toFloat())
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        GL11.glEnable(GL11.GL_BLEND)
        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST)
        GL11.glLineWidth(1.5f)
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(false)
        GL11.glLineWidth(width)
        GL11.glColor4f(red, green, blue, alpha)
        GL11.glBegin(GL11.GL_LINES)
        GL11.glVertex3d(eyes.x, mc.player.getEyeHeight() + eyes.y, eyes.z)
        GL11.glVertex3d(x, y, z)
        GL11.glEnd()
        GL11.glEnable(GL11.GL_TEXTURE_2D)
        GL11.glEnable(GL11.GL_DEPTH_TEST)
        GL11.glDepthMask(true)
        GL11.glDisable(GL11.GL_LINE_SMOOTH)
        GlStateManager.resetColor()
    }
}