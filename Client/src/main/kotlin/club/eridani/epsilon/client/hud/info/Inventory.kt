package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.util.Utils
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.item.ItemStack
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import org.lwjgl.opengl.GL11

object Inventory : HUDModule(name = "Inventory", category = Category.InfoHUD, description = "Display what your inventory having") {

    init {
        resize {
            width = 162
            height = 54
        }
    }

    override fun onRender() {
        preBoxRender()
        mc.renderEngine.bindTexture(ResourceLocation("textures/gui/container/generic_54.png"))
        mc.ingameGUI.drawTexturedModalRect(x, y, 7, 17, 162, 54)
        postBoxRender()
        if (Utils.nullCheck()) return
        val items = mc.player.inventory.mainInventory
        inventoryItemRender(items, x, y)
    }


    private fun inventoryItemRender(items: NonNullList<*>, x: Int, y: Int) {
        val size = items.size
        for (item in 9 until size) {
            val slotX = x + 1 + item % 9 * 18
            val slotY = y + 1 + (item / 9 - 1) * 18
            preItemRender()
            mc.renderItem.renderItemAndEffectIntoGUI(items[item] as ItemStack, slotX, slotY)
            mc.renderItem.renderItemOverlays(mc.fontRenderer, items[item] as ItemStack, slotX, slotY)
            postItemRender()
        }
    }

    private fun preBoxRender() {
        GL11.glPushMatrix()
        GlStateManager.pushMatrix()
        GlStateManager.disableAlpha()
        GlStateManager.clear(256)
        GlStateManager.enableBlend()
    }

    private fun postBoxRender() {
        GlStateManager.disableBlend()
        GlStateManager.disableDepth()
        GlStateManager.disableLighting()
        GlStateManager.enableDepth()
        GlStateManager.enableAlpha()
        GlStateManager.popMatrix()
        GL11.glPopMatrix()
    }

    private fun preItemRender() {
        GL11.glPushMatrix()
        GL11.glDepthMask(true)
        GlStateManager.clear(256)
        GlStateManager.disableDepth()
        GlStateManager.enableDepth()
        RenderHelper.enableStandardItemLighting()
        GlStateManager.scale(1.0f, 1.0f, 0.01f)
    }

    private fun postItemRender() {
        GlStateManager.scale(1.0f, 1.0f, 1.0f)
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableAlpha()
        GlStateManager.disableBlend()
        GlStateManager.disableLighting()
        GlStateManager.scale(0.5, 0.5, 0.5)
        GlStateManager.disableDepth()
        GlStateManager.enableDepth()
        GlStateManager.scale(2.0f, 2.0f, 2.0f)
        GL11.glPopMatrix()
    }
}