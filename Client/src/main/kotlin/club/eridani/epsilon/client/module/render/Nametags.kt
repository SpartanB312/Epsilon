package club.eridani.epsilon.client.module.render

import baritone.api.utils.Helper
import club.eridani.epsilon.client.common.Category
import club.eridani.epsilon.client.common.extensions.stackSize
import club.eridani.epsilon.client.event.events.Render3DEvent
import club.eridani.epsilon.client.management.FriendManager.isFriend
import club.eridani.epsilon.client.management.GUIManager
import club.eridani.epsilon.client.module.Module
import club.eridani.epsilon.client.module.misc.AntiBot.isBot
import club.eridani.epsilon.client.util.ColorRGB
import club.eridani.epsilon.client.util.EntityUtil.getHealthColor
import club.eridani.epsilon.client.util.graphics.RenderUtils2D
import club.eridani.epsilon.client.util.graphics.font.renderer.MainFontRenderer
import club.eridani.epsilon.client.util.math.MathUtils
import club.eridani.epsilon.client.util.onRender3D
import club.eridani.epsilon.client.util.onTick
import club.eridani.epsilon.client.util.relativeHealth
import club.eridani.epsilon.client.util.threads.runSafe
import com.mojang.realmsclient.gui.ChatFormatting
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderHelper
import net.minecraft.client.renderer.culling.Frustum
import net.minecraft.client.renderer.culling.ICamera
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.init.Items
import net.minecraft.item.ItemArmor
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemTool
import net.minecraft.util.NonNullList
import net.minecraft.util.math.MathHelper
import org.lwjgl.opengl.GL11
import java.util.*

object Nametags : Module(name = "Nametags", category = Category.Render, description = "") {

    private val customFont by setting("CustomFont", false)
    private val shadow by setting("Shadow", true)
    private val gameMode by setting("Gamemode", true)
    private val ping by setting("Ping", true)
    private val entityID by setting("EntityID", false)
    private val bot by setting("Bot", true)
    private val heldStackName by setting("HeldStackName", false)
    private val health by setting("Health", true)
    private val mode by setting("Mode", Mode.Default)
    private val healthBar by setting("HealthBar", true) { mode == Mode.Default }
    private val widths by setting("Width", 1.5f, 0.1f..5f, 0.1f) { mode == Mode.DotGod }
    private val alpha by setting("Alpha", 110, 0..255, 1)
    private val info by setting("Info", Info.Durability)
    private val enchant by setting("Enchant", true)
    private val xAdd by setting("XAdd", 0, 0..30, 1)
    private val yAdd by setting("YAdd", 0, 0..30, 1)
    private val nameTagScale by setting("Scale", 2f, 0.1f..10f, 0.1f)
    private val count by setting("Count", false)
    private val playerCount by setting("PlayerCount", 15, 0..50, 1) { count }
    private val range by setting("Range", 200, 0..500, 1)
    private var nameTags = mutableListOf<NameTag>()
    private val camera: ICamera = Frustum()

    enum class Mode {
        Default, DotGod
    }

    enum class Info {
        Durability, Name, None
    }


    init {
        onRender3D { event ->
            runSafe {
                if (nameTags.isNotEmpty()) {
                    for (nameTag in nameTags) {
                        nameTag.draw(event)
                    }
                }
            }
        }

        onTick {
            nameTags = mutableListOf()

            val players = mutableListOf<EntityPlayer>()

            camera.setPosition(mc.renderViewEntity!!.posX, mc.renderViewEntity!!.posY, mc.renderViewEntity!!.posZ)

            mc.world.playerEntities.filter { entity ->
                mc.player.getDistance(entity) < range
                        && !entity.isDead
                        && entity != mc.renderViewEntity }.forEach { e ->
                if (camera.isBoundingBoxInFrustum(e.entityBoundingBox)) {
                    players.add(e)
                }
            }

            players.sortBy { player: EntityPlayer -> mc.player.getDistance(player) }

            var count = 0
            for (player in players) {

                if (!bot && isBot(player)) continue
                if (Nametags.count && count >= playerCount) continue

                nameTags.add(NameTag(player, NameTagInfo(getDisplayTag(player), getTagStringColor(player)), arrayOf(player.heldItemMainhand.copy(), player.heldItemOffhand.copy()), player.inventory.armorInventory))
                count++
            }
        }
    }

