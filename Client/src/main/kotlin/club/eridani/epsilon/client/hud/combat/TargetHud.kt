package club.eridani.epsilon.client.hud.combat

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.TextureManager
import club.eridani.epsilon.client.management.TotemPopManager
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ColorUtils
import club.eridani.epsilon.client.util.Utils
import club.eridani.epsilon.client.util.graphics.AnimationUtil
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.VertexHelper
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.relativeHealth
import net.minecraft.client.entity.AbstractClientPlayer
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.entity.EntityLivingBase
import net.minecraft.entity.player.EntityPlayer
import org.lwjgl.opengl.GL11

object TargetHud : HUDModule(name = "TargetHud", category = Category.CombatHUD, description = "Display target") {
    private val displaySelf by setting("DisplaySelf", true)
    private val range by setting("range", 7f, 0f..20f, 0.1f)
    private val shadow by setting("Shadow", true)

    private var animationWidth = 0f

    init {
        resize {
            width = 151
            height = 54
        }
    }

    override fun onRender() {
        if (Utils.nullCheck()) return
        val target: EntityLivingBase? = if (mc.player.lastAttackedEntity != null && mc.player.lastAttackedEntity is EntityPlayer) {
            if (mc.player.getDistance(mc.player.lastAttackedEntity) > range) {
                if (displaySelf) mc.player else null
            } else {
                mc.player.lastAttackedEntity
            }
        } else {
            if (displaySelf) mc.player else null
        }

        if (target != null && target is AbstractClientPlayer) {
            RenderUtils2D.drawRectFilled(x, y, x + 150, y + 53, ColorRGB(10, 10, 10, 128))
            if (shadow) TextureManager.renderShadowRect(x, y, 150, 53, 10)
            TextureManager.renderPlayer2d(target, x + 5, y + 5, 32, 32)
            MainFontRenderer.drawString("Name " + target.name, x + 40f, y + 4f, ColorRGB(255, 255, 255, 200), 0.85f)
            val pops = "Pops " + TotemPopManager.getTotemPops(target)
            val distance = "Distance " + String.format("%.1f", mc.player.getDistance(target))
//            val kills = "Kills 10"
            MainFontRenderer.drawString(pops, x + 40f, y + 5f + MainFontRenderer.getHeight(0.85f), ColorRGB(255, 255, 255, 200), 0.85f)
            MainFontRenderer.drawString(distance, x + 52f + MainFontRenderer.getWidth(pops, 0.85f), y + 5f + MainFontRenderer.getHeight(0.85f), ColorRGB(255, 255, 255, 200), 0.85f)
            drawArmor(target, x + 26, y + 23)

            val startX = x + 5
            val startY = y + 44.0
            val targetWidth = 129f * (target.health / target.maxHealth)
            animationWidth = AnimationUtil.animate(targetWidth, animationWidth, 0.2f)
            val step = 129.0 / 6.0

            GlStateManager.enableDepth()
            GlStateManager.depthMask(true)
            GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT)
            GlStateManager.depthFunc(GL11.GL_ALWAYS)

            RenderUtils2D.drawRectFilled(startX.toDouble(), startY - 4.0, startX + animationWidth.toDouble(), startY + 4.0, ColorRGB(0, 0, 0, 26))

            GlStateManager.depthFunc(GL11.GL_EQUAL)
            RenderUtils2D.prepareGl()
            GL11.glLineWidth(16f)
            VertexHelper.begin(GL11.GL_LINES)
            val delay = -600
            VertexHelper.put(startX + step * 0, startY, ColorUtils.rainbowRGB(delay * 0, 0.6f, 1f))
            VertexHelper.put(startX + step * 1, startY, ColorUtils.rainbowRGB(delay * 1, 0.6f, 1f))

            VertexHelper.put(startX + step * 1, startY, ColorUtils.rainbowRGB(delay * 1, 0.6f, 1f))
            VertexHelper.put(startX + step * 2, startY, ColorUtils.rainbowRGB(delay * 2, 0.6f, 1f))

            VertexHelper.put(startX + step * 2, startY, ColorUtils.rainbowRGB(delay * 2, 0.6f, 1f))
            VertexHelper.put(startX + step * 3, startY, ColorUtils.rainbowRGB(delay * 3, 0.6f, 1f))

            VertexHelper.put(startX + step * 3, startY, ColorUtils.rainbowRGB(delay * 3, 0.6f, 1f))
            VertexHelper.put(startX + step * 4, startY, ColorUtils.rainbowRGB(delay * 4, 0.6f, 1f))

            VertexHelper.put(startX + step * 4, startY, ColorUtils.rainbowRGB(delay * 4, 0.6f, 1f))
            VertexHelper.put(startX + step * 5, startY, ColorUtils.rainbowRGB(delay * 5, 0.6f, 1f))

            VertexHelper.put(startX + step * 5, startY, ColorUtils.rainbowRGB(delay * 5, 0.6f, 1f))
            VertexHelper.put(startX + step * 6, startY, ColorUtils.rainbowRGB(delay * 6, 0.6f, 1f))
            VertexHelper.end()
            RenderUtils2D.releaseGl()
            GlStateManager.depthFunc(GL11.GL_LEQUAL)

            MainFontRenderer.drawString(String.format("%.1f", target.relativeHealth), x + animationWidth + 6, y + 39.5f, ColorRGB(255, 255, 255, 200), 0.82f)
        } else {
            animationWidth = 0f
        }
    }


    private fun drawArmor(player: EntityPlayer, x: Int, y: Int) {
        GlStateManager.enableTexture2D()
        var iteration = 0
        for (itemStack in player.inventory.armorInventory) {
            iteration++
            if (itemStack.isEmpty) continue
            val x2 = x - 90 + (9 - iteration) * 20 + 2
            GlStateManager.enableDepth()
            mc.renderItem.zLevel = 200f
            mc.renderItem.renderItemAndEffectIntoGUI(itemStack, x2, y)
            mc.renderItem.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, x2, y, "")
            mc.renderItem.zLevel = 0f
            GlStateManager.enableTexture2D()
            GlStateManager.disableLighting()
            GlStateManager.disableDepth()
        }
        GlStateManager.enableDepth()
        GlStateManager.disableLighting()
    }
}