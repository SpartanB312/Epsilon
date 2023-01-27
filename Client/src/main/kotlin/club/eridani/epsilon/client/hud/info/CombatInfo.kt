package club.eridani.epsilon.client.hud.info

import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.hud.HUDModule
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.management.TextureManager
import club.eridani.epsilon.client.module.combat.*
import club.eridani.epsilon.client.module.combat.AutoTrap
import club.eridani.epsilon.client.module.combat.Surround
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.ItemUtil
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.render.AsyncRenderer
import club.eridani.epsilon.client.util.graphics.render.asyncRender
import club.eridani.epsilon.client.util.math.Vec2d
import net.minecraft.init.Blocks
import net.minecraft.init.Items
import net.minecraft.item.Item
import kotlin.math.max

object CombatInfo : HUDModule(name = "CombatInfo", category = Category.InfoHUD, description = "Combat information") {

    private val shadow by setting("Shadow", true)

    //Module check
    private val moduleCheck by setting("ModuleCheck", true)
    private val checkAutoCrystal by setting("AutoCrystal", true) { moduleCheck }
    private val checkSurround by setting("Surround", true) { moduleCheck }
    private val checkAutoTrap by setting("AutoTrap", false) { moduleCheck }
    private val checkHeadCrystal by setting("HeadCrystal", false) { moduleCheck }

    //Items
    private val items by setting("Items", true)
    private val goldenApple by setting("GoldenApple", true) { items }
    private val enderCrystal by setting("EnderCrystal", true) { items }
    private val obsidian by setting("Obsidian", true) { items }
    private val undyingTotem by setting("UndyingTotem", true) { items }
    private val expBottle by setting("ExpBottle", true) { items }

    private var startY = y.toFloat()
    private var maxWidth = 0f

    private val asyncRenderer = asyncRender {
        startY = y.toFloat()
        startY += club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight() + 2

        val cacheY = startY

        draw {
            RenderUtils2D.drawRectFilled(x, y, x + width, y + height, ColorRGB(0, 0, 0, 128))
            RenderUtils2D.drawRectFilled(x, y - 2, x + width, y, GUIManager.firstColor)
            if (shadow) TextureManager.renderShadowRect(x, y - 2, width, height + 2, 10)
            RenderUtils2D.drawLine(Vec2d(x + 2.0, cacheY + 1.0), Vec2d(x + width - 2.0, cacheY + 1.0), 1f, GUIManager.white.alpha(200))
        }

        drawStringWithShadow("Combat Info", x + 4f, y + 1f, GUIManager.white)

        startY += 2

        if (moduleCheck) {
            if (checkAutoCrystal) drawModuleCheck("AutoCrystal: ") { ZealotCrystalTwo.isEnabled || ZealotCrystalPlus.isEnabled }
            if (checkSurround) drawModuleCheck("Surround: ") { Surround.isEnabled }
            if (checkAutoTrap) drawModuleCheck("Trap: ") { AutoTrap.isEnabled }
            if (checkHeadCrystal) drawModuleCheck("AutoCEV: ") { AutoCev.isEnabled }
        }

        if (items) {
            if (goldenApple) drawItems("GApple: ", Items.GOLDEN_APPLE)
            if (enderCrystal) drawItems("Crystal: ", Items.END_CRYSTAL)
            if (obsidian) drawItems("Obsidian: ", Item.getItemFromBlock(Blocks.OBSIDIAN))
            if (undyingTotem) drawItems("Totem: ", Items.TOTEM_OF_UNDYING, 1)
            if (expBottle) drawItems("EXP: ", Items.EXPERIENCE_BOTTLE)
        }

        resize {
            width = max(maxWidth.toInt() + 8, 10)
            height = max(startY.toInt() + 2, 10) - y
        }

    }

    private inline fun AsyncRenderer.drawModuleCheck(name: String, predicate: () -> Boolean) {
        drawStringWithShadow(("$name ${club.eridani.epsilon.client.util.text.ChatUtil.SECTION_SIGN}" + if (predicate.invoke()) "aTrue" else "cFalse").also {
            maxWidth = max(club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getWidth(it), maxWidth)
        }, x.toFloat() + 4, startY, GUIManager.white, 0.95f)
        startY += club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight(0.95f)
    }

    private fun AsyncRenderer.drawItems(name: String, item: Item, stackSize: Int = 64) {
        val count = ItemUtil.getItemCount(item)
        drawStringWithShadow(("$name ${club.eridani.epsilon.client.util.text.ChatUtil.SECTION_SIGN}" + when (count) {
            in 0..(1 * stackSize) -> "c"
            in (1 * stackSize)..(3 * stackSize) -> "e"
            else -> "a"
        } + count).also {
            maxWidth = max(club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getWidth(it), maxWidth)
        }, x + 4f, startY, GUIManager.white)
        startY += club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer.getHeight()
    }

    override fun onRender() {
        asyncRenderer.render()
    }

}