    private fun renderItemStack(stack: ItemStack, x: Int, y: Int) {
        GlStateManager.pushMatrix()
        GlStateManager.depthMask(true)
        GlStateManager.clear(256)
        RenderHelper.enableStandardItemLighting()
        mc.renderItem.zLevel = -150.0f
        GlStateManager.disableAlpha()
        GlStateManager.enableDepth()
        GlStateManager.disableCull()
        Helper.mc.renderItem.renderItemAndEffectIntoGUI(stack, x, y)
        Helper.mc.renderItem.renderItemOverlays(mc.fontRenderer, stack, x, y)
        mc.renderItem.zLevel = 0.0f
        RenderHelper.disableStandardItemLighting()
        GlStateManager.enableCull()
        GlStateManager.enableAlpha()
        GlStateManager.scale(0.5f, 0.5f, 0.5f)
        GlStateManager.disableDepth()
        renderEnchantmentText(stack, x, y)
        GlStateManager.enableDepth()
        GlStateManager.scale(2.0f, 2.0f, 2.0f)
        GlStateManager.popMatrix()
    }

    private fun renderEnchantmentText(stack: ItemStack, x: Int, y: Int) {
        val addY = 10
        var enchantmentY = (y - addY - yAdd).toFloat()
        if (enchant) {
            if (stack.item == Items.GOLDEN_APPLE && stack.hasEffect()) {
                drawFont("God", x * 2f, enchantmentY, -0x3cb2bf)
                enchantmentY -= addY
            }
            for ((key) in EnchantmentHelper.getEnchantments(stack)) {
                drawFont(getEnchantName(key, EnchantmentHelper.getEnchantmentLevel(key, stack)), x * 2f + xAdd, enchantmentY, -0x1)
                enchantmentY -= addY
            }
        }
        if (info == Info.Durability) {
            if (stack.item is ItemArmor || stack.item is ItemTool) {
                val green = (stack.maxDamage - stack.itemDamage.toFloat()) / stack.maxDamage
                val red = 1.0f - green
                val dmg = 100 - (red * 100.0f).toInt()
                drawFont("$dmg%", x * 2f + xAdd, enchantmentY, ColorRGB((red * 255.0f).toInt(), (green * 255.0f).toInt(), 0).toArgb())
            }
        } else {
            if (stack.item is ItemArmor) {
                drawFont(getOutlandShotNaming(stack.displayName), x * 2f + xAdd, enchantmentY, -0x1)
            }
        }
    }

    private fun getOutlandShotNaming(string: String): String {
        var out = string.replace("Kevlar", "Kev").replace("Soldier", "Sod").replace("Reinforced Wooden", "Wod").replace("Forest", "For").replace("Wooden", "Wod")
        val name = arrayOf("Helmet", "Chestplate", "Leggings", "Boots")
        for (s in name) {
            out = out.replace(s, "")
        }
        return out
    }

    private fun getEnchantName(enchantment: Enchantment, n: Int): String {
        val n2 = if (n > 1) 2 else 3
        if (enchantment.getTranslatedName(n).contains("Vanish")) {
            return ChatFormatting.RED.toString() + "Van"
        }
        if (enchantment.getTranslatedName(n).contains("Bind")) {
            return ChatFormatting.RED.toString() + "Bind"
        }
        var substring = enchantment.getTranslatedName(n)
        if (substring.length > n2) {
            substring = substring.substring(0, n2)
        }
        val sb = StringBuilder()
        val s = substring
        var s2 = sb.insert(0, s.substring(0, 1).uppercase(Locale.getDefault())).append(substring.substring(1)).toString()
        if (n > 1) {
            s2 = StringBuilder().insert(0, s2).append(n).toString()
        }
        return s2
    }

