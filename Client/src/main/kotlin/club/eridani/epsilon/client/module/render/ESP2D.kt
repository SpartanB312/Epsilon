package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.setupCameraTransform
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.ColorUtils
import club.eridani.epsilon.client.util.onRender2D
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.client.renderer.GLAllocation
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.MobEffects
import net.minecraft.util.math.AxisAlignedBB
import org.lwjgl.opengl.Display
import org.lwjgl.opengl.GL11
import org.lwjgl.util.glu.GLU
import javax.vecmath.Vector3d
import javax.vecmath.Vector4d
import kotlin.math.pow

object ESP2D : Module(
    name = "ESP2D",
    alias = arrayOf("2DESP", "ESP"),
    category = Category.Render,
    description = "Render ESP with 2d renderer"
) {

    private val width by setting("Width", .5, 0.0..2.0, .1)
    private val mode by setting("Mode", Mode.Box)
    private val fill by setting("Fill", true)
    private val fillMode by setting("FillMode", FillMode.Full) { fill }
    private val alpha by setting("FillAlpha", 100, 0..255, 1) { fill }
    private val healthBar by setting("HealthBar", true)
    private val split by setting("SplitHealth", true) { healthBar }
    private var healthNumber = setting("Health", false)
    private var potion by setting("Potion", false)
    var displayName = setting("Name", false)
    private val self by setting("Self", false)
    val color by setting("Color", ColorRGB(255, 255, 255, 255))

    private val viewport = GLAllocation.createDirectIntBuffer(16)
    private val modelview = GLAllocation.createDirectFloatBuffer(16)
    private val projection = GLAllocation.createDirectFloatBuffer(16)
    private val vector = GLAllocation.createDirectFloatBuffer(4)
    private val black = ColorRGB(0, 0, 0, 150)
    val camera: ICamera = Frustum()

    init {
        onRender2D { event ->
            runSafe {
                camera.setPosition(mc.renderViewEntity!!.posX, mc.renderViewEntity!!.posY, mc.renderViewEntity!!.posZ)
                GL11.glPushMatrix()
                val boxWidth: Double = width
                val scaling: Double = event.resolution.scaleFactor / event.resolution.scaleFactor.toDouble().pow(2.0)
                GlStateManager.scale(scaling, scaling, scaling)
                for (entity in mc.world.playerEntities) {
                    if (!camera.isBoundingBoxInFrustum(entity.renderBoundingBox)) {
                        continue
                    }
                    if (isValid(entity)) {
                        val x = MathUtils.getInterpolatedPos(entity, mc.renderPartialTicks).x
                        val y = MathUtils.getInterpolatedPos(entity, mc.renderPartialTicks).y
                        val z = MathUtils.getInterpolatedPos(entity, mc.renderPartialTicks).z
                        val width = entity.width / 1.5
                        val height = entity.height + if (entity.isSneaking) -0.3 else 0.2
                        val aabb = AxisAlignedBB(x - width, y, z - width, x + width, y + height, z + width)
                        val vectors = listOf(
                            Vector3d(aabb.minX, aabb.minY, aabb.minZ),
                            Vector3d(aabb.minX, aabb.maxY, aabb.minZ),
                            Vector3d(aabb.maxX, aabb.minY, aabb.minZ),
                            Vector3d(aabb.maxX, aabb.maxY, aabb.minZ),
                            Vector3d(aabb.minX, aabb.minY, aabb.maxZ),
                            Vector3d(aabb.minX, aabb.maxY, aabb.maxZ),
                            Vector3d(aabb.maxX, aabb.minY, aabb.maxZ),
                            Vector3d(aabb.maxX, aabb.maxY, aabb.maxZ)
                        )
                        mc.entityRenderer.setupCameraTransform(mc.renderPartialTicks, 0)

                        var position: Vector4d? = null
                        for (vector in vectors) {
                            val newVector = project2D(
                                event.resolution,
                                vector.x - mc.renderManager.viewerPosX,
                                vector.y - mc.renderManager.viewerPosY,
                                vector.z - mc.renderManager.viewerPosZ
                            ) ?: return@runSafe
                            if (newVector.z >= 0.0 && newVector.z < 1.0) {
                                if (position == null) {
                                    position = Vector4d(newVector.x, newVector.y, newVector.z, 0.0)
                                }
                                position.x = newVector.x.coerceAtMost(position.x)
                                position.y = newVector.y.coerceAtMost(position.y)
                                position.z = newVector.x.coerceAtLeast(position.z)
                                position.w = newVector.y.coerceAtLeast(position.w)
                            }
                        }

                        mc.entityRenderer.setupOverlayRendering()

                        if (position != null) {
                            val posX = position.x
                            val posY = position.y
                            val endPosX = position.z
                            val endPosY = position.w
                            val color2 = ColorRGB(color.r, color.g, color.b)

                            if (fill) {
                                if (fillMode == FillMode.Gradient)
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawGradientRect((posX + width).toFloat(), (posY + width).toFloat(), endPosX.toFloat(), endPosY.toFloat(),
                                    color.alpha(0),
                                    color.alpha(0),
                                    color.alpha(alpha),
                                    color.alpha(alpha))
                                else club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(posX + width, posY + width, endPosX, endPosY, color.alpha(
                                    alpha
                                ))
                            }

                            if (mode == Mode.Box) {
                                // Left
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(posX - 1, posY, posX + boxWidth, endPosY + .5, black)
                                // Top
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - 1,
                                    posY - .5,
                                    endPosX + .5,
                                    posY + .5 + boxWidth,
                                    black
                                )
                                // Right
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - .5 - boxWidth,
                                    posY,
                                    endPosX + .5,
                                    endPosY + .5,
                                    black
                                )
                                // Bottom
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - 1,
                                    endPosY - boxWidth - .5,
                                    endPosX + .5,
                                    endPosY + .5,
                                    black
                                )
                                // Left
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(posX - .5, posY, posX + boxWidth - .5, endPosY, color2)
                                // Bottom
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(posX, endPosY - boxWidth, endPosX, endPosY, color2)
                                // Top
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(posX - .5, posY, endPosX, posY + boxWidth, color2)
                                // Right
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(endPosX - boxWidth, posY, endPosX, endPosY, color2)
                            }
                            if (mode == Mode.Corners) {
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX + .5, posY, posX - 1, posY + (endPosY - posY) / 4 + .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - 1, endPosY, posX + .5, endPosY - (endPosY - posY) / 4 - .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - 1, posY - .5, posX + (endPosX - posX) / 3 + .5, posY + 1,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - (endPosX - posX) / 3 - .5, posY - .5, endPosX, posY + 1,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - 1, posY, endPosX + .5, posY + (endPosY - posY) / 4 + .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - 1, endPosY, endPosX + .5, endPosY - (endPosY - posY) / 4 - .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - 1, endPosY - 1, posX + (endPosX - posX) / 3 + .5, endPosY + .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - (endPosX - posX) / 3 - .5, endPosY - 1, endPosX + .5, endPosY + .5,
                                    black
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX, posY, posX - .5, posY + (endPosY - posY) / 4,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX, endPosY, posX - .5, endPosY - (endPosY - posY) / 4,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX - .5, posY, posX + (endPosX - posX) / 3, posY + .5,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - (endPosX - posX) / 3, posY, endPosX, posY + .5,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - .5, posY, endPosX, posY + (endPosY - posY) / 4,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - .5, endPosY, endPosX, endPosY - (endPosY - posY) / 4,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    posX, endPosY - .5, posX + (endPosX - posX) / 3, endPosY,
                                    color2
                                )
                                club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                    endPosX - (endPosX - posX) / 3, endPosY - .5, endPosX - .5, endPosY,
                                    color2
                                )
                            }
                            if (displayName.value) {
                                val message = if (displayName.value ) entity.name else ""
                                val dif = (endPosX - posX) / 2
                                club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.drawStringWithShadow(
                                    message,
                                    (posX + dif).toFloat() - club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
                                        .getWidth(message, 0.6f) / 2f,
                                    (posY - 9 / 1.5f * 2.0f + 2.0f).toFloat(),
                                    ColorRGB(255, 255, 255)
                                    ,0.6f
                                )
                            }
                            if (healthBar) {
                                var hpPercentage = (entity.health / entity.maxHealth).toDouble()
                                if (hpPercentage > 1) hpPercentage = 1.0 else if (hpPercentage < 0) hpPercentage = 0.0
                                val health = entity.health
                                val hpHeight = (endPosY - posY) * hpPercentage
                                val difference = posY - endPosY + 0.5
                                if (health > 0 && healthBar) {
                                    club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawOutline(posX - 4, posY - .5, 2.0, endPosY - posY + .5, .5, black)
                                    club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                        posX - 4,
                                        posY - .5,
                                        posX - 2,
                                        endPosY + .5,
                                        ColorRGB(0, 0, 0, 70)
                                    )
                                    club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                        posX - 3.5,
                                        endPosY - hpHeight,
                                        posX - 2.5,
                                        endPosY,
                                        club.eridani.epsilon.client.util.EntityUtil.getHealthColor(entity, 255)
                                    )
                                }
                                if (split)
                                if (-difference > 50.0)
                                    for (i in 1..9) {
                                    val increment = difference / 10.0 * i
                                    club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawRectFilled(
                                        posX - 3.5,
                                        endPosY - 0.5 + increment,
                                        posX - 2.5,
                                        endPosY - 0.5 + increment - 1.0,
                                        black
                                    )
                                }

                                if (healthNumber.value) {
                                    val healthNum = (hpPercentage * 100).toString()
                                    club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.drawStringWithShadow(healthNum, posX.toFloat() - (club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getWidth(healthNum, 0.53f)),
                                        (endPosY - hpHeight).toFloat() - club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight(0.53f),
                                        ColorRGB(255, 255, 255), 0.53f)
                                }

                                if (potion) {
                                    var startY = posY.toFloat()
                                    if (entity.isPotionActive(MobEffects.STRENGTH)) {
                                        club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.drawStringWithShadow("Strength " + (entity.getActivePotionEffect(MobEffects.STRENGTH)!!.amplifier + 1), endPosX.toFloat() + 1.5f, startY, ColorRGB(
                                            ColorUtils.argbToRgba(MobEffects.STRENGTH.liquidColor or -0x1000000)), 0.55f)
                                        startY += club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight(0.55f) + 1f
                                    }
                                    if (entity.isPotionActive(MobEffects.WEAKNESS)) {
                                        club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.drawStringWithShadow("Weakness " + (entity.getActivePotionEffect(MobEffects.WEAKNESS)!!.amplifier + 1), endPosX.toFloat() + 1.5f, startY, ColorRGB(
                                            ColorUtils.argbToRgba(MobEffects.WEAKNESS.liquidColor or -0x1000000)), 0.55f)
                                    }
                                }
                            }
                        }
                    }
                }

                GL11.glPopMatrix()
                GlStateManager.enableBlend()
                mc.entityRenderer.setupOverlayRendering()
            }
        }
    }


    private fun isValid(entityLivingBase: EntityPlayer): Boolean {
        return (self || entityLivingBase != mc.player) && !entityLivingBase.isDead && !entityLivingBase.isInvisible
    }

    private fun project2D(scaledResolution: ScaledResolution, x: Double, y: Double, z: Double): Vector3d? {
        GL11.glGetFloat(2982, modelview)
        GL11.glGetFloat(2983, projection)
        GL11.glGetInteger(2978, viewport)
        return if (GLU.gluProject(x.toFloat(), y.toFloat(), z.toFloat(), modelview, projection, viewport, vector)) {
            Vector3d(
                (vector[0] / scaledResolution.scaleFactor).toDouble(),
                ((Display.getHeight() - vector[1]) / scaledResolution.scaleFactor).toDouble(),
                vector[2].toDouble()
            )
        } else null
    }


    enum class Mode {
        Box,
        Corners
    }

    enum class FillMode {
        Gradient,
        Full
    }
}