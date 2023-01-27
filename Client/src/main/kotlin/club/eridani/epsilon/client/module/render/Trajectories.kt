package club.eridani.epsilon.client.module.render

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.threads.runSafe
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.Entity
import net.minecraft.init.Items
import net.minecraft.item.*
import net.minecraft.util.math.AxisAlignedBB
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.RayTraceResult
import net.minecraft.util.math.Vec3d
import org.lwjgl.opengl.GL11
import kotlin.math.sqrt


object Trajectories :
    Module(name = "Trajectories", category = Category.Render, description = "Predicts the flight path of arrows and throwable items.") {

    private val colorMode by setting("Color Mode", ColorMode.HitColor)
    private val color by setting("Color", ColorRGB(255, 0, 0, 255))

    private var landingOnEntity: Entity? = null

    init {
        onRender3D {
            runSafe {
                val renderPosX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * it.partialTicks
                val renderPosY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * it.partialTicks
                val renderPosZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * it.partialTicks
                if (isThrowable(mc.player.heldItemMainhand)) {
                    GlStateManager.pushMatrix()
                    val item = mc.player.heldItemMainhand.item
                    var posX = renderPosX - MathHelper.cos(mc.player.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16
                    var posY = renderPosY + mc.player.getEyeHeight() - 0.1000000014901161
                    var posZ = renderPosZ - MathHelper.sin(mc.player.rotationYaw / 180.0f * Math.PI.toFloat()) * 0.16
                    var motionX = -MathHelper.sin(mc.player.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(mc.player.rotationPitch / 180.0f * Math.PI.toFloat()) * if (item is ItemBow) 1.0 else 0.4
                    var motionY = -MathHelper.sin(mc.player.rotationPitch / 180.0f * Math.PI.toFloat()) * if (item is ItemBow) 1.0 else 0.4
                    var motionZ = MathHelper.cos(mc.player.rotationYaw / 180.0f * Math.PI.toFloat()) * MathHelper.cos(mc.player.rotationPitch / 180.0f * Math.PI.toFloat()) * if (item is ItemBow) 1.0 else 0.4
                    var power = (72000 - mc.player.itemInUseCount).toDouble() / 20.0
                    power = (power * power + power * 2.0) / 3.0
                    if (power > 1.0) {
                        power = 1.0
                    }
                    val distance = sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ)
                    motionX /= distance
                    motionY /= distance
                    motionZ /= distance

                    val pow = when (item) {
                        is ItemBow -> power * 2.0
                        is ItemFishingRod -> 1.25
                        is ItemExpBottle -> 0.9
                        is ItemPotion -> 0.5
                        else -> 1.0
                    }

                    val gravity = when (item) {
                        is ItemPotion -> 0.05
                        is ItemExpBottle -> 0.07
                        is ItemBow -> 0.05
                        is ItemFishingRod -> 0.15
                        else -> 0.03
                    }

                    motionX *= (pow * if (item is ItemFishingRod) 0.75 else if (mc.player.heldItemMainhand.item == Items.EXPERIENCE_BOTTLE) 0.75 else 1.5)
                    motionY *= (pow * if (item is ItemFishingRod) 0.75 else if (mc.player.heldItemMainhand.item == Items.EXPERIENCE_BOTTLE) 0.75 else 1.5)
                    motionZ *= (pow * if (item is ItemFishingRod) 0.75 else if (mc.player.heldItemMainhand.item == Items.EXPERIENCE_BOTTLE) 0.75 else 1.5)
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.glPre(1.5f)
                    GL11.glEnable(2848)
                    val size = (if (item is ItemBow) 0.3 else 0.25).toFloat()
                    var hasLanded = false
                    var landingPosition: RayTraceResult? = null
                    when (colorMode) {
                        ColorMode.HitColor -> if (landingOnEntity != null) GlStateManager.color(0f, 1f, 0f, color.aFloat)
                        else GlStateManager.color(1f, 0f, 0f, color.aFloat)
                        ColorMode.Custom -> GlStateManager.color(color.rFloat, color.gFloat, color.bFloat, color.aFloat)
                        ColorMode.Gui -> GlStateManager.color(GUIManager.firstColor.rFloat, GUIManager.firstColor.gFloat, GUIManager.firstColor.bFloat, color.aFloat)
                    }
                    GL11.glBegin(GL11.GL_LINE_STRIP)
                    while (!hasLanded && posY > 0.0) {
                        val present = Vec3d(posX, posY, posZ)
                        val future = Vec3d(posX + motionX, posY + motionY, posZ + motionZ)
                        val possibleLandingStrip = mc.world.rayTraceBlocks(present, future, false, true, false)
                        if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != RayTraceResult.Type.MISS) {
                            landingPosition = possibleLandingStrip
                            hasLanded = true
                        }
                        val arrowBox = AxisAlignedBB(posX - size, posY - size, posZ - size, posX + size, posY + size, posZ + size).offset(motionX, motionY, motionZ).expand(1.0, 1.0, 1.0)
                        val entities = getEntitiesWithinAABB(arrowBox)
                        if (entities.isNotEmpty()) {
                            for (entity in entities) {
                                if (entity.canBeCollidedWith() && entity != mc.player) {
                                    val boundingBox = entity.entityBoundingBox.expand(0.30000001192092896, 0.30000001192092896, 0.30000001192092896)
                                    val possibleEntityLanding = boundingBox.calculateIntercept(present, future) ?: continue
                                    hasLanded = true
                                    landingOnEntity = entity
                                    landingPosition = possibleEntityLanding
                                }
                            }
                        } else {
                            landingOnEntity = null
                        }

                        posX += motionX
                        posY += motionY
                        posZ += motionZ
                        val motionAdjustment = 0.9900000095367432

                        motionX *= motionAdjustment
                        motionY *= motionAdjustment
                        motionZ *= motionAdjustment

                        motionY -= gravity
                        GL11.glVertex3d(posX - renderPosX, posY - renderPosY, posZ - renderPosZ)
                    }
                    GL11.glEnd()

                    if (landingPosition != null && landingPosition.typeOfHit == RayTraceResult.Type.BLOCK) {
                        GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ)
                        when (landingPosition.sideHit.index) {
                            1 -> {
                                GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f)
                            }
                            2 -> {
                                GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f)
                            }
                            3 -> {
                                GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f)
                            }
                            4 -> {
                                GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f)
                            }
                            5 -> {
                                GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f)
                            }
                        }
                        GlStateManager.rotate(-90.0f, 1.0f, 0.0f, 0.0f)
                        GlStateManager.scale(0.05f, 0.05f, 0.05f)
                        val color =
                            when (colorMode) {
                                ColorMode.HitColor -> if (landingOnEntity != null) ColorRGB(0, 255, 0) else ColorRGB(255, 0, 0)
                                ColorMode.Gui -> GUIManager.firstColor
                                ColorMode.Custom -> color
                            }
                        club.eridani.epsilon.client.util.graphics.RenderUtils2D.drawBorderedRect(-8.25f, -8.25f, 8.25f, 8.25f, 1f, color.alpha(255), color.alpha(69))
                    }
                    club.eridani.epsilon.client.util.graphics.RenderUtils3D.glPost()
                    GlStateManager.popMatrix()
                }
            }
        }
    }

    private fun isThrowable(stack: ItemStack): Boolean {
        val item = stack.item
        return item is ItemBow || item is ItemSnowball || item is ItemEgg || item is ItemEnderPearl || item is ItemSplashPotion || item is ItemLingeringPotion || item is ItemFishingRod || item is ItemExpBottle
    }

    private fun getEntitiesWithinAABB(bb: AxisAlignedBB): List<Entity> {
        val list = mutableListOf<Entity>()
        val chunkMinX = MathHelper.floor((bb.minX - 2.0) / 16.0)
        val chunkMaxX = MathHelper.floor((bb.maxX + 2.0) / 16.0)
        val chunkMinZ = MathHelper.floor((bb.minZ - 2.0) / 16.0)
        val chunkMaxZ = MathHelper.floor((bb.maxZ + 2.0) / 16.0)
        for (x in chunkMinX..chunkMaxX) {
            for (z in chunkMinZ..chunkMaxZ) {
                if (mc.world.chunkProvider.getLoadedChunk(x, z) != null) {
                    mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(mc.player, bb, list, null)
                }
            }
        }
        return list
    }

    enum class ColorMode {
        HitColor, Gui, Custom
    }
}