    private fun getBiggestArmorTag(player: EntityPlayer): Float {
        var renderOffHand: ItemStack
        var enc: Enchantment?
        var index: Int
        var enchantmentY = 0.0f
        var arm = false
        if (enchant) {
            for (stack in player.inventory.armorInventory) {
                var encY = 0.0f
                if (stack != null) {
                    val enchants = stack.enchantmentTagList
                    index = 0
                    while (index < enchants.tagCount()) {
                        val id = enchants.getCompoundTagAt(index).getShort("id")
                        enc = Enchantment.getEnchantmentByID(id.toInt())
                        if (enc == null) {
                            ++index
                            continue
                        }
                        encY += 10.0f
                        arm = true
                        ++index
                    }
                }
                if (encY <= enchantmentY) continue
                enchantmentY = encY
            }
            val renderMainHand = player.heldItemMainhand.copy()
            if (renderMainHand.hasEffect()) {
                var encY = 0.0f
                val enchants = renderMainHand.enchantmentTagList
                for (index2 in 0 until enchants.tagCount()) {
                    val id = enchants.getCompoundTagAt(index2).getShort("id")
                    Enchantment.getEnchantmentByID(id.toInt()) ?: continue
                    encY += 10.0f
                    arm = true
                }
                if (encY > enchantmentY) {
                    enchantmentY = encY
                }
            }
            if (player.heldItemOffhand.copy().also { renderOffHand = it }.hasEffect()) {
                var encY = 0.0f
                val enchants = renderOffHand.enchantmentTagList
                index = 0
                while (index < enchants.tagCount()) {
                    val id = enchants.getCompoundTagAt(index).getShort("id")
                    enc = Enchantment.getEnchantmentByID(id.toInt())
                    if (enc == null) {
                        ++index
                        continue
                    }
                    encY += 10.0f
                    arm = true
                    ++index
                }
                if (encY > enchantmentY) {
                    enchantmentY = encY
                }
            }
        }
        return (if (arm) 0 else 20).toFloat() + enchantmentY + yAdd
    }

    private fun getDisplayTag(player: EntityPlayer): String {
        var name = player.name
        if (entityID) {
            name = name + " ID: " + player.entityId + " "
        }
        if (gameMode) {
            name = name + "" + getGamemode(player)
        }
        if (ping) {
            var pingStr = ""
            val responseTime = mc.connection?.getPlayerInfo(player.uniqueID)?.responseTime
            pingStr += responseTime
            name = name + " " + pingStr + "ms"
        }
        if (health) {
            val health = player.relativeHealth
            val color = if (health > 18.0f) "\u00a7a" else if (health > 16.0f) "\u00a72" else if (health > 12.0f) "\u00a7e" else if (health > 8.0f) "\u00a76" else if (health > 5.0f) "\u00a7c" else "\u00a74"
            name = name + " " + color + MathHelper.ceil(health)
        }
        if (bot) {
            name += if (isBot(player)) " \u00a7cBot" else ""
        }
        return name
    }


    private fun getGamemode(player: EntityPlayer): String {
        if (player.isCreative) {
            return " [C]"
        }
        if (player.isSpectator) {
            return " [I]"
        }
        if (!player.isAllowEdit && !player.isSpectator) {
            return " [A]"
        }
        return if (!player.isCreative && !player.isSpectator && player.isAllowEdit) {
            " [S]"
        } else ""
    }

    private fun getTagStringColor(player: EntityPlayer): Int {
        var colour = -0x1
        if (isFriend(player)) {
            return -0xff0001
        }
        if (player.isInvisible) {
            colour = -0x410000
        } else if (player.isSneaking) {
            colour = -0x5600
        }
        return colour
    }

    private inline fun drawFont(str: String, x: Float, y: Float, color: Int) {
        if (customFont) mc.fontRenderer.drawString(str, x, y, color, shadow) else MainFontRenderer.drawStringJava(str, x, y, color, 1f, shadow)
    }
    private inline fun getStringWidth(string: String): Float {
        return if (customFont) mc.fontRenderer.getStringWidth(string).toFloat() else MainFontRenderer.getWidth(string)
    }
    private inline fun getHeight(): Float {
        return if (customFont) mc.fontRenderer.FONT_HEIGHT.toFloat() else MainFontRenderer.getHeight()
    }

    class NameTagInfo(val text: String, val color: Int)

    class NameTag(private val entity: EntityPlayer, private val nameDisplay: NameTagInfo, private val handStacks: Array<ItemStack>, private var itemStacks: NonNullList<ItemStack>) {
        fun draw(event: Render3DEvent) {
            val entity2 = mc.renderViewEntity ?: return

            var pos = MathUtils.getInterpolateEntityClose(entity, event.partialTicks)

            val x = pos.x
            val z = pos.z
            var distance = pos.y + if (entity.isSneaking) 0.5 else 0.68
            val y = distance + if (entity.isSneaking) 0.0 else 0.08

            pos = MathUtils.getInterpolateEntityClose(entity2, event.partialTicks)

            val posX = entity2.posX
            val posY = entity2.posY
            val posZ = entity2.posZ
            entity2.posX = pos.x
            entity2.posY = pos.y
            entity2.posZ = pos.z
            distance = entity2.getDistance(x, distance, z)

            val scale =
                if (distance > 0.0) (0.02 + ((nameTagScale / 1000.0f) * distance).coerceAtLeast(nameTagScale / 1000.0)).toFloat()
            else
                0.04f

            GlStateManager.pushMatrix()
            RenderHelper.enableStandardItemLighting()
            GlStateManager.enablePolygonOffset()
            GlStateManager.doPolygonOffset(1.0f, -1500000.0f)
            GlStateManager.disableLighting()
            GlStateManager.translate(x.toFloat(), y.toFloat() + 1.4f, z.toFloat())
            GlStateManager.rotate(-mc.renderManager.playerViewY, 0.0f, 1.0f, 0.0f)
            GlStateManager.rotate(mc.renderManager.playerViewX, if (mc.gameSettings.thirdPersonView == 2) -1.0f else 1.0f, 0.0f, 0.0f)
            GlStateManager.scale(-scale, -scale, scale)
            GlStateManager.disableDepth()
            GlStateManager.enableBlend()
            val nameTag: String = nameDisplay.text
            val width: Float = getStringWidth(nameTag) / 2f
            val renderMainHand = handStacks[0]
            if (renderMainHand.hasEffect() && (renderMainHand.item is ItemTool || renderMainHand.item is ItemArmor)) {
                renderMainHand.stackSize = 1
            }
            val background = ColorRGB(0, 0, 0, alpha)

            val color = if (isFriend(entity)) {
                ColorRGB(0, 255, 255)
            } else {
                GUIManager.firstColor
            }

            GlStateManager.enableBlend()
            GlStateManager.disableTexture2D()
            if (mode == Mode.Default) {
                RenderUtils2D.drawRectFilled(-width - 3f, -(getHeight() + 3.8f), width + 3.0f, 1.5f, background)
                if (healthBar) {
                    val healthBar = (width + 3) * entity.health / entity.maxHealth
                    val getAbsorptionAmountBar = (width + 3) * entity.absorptionAmount / 16f
                    val colorAbsorptionAmount = ColorRGB(255, 255, 0, 100)
                    RenderUtils2D.drawRectFilled(-width - 3, 1.5f, healthBar, 2.5f, getHealthColor(entity, 155))
                    RenderUtils2D.drawRectFilled(-width - 3, 1.5f, getAbsorptionAmountBar, 2.5f, colorAbsorptionAmount)
                }
            } else if (mode == Mode.DotGod) {
                RenderUtils2D.drawBorderedRect(-width - 3, -(getHeight() + 3f), width + 3.0f, 1.5f, widths, color, background)
            }
            GlStateManager.enableTexture2D()
            GlStateManager.disableBlend()
            GlStateManager.color(1f, 1f, 1f, 1f)
            drawFont(nameTag, -width, -getHeight(), nameDisplay.color)
            if (heldStackName && !renderMainHand.isEmpty && renderMainHand.item != Items.AIR) {
                val stackName = renderMainHand.displayName
                val stackNameWidth = getStringWidth(stackName) / 2f
                GL11.glPushMatrix()
                GL11.glScalef(0.75f, 0.75f, 0.0f)
                drawFont(stackName, (-stackNameWidth), -(getBiggestArmorTag(entity) + 20.0f), -0x1)
                GL11.glScalef(1.5f, 1.5f, 1.0f)
                GL11.glPopMatrix()
            }
            GlStateManager.pushMatrix()
            var xOffset = -10
            for (stack in itemStacks) {
                if (stack == null) continue
                xOffset -= 9
            } // start x;

            // render main hand
            xOffset -= 9
            if (renderMainHand.hasEffect() && (renderMainHand.item is ItemTool || renderMainHand.item is ItemArmor)) {

                renderMainHand.stackSize = 1
            }
            renderItemStack(renderMainHand, xOffset, -28)

            // render armor
            xOffset += 18
            for (index in 3 downTo 0) {
                val armourStack = itemStacks[index]
                val stack = armourStack.copy()
                if (stack.hasEffect() && (stack.item is ItemTool || stack.item is ItemArmor)) {
                    stack.stackSize = 1
                }
                renderItemStack(stack, xOffset, -27)
                xOffset += 18
            }

            // render off hand
            val renderOffhand = handStacks[1] //player.getHeldItemOffhand().copy();
            renderItemStack(renderOffhand, xOffset, -28)
            GlStateManager.popMatrix()
            GlStateManager.enableDepth()
            GlStateManager.disableBlend()
            GlStateManager.disablePolygonOffset()
            GlStateManager.doPolygonOffset(1.0f, 1500000.0f)
            GlStateManager.popMatrix()
            entity2.posX = posX
            entity2.posY = posY
            entity2.posZ = posZ
        }
    }
}